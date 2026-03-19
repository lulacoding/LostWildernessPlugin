package com.lostwilderness.rpgcore.party;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async repository for party persistence using "player" datasource.
 * Supports both MySQL and H2.
 */
public final class PartyRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public PartyRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    public void createTablesIfNotExists() {
        String ddlParty = """
            CREATE TABLE IF NOT EXISTS party (
                party_id VARCHAR(36) PRIMARY KEY,
                leader_uuid VARCHAR(36) NOT NULL,
                name VARCHAR(64),
                color VARCHAR(7) DEFAULT '#FFFFFF',
                max_size INT DEFAULT 6,
                created_at BIGINT NOT NULL
            )
            """;

        String ddlMember = """
            CREATE TABLE IF NOT EXISTS party_member (
                party_id VARCHAR(36) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                joined_at BIGINT NOT NULL,
                PRIMARY KEY (party_id, player_uuid)
            )
            """;

        String ddlInvite = """
            CREATE TABLE IF NOT EXISTS party_invite (
                party_id VARCHAR(36) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                invited_at BIGINT NOT NULL,
                PRIMARY KEY (party_id, player_uuid)
            )
            """;

        sql.update(DATASOURCE, ddlParty).join();
        sql.update(DATASOURCE, ddlMember).join();
        sql.update(DATASOURCE, ddlInvite).join();

