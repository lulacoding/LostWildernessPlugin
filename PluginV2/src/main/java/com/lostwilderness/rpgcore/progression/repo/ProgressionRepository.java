package com.lostwilderness.rpgcore.progression.repo;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async load/save for player_achievements and progression_counters. Uses "player" datasource.
 * Supports H2 (MERGE) and MySQL (INSERT ... ON DUPLICATE KEY UPDATE).
 */
public final class ProgressionRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public ProgressionRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    public void createTableIfNotExists() {
        String ddlAchievements = """
            CREATE TABLE IF NOT EXISTS player_achievements (
                player_uuid VARCHAR(36) NOT NULL,
                `key` VARCHAR(255) NOT NULL,
                unlocked_at BIGINT NOT NULL,
                PRIMARY KEY (player_uuid, `key`)
            )
            """;
        // VALUE is reserved in H2/SQL; use counter_value
        String ddlCounters = """
            CREATE TABLE IF NOT EXISTS progression_counters (
                player_uuid VARCHAR(36) NOT NULL,
                counter_key VARCHAR(255) NOT NULL,
                counter_value BIGINT NOT NULL,
                PRIMARY KEY (player_uuid, counter_key)
            )
            """;
        sql.update(DATASOURCE, ddlAchievements).join();
        sql.update(DATASOURCE, ddlCounters).join();

        // Migration for old V1 column names if they exist
        if (mysql) {
            try {
                sql.update(DATASOURCE, "ALTER TABLE progression_counters CHANGE COLUMN `value` counter_value BIGINT NOT NULL").join();
            } catch (Exception ignored) {}
        }
    }

    public CompletableFuture<Boolean> hasUnlocked(UUID playerUuid, String key) {
        return sql.query(DATASOURCE,
            "SELECT 1 FROM player_achievements WHERE player_uuid = ? AND `key` = ? LIMIT 1",
            rs -> rs.next(),
            playerUuid.toString(),
            key);
    }

    public CompletableFuture<Void> unlock(UUID playerUuid, String key, long unlockedAt) {
        final String sqlStr = mysql
            ? "INSERT INTO player_achievements (player_uuid, `key`, unlocked_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE unlocked_at = VALUES(unlocked_at)"
            : "MERGE INTO player_achievements (player_uuid, `key`, unlocked_at) KEY(player_uuid, `key`) VALUES (?, ?, ?)";
        return sql.update(DATASOURCE, sqlStr, playerUuid.toString(), key, unlockedAt).thenRun(() -> {});
    }

    public CompletableFuture<List<String>> getUnlockedKeys(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT `key` FROM player_achievements WHERE player_uuid = ? ORDER BY unlocked_at ASC",
            rs -> {
                List<String> keys = new ArrayList<>();
                while (rs.next()) {
                    keys.add(rs.getString("key"));
                }
                return keys;
            },
            playerUuid.toString());
    }

    public CompletableFuture<Long> getCounter(UUID playerUuid, String counterKey) {
        return sql.query(DATASOURCE,
            "SELECT counter_value FROM progression_counters WHERE player_uuid = ? AND counter_key = ?",
            rs -> rs.next() ? rs.getLong("counter_value") : 0L,
            playerUuid.toString(),
            counterKey);
    }

    public CompletableFuture<Long> incrementCounter(UUID playerUuid, String counterKey) {
        return getCounter(playerUuid, counterKey).thenCompose(current -> {
            long newValue = current + 1;
            return setCounter(playerUuid, counterKey, newValue).thenApply(x -> newValue);
        });
    }

    /** Set counter to an exact value (upsert). Returns the value that was set. */
    public CompletableFuture<Long> setCounter(UUID playerUuid, String counterKey, long value) {
        if (mysql) {
            String upsert = "INSERT INTO progression_counters (player_uuid, counter_key, counter_value) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE counter_value = VALUES(counter_value)";
            return sql.update(DATASOURCE, upsert, playerUuid.toString(), counterKey, value).thenApply(v -> value);
        } else {
            String merge = "MERGE INTO progression_counters (player_uuid, counter_key, counter_value) KEY(player_uuid, counter_key) VALUES (?, ?, ?)";
            return sql.update(DATASOURCE, merge, playerUuid.toString(), counterKey, value).thenApply(v -> value);
        }
    }

    /** Add delta to counter (for playtime etc.). */
    public CompletableFuture<Long> addToCounter(UUID playerUuid, String counterKey, long delta) {
        return getCounter(playerUuid, counterKey).thenCompose(current -> {
            long newValue = current + delta;
            return setCounter(playerUuid, counterKey, newValue).thenApply(v -> newValue);
        });
    }
}
