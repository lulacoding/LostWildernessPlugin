package com.lostwilderness.rpgcore.clans.repo;

import com.lostwilderness.rpgcore.clans.model.Clan;
import com.lostwilderness.rpgcore.clans.model.Invite;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async clan persistence using "player" datasource. H2 and MySQL compatible.
 */
public final class ClanRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public ClanRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    public void createTablesIfNotExists() {
        String ddlClans = """
            CREATE TABLE IF NOT EXISTS clans (
                id VARCHAR(36) PRIMARY KEY,
                name VARCHAR(128) NOT NULL,
                color VARCHAR(32)
            )
            """;
        String ddlMembers = """
            CREATE TABLE IF NOT EXISTS clan_members (
                clan_id VARCHAR(36) NOT NULL,
                player_uuid VARCHAR(36) NOT NULL,
                member_rank VARCHAR(32) NOT NULL,
                PRIMARY KEY (clan_id, player_uuid)
            )
            """;
        String ddlInvites = """
            CREATE TABLE IF NOT EXISTS clan_invites (
                clan_id VARCHAR(36) NOT NULL,
                invited_uuid VARCHAR(36) NOT NULL,
                inviter_uuid VARCHAR(36) NOT NULL,
                invite_time BIGINT NOT NULL
            )
            """;
        String ddlRelations = """
            CREATE TABLE IF NOT EXISTS clan_relations (
                clan_id VARCHAR(36) NOT NULL,
                target_clan_id VARCHAR(36) NOT NULL,
                relation VARCHAR(32) NOT NULL,
                PRIMARY KEY (clan_id, target_clan_id)
            )
            """;
        String ddlWars = """
            CREATE TABLE IF NOT EXISTS clan_wars (
                clan_a VARCHAR(36) NOT NULL,
                clan_b VARCHAR(36) NOT NULL,
                PRIMARY KEY (clan_a, clan_b)
            )
            """;
        String ddlAllianceInvites = """
            CREATE TABLE IF NOT EXISTS alliance_invites (
                clan_id VARCHAR(36) NOT NULL,
                target_clan_id VARCHAR(36) NOT NULL,
                inviter_uuid VARCHAR(36) NOT NULL,
                invite_time BIGINT NOT NULL,
                PRIMARY KEY (clan_id, target_clan_id)
            )
            """;
        sql.update(DATASOURCE, ddlClans).join();
        sql.update(DATASOURCE, ddlMembers).join();
        sql.update(DATASOURCE, ddlInvites).join();
        sql.update(DATASOURCE, ddlRelations).join();
        sql.update(DATASOURCE, ddlWars).join();
        sql.update(DATASOURCE, ddlAllianceInvites).join();
    }

    /** Canonical ordering for war pair (smaller UUID first). */
    private static String warKeyA(UUID clan1, UUID clan2) {
        return clan1.compareTo(clan2) <= 0 ? clan1.toString() : clan2.toString();
    }

    private static String warKeyB(UUID clan1, UUID clan2) {
        return clan1.compareTo(clan2) <= 0 ? clan2.toString() : clan1.toString();
    }

    public CompletableFuture<Boolean> areAtWar(UUID clan1, UUID clan2) {
        if (clan1.equals(clan2)) return CompletableFuture.completedFuture(false);
        String a = warKeyA(clan1, clan2);
        String b = warKeyB(clan1, clan2);
        return sql.query(DATASOURCE,
            "SELECT 1 FROM clan_wars WHERE clan_a = ? AND clan_b = ? LIMIT 1",
            rs -> rs.next(),
            a, b);
    }

    public CompletableFuture<Void> addWar(UUID clan1, UUID clan2) {
        if (clan1.equals(clan2)) return CompletableFuture.completedFuture(null);
        String a = warKeyA(clan1, clan2);
        String b = warKeyB(clan1, clan2);
        String insert = mysql
            ? "INSERT INTO clan_wars (clan_a, clan_b) VALUES (?, ?) ON DUPLICATE KEY UPDATE clan_a = clan_a"
            : "MERGE INTO clan_wars (clan_a, clan_b) KEY(clan_a, clan_b) VALUES (?, ?)";
        return sql.update(DATASOURCE, insert, a, b).thenRun(() -> {});
    }

    public CompletableFuture<Void> removeWar(UUID clan1, UUID clan2) {
        if (clan1.equals(clan2)) return CompletableFuture.completedFuture(null);
        String a = warKeyA(clan1, clan2);
        String b = warKeyB(clan1, clan2);
        return sql.update(DATASOURCE, "DELETE FROM clan_wars WHERE clan_a = ? AND clan_b = ?", a, b).thenRun(() -> {});
    }

    public CompletableFuture<List<UUID[]>> getActiveWars() {
        return sql.query(DATASOURCE,
            "SELECT clan_a, clan_b FROM clan_wars",
            rs -> {
                List<UUID[]> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new UUID[]{ UUID.fromString(rs.getString("clan_a")), UUID.fromString(rs.getString("clan_b")) });
                }
                return list;
            });
    }

    public CompletableFuture<Void> createAllianceInvite(UUID clanId, UUID targetClanId, UUID inviterUuid) {
        long time = System.currentTimeMillis();
        String upsert = mysql
            ? "INSERT INTO alliance_invites (clan_id, target_clan_id, inviter_uuid, invite_time) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE inviter_uuid = VALUES(inviter_uuid), invite_time = VALUES(invite_time)"
            : "MERGE INTO alliance_invites (clan_id, target_clan_id, inviter_uuid, invite_time) KEY(clan_id, target_clan_id) VALUES (?, ?, ?, ?)";
        return sql.update(DATASOURCE, upsert, clanId.toString(), targetClanId.toString(), inviterUuid.toString(), time).thenRun(() -> {});
    }

    public CompletableFuture<List<UUID>> getPendingAllianceInvitesTargeting(UUID targetClanId) {
        return sql.query(DATASOURCE,
            "SELECT clan_id FROM alliance_invites WHERE target_clan_id = ?",
            rs -> {
                List<UUID> list = new ArrayList<>();
                while (rs.next()) list.add(UUID.fromString(rs.getString("clan_id")));
                return list;
            },
            targetClanId.toString());
    }

    public CompletableFuture<Void> removeAllianceInvite(UUID clanId, UUID targetClanId) {
        return sql.update(DATASOURCE, "DELETE FROM alliance_invites WHERE clan_id = ? AND target_clan_id = ?", clanId.toString(), targetClanId.toString()).thenRun(() -> {});
    }

    public CompletableFuture<List<UUID>> getAllies(UUID clanId) {
        return sql.query(DATASOURCE,
            "SELECT target_clan_id FROM clan_relations WHERE clan_id = ? AND relation = 'alliance'",
            rs -> {
                List<UUID> list = new ArrayList<>();
                while (rs.next()) list.add(UUID.fromString(rs.getString("target_clan_id")));
                return list;
            },
            clanId.toString());
    }

    public CompletableFuture<Void> saveClan(Clan clan) {
        String upsert = mysql
            ? "INSERT INTO clans (id, name, color) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE name = VALUES(name), color = VALUES(color)"
            : "MERGE INTO clans (id, name, color) KEY(id) VALUES (?, ?, ?)";
        return sql.update(DATASOURCE, upsert,
            clan.getId().toString(),
            clan.getName(),
            clan.getColorHex()).thenRun(() -> {});
    }

    public CompletableFuture<Clan> getClanById(UUID clanId) {
        return sql.query(DATASOURCE,
            "SELECT id, name, color FROM clans WHERE id = ?",
            this::mapClan,
            clanId.toString());
    }

    public CompletableFuture<Clan> getClanByName(String name) {
        return sql.query(DATASOURCE,
            "SELECT id, name, color FROM clans WHERE name = ?",
            this::mapClan,
            name);
    }

    public CompletableFuture<UUID> getClanOfPlayer(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT clan_id FROM clan_members WHERE player_uuid = ? LIMIT 1",
            rs -> rs.next() ? UUID.fromString(rs.getString("clan_id")) : null,
            playerUuid.toString());
    }

    public CompletableFuture<Void> addMember(UUID clanId, UUID playerUuid, String rank) {
        String upsert = mysql
            ? "INSERT INTO clan_members (clan_id, player_uuid, member_rank) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE member_rank = VALUES(member_rank)"
            : "MERGE INTO clan_members (clan_id, player_uuid, member_rank) KEY(clan_id, player_uuid) VALUES (?, ?, ?)";
        return sql.update(DATASOURCE, upsert, clanId.toString(), playerUuid.toString(), rank).thenRun(() -> {});
    }

    public CompletableFuture<Integer> getClanMemberCount(UUID clanId) {
        return sql.query(DATASOURCE,
            "SELECT COUNT(*) AS cnt FROM clan_members WHERE clan_id = ?",
            rs -> rs.next() ? rs.getInt("cnt") : 0,
            clanId.toString());
    }

    public CompletableFuture<Void> removeMemberFromClan(UUID clanId, UUID playerUuid) {
        return sql.update(DATASOURCE,
            "DELETE FROM clan_members WHERE clan_id = ? AND player_uuid = ?",
            clanId.toString(), playerUuid.toString()).thenRun(() -> {});
    }

    public CompletableFuture<String> getMemberRank(UUID clanId, UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT member_rank FROM clan_members WHERE clan_id = ? AND player_uuid = ?",
            rs -> rs.next() ? rs.getString("member_rank") : null,
            clanId.toString(), playerUuid.toString());
    }

    public CompletableFuture<Void> createInvite(Invite inv) {
        return removeInvitesForPlayer(inv.getInvitedUuid())
            .thenCompose(v -> sql.update(DATASOURCE,
                "INSERT INTO clan_invites (clan_id, invited_uuid, inviter_uuid, invite_time) VALUES (?, ?, ?, ?)",
                inv.getClanId().toString(),
                inv.getInvitedUuid().toString(),
                inv.getInviterUuid().toString(),
                inv.getInviteTime()).thenRun(() -> {}));
    }

    public CompletableFuture<List<Invite>> getPendingInvites(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT clan_id, inviter_uuid, invite_time FROM clan_invites WHERE invited_uuid = ?",
            rs -> {
                List<Invite> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Invite(
                        UUID.fromString(rs.getString("clan_id")),
                        playerUuid,
                        UUID.fromString(rs.getString("inviter_uuid")),
                        rs.getLong("invite_time")
                    ));
                }
                return list;
            },
            playerUuid.toString());
    }

    public CompletableFuture<Void> removeInvitesForPlayer(UUID playerUuid) {
        return sql.update(DATASOURCE, "DELETE FROM clan_invites WHERE invited_uuid = ?", playerUuid.toString())
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> updateClanColor(UUID clanId, String colorHex) {
        return sql.update(DATASOURCE, "UPDATE clans SET color = ? WHERE id = ?", colorHex, clanId.toString())
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> addClanRelation(UUID clanId, UUID targetClanId, String relation) {
        String upsert = mysql
            ? "INSERT INTO clan_relations (clan_id, target_clan_id, relation) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE relation = VALUES(relation)"
            : "MERGE INTO clan_relations (clan_id, target_clan_id, relation) KEY(clan_id, target_clan_id) VALUES (?, ?, ?)";
        return sql.update(DATASOURCE, upsert, clanId.toString(), targetClanId.toString(), relation).thenRun(() -> {});
    }

    public CompletableFuture<Void> removeClanRelation(UUID clanId, UUID targetClanId) {
        return sql.update(DATASOURCE,
            "DELETE FROM clan_relations WHERE clan_id = ? AND target_clan_id = ?",
            clanId.toString(), targetClanId.toString()).thenRun(() -> {});
    }

    public CompletableFuture<Void> removeClanAndData(UUID clanId) {
        String id = clanId.toString();
        return sql.update(DATASOURCE, "DELETE FROM clan_relations WHERE clan_id = ? OR target_clan_id = ?", id, id)
            .thenCompose(v -> sql.update(DATASOURCE, "DELETE FROM clan_invites WHERE clan_id = ?", id))
            .thenCompose(v -> sql.update(DATASOURCE, "DELETE FROM clan_wars WHERE clan_a = ? OR clan_b = ?", id, id))
            .thenCompose(v -> sql.update(DATASOURCE, "DELETE FROM alliance_invites WHERE clan_id = ? OR target_clan_id = ?", id, id))
            .thenCompose(v -> sql.update(DATASOURCE, "DELETE FROM clan_members WHERE clan_id = ?", id))
            .thenCompose(v -> sql.update(DATASOURCE, "DELETE FROM clans WHERE id = ?", id))
            .thenRun(() -> {});
    }

    public CompletableFuture<List<Clan>> getAllClans() {
        return sql.query(DATASOURCE,
            "SELECT id, name, color FROM clans ORDER BY name",
            rs -> {
                List<Clan> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new Clan(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("name"),
                        rs.getString("color")
                    ));
                }
                return list;
            });
    }

    public CompletableFuture<List<Map.Entry<UUID, String>>> getClanMembersWithRanks(UUID clanId) {
        return sql.query(DATASOURCE,
            "SELECT player_uuid, member_rank FROM clan_members WHERE clan_id = ?",
            rs -> {
                List<Map.Entry<UUID, String>> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new AbstractMap.SimpleEntry<>(
                        UUID.fromString(rs.getString("player_uuid")),
                        rs.getString("member_rank")
                    ));
                }
                return list;
            },
            clanId.toString());
    }

    private Clan mapClan(ResultSet rs) throws SQLException {
        if (!rs.next()) return null;
        return new Clan(
            UUID.fromString(rs.getString("id")),
            rs.getString("name"),
            rs.getString("color")
        );
    }
}