        // Create indexes
        if (mysql) {
            sql.update(DATASOURCE,
                "CREATE INDEX IF NOT EXISTS idx_party_member_player ON party_member(player_uuid)").join();
            sql.update(DATASOURCE,
                "CREATE INDEX IF NOT EXISTS idx_party_invite_player ON party_invite(player_uuid)").join();
        } else {
            // H2 uses different syntax
            try {
                sql.update(DATASOURCE,
                    "CREATE INDEX IF NOT EXISTS idx_party_member_player ON party_member(player_uuid)").join();
                sql.update(DATASOURCE,
                    "CREATE INDEX IF NOT EXISTS idx_party_invite_player ON party_invite(player_uuid)").join();
            } catch (Exception ignored) {
                // Index might already exist
            }
        }
    }

    // --- Party CRUD ---

    public CompletableFuture<Void> createParty(Party party) {
        String insert = """
            INSERT INTO party (party_id, leader_uuid, name, color, max_size, created_at)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
        return sql.update(DATASOURCE, insert,
                party.getId().toString(),
                party.getLeader().toString(),
                party.getName(),
                party.getColor(),
                party.getMaxSize(),
                party.getCreatedAt())
            .thenCompose(v -> addMember(party.getId(), party.getLeader()));
    }

    public CompletableFuture<Void> deleteParty(UUID partyId) {
        // Delete members and invites first (no CASCADE in H2)
        return removeMember(partyId, null)  // null = delete all
            .thenCompose(v -> removeInvite(partyId, null))
            .thenCompose(v -> sql.update(DATASOURCE,
                "DELETE FROM party WHERE party_id = ?",
                partyId.toString()))
            .thenRun(() -> {});
    }

    public CompletableFuture<Party> getPartyById(UUID partyId) {
        return sql.query(DATASOURCE,
            "SELECT party_id, leader_uuid, name, color, max_size, created_at FROM party WHERE party_id = ?",
            rs -> {
                if (!rs.next()) return null;
                UUID id = UUID.fromString(rs.getString("party_id"));
                UUID leader = UUID.fromString(rs.getString("leader_uuid"));
                String name = rs.getString("name");
                String color = rs.getString("color");
                int maxSize = rs.getInt("max_size");
                long createdAt = rs.getLong("created_at");
                return new Party(id, leader, name, maxSize, createdAt, color);
            },
            partyId.toString())
        .thenCompose(party -> {
            if (party == null) return CompletableFuture.completedFuture(null);
            return loadMembersAndInvites(party);
        });
    }

    public CompletableFuture<UUID> getPlayerParty(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT party_id FROM party_member WHERE player_uuid = ?",
            rs -> rs.next() ? UUID.fromString(rs.getString("party_id")) : null,
            playerUuid.toString());
    }

    // --- Members ---

    public CompletableFuture<Void> addMember(UUID partyId, UUID playerUuid) {
        String insert = mysql
            ? "INSERT INTO party_member (party_id, player_uuid, joined_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE joined_at = VALUES(joined_at)"
            : "MERGE INTO party_member (party_id, player_uuid, joined_at) KEY(party_id, player_uuid) VALUES (?, ?, ?)";

        return sql.update(DATASOURCE, insert,
                partyId.toString(),
                playerUuid.toString(),
                System.currentTimeMillis())
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> removeMember(UUID partyId, UUID playerUuid) {
        if (playerUuid == null) {
            // Delete all members
            return sql.update(DATASOURCE,
                "DELETE FROM party_member WHERE party_id = ?",
                partyId.toString())
            .thenRun(() -> {});
        } else {
            return sql.update(DATASOURCE,
                "DELETE FROM party_member WHERE party_id = ? AND player_uuid = ?",
                partyId.toString(),
                playerUuid.toString())
            .thenRun(() -> {});
        }
    }

    public CompletableFuture<Set<UUID>> getAllMembers(UUID partyId) {
        return sql.query(DATASOURCE,
            "SELECT player_uuid FROM party_member WHERE party_id = ?",
            rs -> {
                Set<UUID> members = new HashSet<>();
                while (rs.next()) {
                    members.add(UUID.fromString(rs.getString("player_uuid")));
                }
                return members;
            },
            partyId.toString());
    }

    // --- Invites ---

    public CompletableFuture<Void> addInvite(UUID partyId, UUID playerUuid) {
        String insert = mysql
            ? "INSERT INTO party_invite (party_id, player_uuid, invited_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE invited_at = VALUES(invited_at)"
            : "MERGE INTO party_invite (party_id, player_uuid, invited_at) KEY(party_id, player_uuid) VALUES (?, ?, ?)";

        return sql.update(DATASOURCE, insert,
                partyId.toString(),
                playerUuid.toString(),
                System.currentTimeMillis())
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> removeInvite(UUID partyId, UUID playerUuid) {
        if (playerUuid == null) {
            // Delete all invites
            return sql.update(DATASOURCE,
                "DELETE FROM party_invite WHERE party_id = ?",
                partyId.toString())
            .thenRun(() -> {});
        } else {
            return sql.update(DATASOURCE,
                "DELETE FROM party_invite WHERE party_id = ? AND player_uuid = ?",
                partyId.toString(),
                playerUuid.toString())
            .thenRun(() -> {});
        }
    }

    public CompletableFuture<Set<UUID>> getAllInvites(UUID partyId) {
        return sql.query(DATASOURCE,
            "SELECT player_uuid FROM party_invite WHERE party_id = ?",
            rs -> {
                Set<UUID> invites = new HashSet<>();
                while (rs.next()) {
                    invites.add(UUID.fromString(rs.getString("player_uuid")));
                }
                return invites;
            },
            partyId.toString());
    }

    public CompletableFuture<Set<UUID>> getPlayerInvites(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT party_id FROM party_invite WHERE player_uuid = ?",
            rs -> {
                Set<UUID> partyIds = new HashSet<>();
                while (rs.next()) {
                    partyIds.add(UUID.fromString(rs.getString("party_id")));
                }
                return partyIds;
            },
            playerUuid.toString());
    }

    // --- Helper methods ---

    private CompletableFuture<Party> loadMembersAndInvites(Party party) {
        return getAllMembers(party.getId())
            .thenCompose(members -> {
                members.forEach(party::addMember);
                return getAllInvites(party.getId());
            })
            .thenApply(invites -> {
                invites.forEach(party::addInvite);
                return party;
            });
    }
}
