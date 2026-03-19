package com.lostwilderness.rpgcore.reputation;

import com.lostwilderness.rpgcore.infra.db.DatabaseProvider;
import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.Bukkit;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

public class ReputationRepository {

    private final DatabaseProvider db;
    private final SqlExecutor executor;
    private final boolean mysql;

    public ReputationRepository(DatabaseProvider db, SqlExecutor executor, boolean mysql) {
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
                
                // player_reputation table
                st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS player_reputation (
                        player_uuid VARCHAR(36) NOT NULL,
                        faction VARCHAR(32) NOT NULL,
                        points INT NOT NULL DEFAULT 0,
                        PRIMARY KEY (player_uuid, faction)
                    )
                    """);

                Bukkit.getLogger().info("[ReputationRepository] Tables verified.");
            } catch (SQLException e) {
                Bukkit.getLogger().log(Level.SEVERE, "[ReputationRepository] Failed to create tables", e);
            }
        });
    }

    public CompletableFuture<Integer> getPoints(UUID playerUuid, Faction faction) {
        return executor.query("player", "SELECT points FROM player_reputation WHERE player_uuid=? AND faction=?",
                rs -> rs.next() ? rs.getInt("points") : 0, playerUuid.toString(), faction.name());
    }

    public CompletableFuture<Map<Faction, Integer>> getAllPoints(UUID playerUuid) {
        return executor.query("player", "SELECT faction, points FROM player_reputation WHERE player_uuid=?",
                rs -> {
                    Map<Faction, Integer> map = new EnumMap<>(Faction.class);
                    while (rs.next()) {
                        try {
                            Faction faction = Faction.valueOf(rs.getString("faction"));
                            map.put(faction, rs.getInt("points"));
                        } catch (IllegalArgumentException ignored) {}
                    }
                    return map;
                }, playerUuid.toString());
    }

    public CompletableFuture<Void> setPoints(UUID playerUuid, Faction faction, int points) {
        String sql;
        if (mysql) {
            sql = "INSERT INTO player_reputation (player_uuid, faction, points) VALUES (?, ?, ?) " +
                  "ON DUPLICATE KEY UPDATE points = ?";
        } else {
            sql = "MERGE INTO player_reputation KEY(player_uuid, faction) VALUES (?, ?, ?)";
        }
        
        Object[] params = mysql 
            ? new Object[]{playerUuid.toString(), faction.name(), points, points}
            : new Object[]{playerUuid.toString(), faction.name(), points};

        return executor.update("player", sql, params).thenApply(v -> null);
    }
}
