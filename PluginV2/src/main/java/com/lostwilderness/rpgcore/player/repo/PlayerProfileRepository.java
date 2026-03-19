package com.lostwilderness.rpgcore.player.repo;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.player.PlayerProfile;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Async load/save for player_profiles. Uses "player" datasource.
 * Supports H2 (MERGE) and MySQL/MariaDB (INSERT ... ON DUPLICATE KEY UPDATE).
 */
public final class PlayerProfileRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final Executor executor;
    private final boolean mysql;

    public PlayerProfileRepository(SqlExecutor sql, Executor executor, boolean useMysql) {
        this.sql = sql;
        this.executor = executor;
        this.mysql = useMysql;
    }

    public void createTableIfNotExists() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS player_profiles (
                uuid VARCHAR(36) PRIMARY KEY,
                last_seen_at BIGINT NOT NULL
            )
            """;
        sql.update(DATASOURCE, ddl).join();
    }

    public CompletableFuture<Optional<PlayerProfile>> findById(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT uuid, last_seen_at FROM player_profiles WHERE uuid = ?",
            this::mapRow,
            uuid.toString());
    }

    public CompletableFuture<Void> save(PlayerProfile profile) {
        final String sqlStr = mysql
            ? "INSERT INTO player_profiles (uuid, last_seen_at) VALUES (?, ?) ON DUPLICATE KEY UPDATE last_seen_at = VALUES(last_seen_at)"
            : "MERGE INTO player_profiles (uuid, last_seen_at) KEY(uuid) VALUES (?, ?)";
        return sql.update(DATASOURCE, sqlStr,
            profile.getUuid().toString(),
            profile.getLastSeenAt().toEpochMilli())
            .thenRun(() -> {});
    }

    private Optional<PlayerProfile> mapRow(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        long lastSeen = rs.getLong("last_seen_at");
        return Optional.of(new PlayerProfile(uuid, Instant.ofEpochMilli(lastSeen)));
    }
}
