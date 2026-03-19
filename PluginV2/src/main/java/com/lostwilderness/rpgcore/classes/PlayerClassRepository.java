package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async load/save for player_classes. Uses "player" datasource.
 * Supports H2 (MERGE) and MySQL/MariaDB (INSERT ... ON DUPLICATE KEY UPDATE).
 */
public final class PlayerClassRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public PlayerClassRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    public void createTableIfNotExists() {
        String ddl = """
                CREATE TABLE IF NOT EXISTS player_classes (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    class_name VARCHAR(64) NOT NULL,
                    chosen_at BIGINT NOT NULL
                )
                """;
        sql.update(DATASOURCE, ddl).join();

        String starterXpDdl = """
                CREATE TABLE IF NOT EXISTS class_starter_xp_grants (
                    player_uuid VARCHAR(36) PRIMARY KEY,
                    class_name VARCHAR(64) NOT NULL,
                    enqueued_at BIGINT NOT NULL,
                    claimed_at BIGINT NULL
                )
                """;
        sql.update(DATASOURCE, starterXpDdl).join();

        String ultimateItemsDdl = """
                CREATE TABLE IF NOT EXISTS class_ultimate_items (
                    player_uuid VARCHAR(36) NOT NULL,
                    class_name VARCHAR(64) NOT NULL,
                    granted_at BIGINT NOT NULL,
                    PRIMARY KEY (player_uuid, class_name)
                )
                """;
        sql.update(DATASOURCE, ultimateItemsDdl).join();
    }

    public CompletableFuture<Optional<PlayerClass>> find(UUID playerUuid) {
        String sqlStr = "SELECT class_name FROM player_classes WHERE player_uuid = ?";
        return sql.query(DATASOURCE, sqlStr, this::mapRow, playerUuid.toString());
    }

    public CompletableFuture<Boolean> exists(UUID playerUuid) {
        String sqlStr = "SELECT 1 FROM player_classes WHERE player_uuid = ?";
        return sql.query(DATASOURCE, sqlStr, rs -> rs.next(), playerUuid.toString());
    }

    public CompletableFuture<Void> save(UUID playerUuid, PlayerClass playerClass, long chosenAt) {
        final String sqlStr = mysql
                ? "INSERT INTO player_classes (player_uuid, class_name, chosen_at) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE class_name = VALUES(class_name), chosen_at = VALUES(chosen_at)"
                : "MERGE INTO player_classes (player_uuid, class_name, chosen_at) KEY(player_uuid) VALUES (?, ?, ?)";

        return sql.update(DATASOURCE, sqlStr,
                playerUuid.toString(),
                playerClass.name(),
                chosenAt)
                .thenRun(() -> {
                });
    }

    public CompletableFuture<Void> enqueueStarterXpGrant(UUID playerUuid, PlayerClass playerClass, long enqueuedAt) {
        final String sqlStr = mysql
                ? "INSERT INTO class_starter_xp_grants (player_uuid, class_name, enqueued_at, claimed_at) VALUES (?, ?, ?, NULL) "
                        +
                        "ON DUPLICATE KEY UPDATE class_name = VALUES(class_name), enqueued_at = VALUES(enqueued_at), claimed_at = NULL"
                : "MERGE INTO class_starter_xp_grants (player_uuid, class_name, enqueued_at, claimed_at) " +
                        "KEY(player_uuid) VALUES (?, ?, ?, NULL)";

        return sql.update(DATASOURCE, sqlStr,
                playerUuid.toString(),
                playerClass.name(),
                enqueuedAt)
                .thenRun(() -> {
                });
    }

    public CompletableFuture<Optional<PlayerClass>> findPendingStarterXpGrant(UUID playerUuid) {
        String sqlStr = "SELECT class_name FROM class_starter_xp_grants WHERE player_uuid = ? AND claimed_at IS NULL";
        return sql.query(DATASOURCE, sqlStr, this::mapRow, playerUuid.toString());
    }

    public CompletableFuture<Boolean> markStarterXpGrantClaimed(UUID playerUuid, long claimedAt) {
        String sqlStr = "UPDATE class_starter_xp_grants SET claimed_at = ? WHERE player_uuid = ? AND claimed_at IS NULL";
        return sql.update(DATASOURCE, sqlStr, claimedAt, playerUuid.toString())
                .thenApply(rows -> rows != null && rows > 0);
    }

    /**
     * Check if a player has already been granted the Ultimate item for a specific class.
     * Part of Phase 7: Class Mastery Items system.
     */
    public CompletableFuture<Boolean> hasUltimateItem(UUID playerUuid, PlayerClass playerClass) {
        String sqlStr = "SELECT 1 FROM class_ultimate_items WHERE player_uuid = ? AND class_name = ?";
        return sql.query(DATASOURCE, sqlStr, rs -> rs.next(), playerUuid.toString(), playerClass.name());
    }

    /**
     * Mark that a player has been granted the Ultimate item for a specific class.
     * Part of Phase 7: Class Mastery Items system.
     */
    public CompletableFuture<Void> markUltimateItemGiven(UUID playerUuid, PlayerClass playerClass) {
        long now = System.currentTimeMillis();
        final String sqlStr = mysql
                ? "INSERT INTO class_ultimate_items (player_uuid, class_name, granted_at) VALUES (?, ?, ?) " +
                        "ON DUPLICATE KEY UPDATE granted_at = VALUES(granted_at)"
                : "MERGE INTO class_ultimate_items (player_uuid, class_name, granted_at) KEY(player_uuid, class_name) VALUES (?, ?, ?)";

        return sql.update(DATASOURCE, sqlStr,
                playerUuid.toString(),
                playerClass.name(),
                now)
                .thenRun(() -> {
                });
    }

    private Optional<PlayerClass> mapRow(ResultSet rs) throws SQLException {
        if (!rs.next())
            return Optional.empty();
        String raw = rs.getString("class_name");
        PlayerClass parsed = PlayerClass.fromStorageValue(raw);
        if (parsed == null) {
            return Optional.empty();
        }
        return Optional.of(parsed);
    }
}
