package com.lostwilderness.rpgcore.boss;

import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class BossKillRepository {

    private final DatabaseProvider db;
    private final SqlExecutor executor;
    private final boolean mysql;

    public BossKillRepository(DatabaseProvider db, SqlExecutor executor, boolean mysql) {
        this.db = db;
        this.executor = executor;
        this.mysql = mysql;
        createTablesIfNotExists();
    }

    private void createTablesIfNotExists() {
        CompletableFuture.runAsync(() -> {
            DataSource ds = db.getDataSource("player");
            if (ds == null) return;

            try (Connection conn = ds.getConnection();
                 Statement st = conn.createStatement()) {
                
                // boss_kills table
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS boss_kills (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        wither_kills INT NOT NULL DEFAULT 0,
                        devoider_kills INT NOT NULL DEFAULT 0
                    )
                    """);

                // clan_boss_kills table
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS clan_boss_kills (
                        clan_id VARCHAR(36) PRIMARY KEY,
                        wither_kills INT NOT NULL DEFAULT 0,
                        devoider_kills INT NOT NULL DEFAULT 0
                    )
                    """);

                Bukkit.getLogger().info("[BossKillRepository] Tables verified.");
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[BossKillRepository] Failed to create tables", e);
            }
        });
    }

    public CompletableFuture<Void> incrementPlayerWitherKills(UUID playerUuid) {
        String sql;
        if (mysql) {
            sql = "INSERT INTO boss_kills (player_uuid, wither_kills, devoider_kills) VALUES (?, 1, 0) " +
                  "ON DUPLICATE KEY UPDATE wither_kills = wither_kills + 1";
        } else {
            sql = "MERGE INTO boss_kills KEY(player_uuid) VALUES (?, (SELECT COALESCE(MAX(wither_kills), 0) + 1 FROM boss_kills WHERE player_uuid = ?), 0)";
            // H2 MERGE is a bit different, but for simplicity let's use a conditional update if needed.
            // Actually, H2 supports MERGE. Let's try to keep it simple or use UPSERT logic.
            sql = "INSERT INTO boss_kills (player_uuid, wither_kills, devoider_kills) VALUES (?, 1, 0) " +
                  "ON DUPLICATE KEY UPDATE wither_kills = wither_kills + 1"; 
            // H2 actually supports ON DUPLICATE KEY UPDATE in MySQL mode or just MERGE.
        }
        
        // Let's use standard SQL that works for both if possible, or branch.
        // Paper's H2 usually runs in MySQL compatibility mode.
        return executor.update("player", sql, playerUuid.toString()).thenApply(v -> null);
    }

    public CompletableFuture<Void> incrementPlayerDevoiderKills(UUID playerUuid) {
        String sql = "INSERT INTO boss_kills (player_uuid, wither_kills, devoider_kills) VALUES (?, 0, 1) " +
                     "ON DUPLICATE KEY UPDATE devoider_kills = devoider_kills + 1";
        return executor.update("player", sql, playerUuid.toString()).thenApply(v -> null);
    }

    public CompletableFuture<Integer> getPlayerDevoiderKills(UUID playerUuid) {
        return executor.query("player", "SELECT devoider_kills FROM boss_kills WHERE player_uuid=?",
                rs -> rs.next() ? rs.getInt("devoider_kills") : 0, playerUuid.toString());
    }

    public CompletableFuture<Void> incrementClanWitherKills(UUID clanId) {
        String sql = "INSERT INTO clan_boss_kills (clan_id, wither_kills, devoider_kills) VALUES (?, 1, 0) " +
                     "ON DUPLICATE KEY UPDATE wither_kills = wither_kills + 1";
        return executor.update("player", sql, clanId.toString()).thenApply(v -> null);
    }

    public CompletableFuture<Void> incrementClanDevoiderKills(UUID clanId) {
        String sql = "INSERT INTO clan_boss_kills (clan_id, wither_kills, devoider_kills) VALUES (?, 0, 1) " +
                     "ON DUPLICATE KEY UPDATE devoider_kills = devoider_kills + 1";
        return executor.update("player", sql, clanId.toString()).thenApply(v -> null);
    }

    public CompletableFuture<Integer> getClanWitherKills(UUID clanId) {
        return executor.query("player", "SELECT wither_kills FROM clan_boss_kills WHERE clan_id = ?",
                rs -> rs.next() ? rs.getInt("wither_kills") : 0, clanId.toString());
    }

    public CompletableFuture<Boolean> anyPlayerOrClanHasWitherKills(int threshold) {
        return executor.query("player", "SELECT 1 FROM boss_kills WHERE wither_kills >= ? LIMIT 1",
                rs -> rs.next(), threshold).thenCompose(found -> {
            if (found) return CompletableFuture.completedFuture(true);
            return executor.query("player", "SELECT 1 FROM clan_boss_kills WHERE wither_kills >= ? LIMIT 1",
                    rs -> rs.next(), threshold);
        });
    }

    public CompletableFuture<Boolean> anyPlayerOrClanHasDevoiderKills(int threshold) {
        return executor.query("player", "SELECT 1 FROM boss_kills WHERE devoider_kills >= ? LIMIT 1",
                rs -> rs.next(), threshold).thenCompose(found -> {
            if (found) return CompletableFuture.completedFuture(true);
            return executor.query("player", "SELECT 1 FROM clan_boss_kills WHERE devoider_kills >= ? LIMIT 1",
                    rs -> rs.next(), threshold);
        });
    }
}
