package com.lostwilderness.rpgcore.portals;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import org.bukkit.Location;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async persistence for portals, pending_portals, and portal_transfer_entities.
 * Uses "player" datasource; schema matches legacy for compatibility.
 */
public final class PortalRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public PortalRepository(SqlExecutor sql, boolean mysql) {
        this.sql = sql;
        this.mysql = mysql;
    }

    public void createTablesIfNotExists() {
        // Log start
        java.util.logging.Logger log = java.util.logging.Logger.getLogger("RPGCore-Portals");
        
        try {
            sql.query(DATASOURCE, "SELECT DATABASE(), SCHEMA()", rs -> {
                if (rs.next()) {
                    return "Database: " + rs.getString(1) + ", Schema: " + rs.getString(2);
                }
                return "Unknown";
            }).thenAccept(info -> log.info("Checking/creating portal tables in " + info));
        } catch (Exception ignored) {}

        String ddlPortals = """
            CREATE TABLE IF NOT EXISTS portals (
                player_uuid VARCHAR(36) NOT NULL,
                portal_name VARCHAR(64) NOT NULL,
                direction VARCHAR(16),
                survival_world VARCHAR(64),
                survival_x DOUBLE, survival_y DOUBLE, survival_z DOUBLE,
                amplified_world VARCHAR(64),
                amplified_x DOUBLE, amplified_y DOUBLE, amplified_z DOUBLE,
                exit_x DOUBLE, exit_y DOUBLE, exit_z DOUBLE,
                width INT DEFAULT 4,
                height INT DEFAULT 5,
                PRIMARY KEY (player_uuid, portal_name)
            )
            """;
        String ddlPending = """
            CREATE TABLE IF NOT EXISTS pending_portals (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid VARCHAR(36) NOT NULL,
                portal_name VARCHAR(64) NOT NULL,
                direction VARCHAR(16),
                survival_world VARCHAR(64),
                survival_x DOUBLE, survival_y DOUBLE, survival_z DOUBLE,
                amplified_world VARCHAR(64),
                amplified_x DOUBLE, amplified_y DOUBLE, amplified_z DOUBLE,
                status VARCHAR(32) DEFAULT 'pending',
                width INT DEFAULT 4,
                height INT DEFAULT 5
            )
            """;
        String ddlTransfer = """
            CREATE TABLE IF NOT EXISTS portal_transfer_entities (
                id INT AUTO_INCREMENT PRIMARY KEY,
                player_uuid VARCHAR(36) NOT NULL,
                portal_name VARCHAR(64) NOT NULL,
                entity_data TEXT,
                created_at BIGINT
            )
            """;
        try {
            sql.update(DATASOURCE, ddlPortals).join();
            sql.update(DATASOURCE, ddlPending).join();
            sql.update(DATASOURCE, ddlTransfer).join();

            // Migration for pending_portals (add width/height if missing)
            if (mysql) {
                try {
                    sql.update(DATASOURCE, "ALTER TABLE pending_portals ADD COLUMN IF NOT EXISTS width INT DEFAULT 4").join();
                    sql.update(DATASOURCE, "ALTER TABLE pending_portals ADD COLUMN IF NOT EXISTS height INT DEFAULT 5").join();
                    sql.update(DATASOURCE, "ALTER TABLE portals ADD COLUMN IF NOT EXISTS width INT DEFAULT 4").join();
                    sql.update(DATASOURCE, "ALTER TABLE portals ADD COLUMN IF NOT EXISTS height INT DEFAULT 5").join();
                } catch (Exception ignored) {}
            } else {
                // H2 syntax
                try {
                    sql.update(DATASOURCE, "ALTER TABLE pending_portals ADD COLUMN IF NOT EXISTS width INT DEFAULT 4").join();
                    sql.update(DATASOURCE, "ALTER TABLE pending_portals ADD COLUMN IF NOT EXISTS height INT DEFAULT 5").join();
                    sql.update(DATASOURCE, "ALTER TABLE portals ADD COLUMN IF NOT EXISTS width INT DEFAULT 4").join();
                    sql.update(DATASOURCE, "ALTER TABLE portals ADD COLUMN IF NOT EXISTS height INT DEFAULT 5").join();
                } catch (Exception ignored) {}
            }

            // Verify
            sql.query(DATASOURCE, "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_NAME IN ('PORTALS', 'PENDING_PORTALS', 'PORTAL_TRANSFER_ENTITIES')", rs -> {
                List<String> found = new ArrayList<>();
                while (rs.next()) found.add(rs.getString(1));
                return found;
            }).thenAccept(found -> log.info("Verified tables in PUBLIC schema: " + String.join(", ", found)));

            log.info("Portal tables ready.");
        } catch (Exception e) {
            log.severe("FAILED to create portal tables: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public CompletableFuture<Integer> getPlayerPortalCount(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT COUNT(*) FROM portals WHERE player_uuid = ?",
            rs -> rs.next() ? rs.getInt(1) : 0,
            playerUuid.toString());
    }

    public CompletableFuture<Boolean> hasPermanentPortalAt(UUID playerUuid, boolean isSurvival,
                                                          String worldName, double x, double y, double z) {
        String worldCol = isSurvival ? "survival_world" : "amplified_world";
        String xCol = isSurvival ? "survival_x" : "amplified_x";
        String yCol = isSurvival ? "survival_y" : "amplified_y";
        String zCol = isSurvival ? "survival_z" : "amplified_z";
        String sqlStr = "SELECT 1 FROM portals WHERE player_uuid = ? AND " + worldCol + " = ? AND " + xCol + " = ? AND " + yCol + " = ? AND " + zCol + " = ? LIMIT 1";
        return sql.query(DATASOURCE, sqlStr,
            ResultSet::next,
            playerUuid.toString(), worldName, x, y, z);
    }

    public CompletableFuture<String> getPortalNameAtLocation(UUID playerUuid, boolean isSurvival,
                                                              String worldName, double x, double y, double z) {
        String worldCol = isSurvival ? "survival_world" : "amplified_world";
        String xCol = isSurvival ? "survival_x" : "amplified_x";
        String yCol = isSurvival ? "survival_y" : "amplified_y";
        String zCol = isSurvival ? "survival_z" : "amplified_z";
        String sqlStr = "SELECT portal_name FROM portals WHERE player_uuid = ? AND " + worldCol + " = ? AND " + xCol + " = ? AND " + yCol + " = ? AND " + zCol + " = ? LIMIT 1";
        return sql.query(DATASOURCE, sqlStr,
            rs -> rs.next() ? rs.getString("portal_name") : null,
            playerUuid.toString(), worldName, x, y, z);
    }

    public CompletableFuture<Void> saveLinkedPortal(UUID playerUuid, String portalName, String direction,
                                                     Location survivalLoc, Location amplifiedLoc, int width, int height) {
        String sqlStr = "INSERT INTO portals (player_uuid, portal_name, direction, " +
            "survival_world, survival_x, survival_y, survival_z, " +
            "amplified_world, amplified_x, amplified_y, amplified_z, width, height) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        return sql.update(DATASOURCE, sqlStr,
            playerUuid.toString(), portalName, direction,
            survivalLoc.getWorld().getName(), survivalLoc.getX(), survivalLoc.getY(), survivalLoc.getZ(),
            amplifiedLoc.getWorld().getName(), amplifiedLoc.getX(), amplifiedLoc.getY(), amplifiedLoc.getZ(),
            width, height)
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> updateAmplifiedPortal(UUID playerUuid, String portalName, Location frameOrigin) {
        String sqlStr = "UPDATE portals SET amplified_world=?, amplified_x=?, amplified_y=?, amplified_z=? WHERE player_uuid=? AND portal_name=?";
        return sql.update(DATASOURCE, sqlStr,
            frameOrigin.getWorld().getName(), frameOrigin.getX(), frameOrigin.getY(), frameOrigin.getZ(),
            playerUuid.toString(), portalName)
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> updateSurvivalPortal(UUID playerUuid, String portalName, Location frameOrigin) {
        String sqlStr = "UPDATE portals SET survival_world=?, survival_x=?, survival_y=?, survival_z=? WHERE player_uuid=? AND portal_name=?";
        return sql.update(DATASOURCE, sqlStr,
            frameOrigin.getWorld().getName(), frameOrigin.getX(), frameOrigin.getY(), frameOrigin.getZ(),
            playerUuid.toString(), portalName)
            .thenRun(() -> {});
    }

    public CompletableFuture<Void> updateExitLocation(UUID playerUuid, String portalName, Location exit) {
        String sqlStr = "UPDATE portals SET exit_x=?, exit_y=?, exit_z=? WHERE player_uuid=? AND portal_name=?";
        return sql.update(DATASOURCE, sqlStr,
            exit.getX(), exit.getY(), exit.getZ(),
            playerUuid.toString(), portalName)
            .thenRun(() -> {});
    }

    /** Returns exit location info (world name + coords). Caller builds Location on main thread. */
    public CompletableFuture<ExitLocationInfo> getExitLocation(UUID playerUuid, String portalName, boolean isSurvival) {
        String sqlStr = "SELECT exit_x, exit_y, exit_z, amplified_world, survival_world FROM portals WHERE player_uuid = ? AND portal_name = ? LIMIT 1";
        return sql.query(DATASOURCE, sqlStr,
            rs -> {
                if (!rs.next()) return null;
                double ex = rs.getDouble("exit_x");
                if (rs.wasNull()) return null;
                String worldName = isSurvival ? rs.getString("amplified_world") : rs.getString("survival_world");
                return new ExitLocationInfo(worldName, ex, rs.getDouble("exit_y"), rs.getDouble("exit_z"));
            },
            playerUuid.toString(), portalName);
    }

    public record ExitLocationInfo(String worldName, double x, double y, double z) {}

    /** INSERT into pending_portals. When isSurvival, sets survival_*; otherwise sets amplified_*. */
    public CompletableFuture<Void> addPendingPortal(UUID playerUuid, String portalName, String direction,
                                                    Location origin, boolean isSurvival, int width, int height) {
        if (isSurvival) {
            String sqlStr = "INSERT INTO pending_portals (player_uuid, portal_name, direction, survival_world, survival_x, survival_y, survival_z, status, width, height) VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', ?, ?)";
            return sql.update(DATASOURCE, sqlStr,
                playerUuid.toString(), portalName, direction,
                origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), width, height)
                .thenRun(() -> {});
        } else {
            String sqlStr = "INSERT INTO pending_portals (player_uuid, portal_name, direction, amplified_world, amplified_x, amplified_y, amplified_z, status, width, height) VALUES (?, ?, ?, ?, ?, ?, ?, 'pending', ?, ?)";
            return sql.update(DATASOURCE, sqlStr,
                playerUuid.toString(), portalName, direction,
                origin.getWorld().getName(), origin.getX(), origin.getY(), origin.getZ(), width, height)
                .thenRun(() -> {});
        }
    }

    /**
     * One pending row for this player where "other side" coords are set.
     * When isSurvival (we're Survival): row with amplified_x IS NOT NULL.
     * When !isSurvival (we're Amplified): row with survival_x IS NOT NULL.
     * Returns the coords where we should build the return portal (our side = other side's world name + x/z; y computed as surface).
     */
    public CompletableFuture<PendingPortalRow> getPendingPortalForPlayer(UUID playerUuid, boolean isSurvival) {
        String worldCol = isSurvival ? "amplified_world" : "survival_world";
        String xCol = isSurvival ? "amplified_x" : "survival_x";
        String yCol = isSurvival ? "amplified_y" : "survival_y";
        String zCol = isSurvival ? "amplified_z" : "survival_z";
        String readyCond = isSurvival ? "amplified_x IS NOT NULL" : "survival_x IS NOT NULL";
        String sqlStr = "SELECT id, portal_name, direction, " + worldCol + ", " + xCol + ", " + yCol + ", " + zCol + ", width, height" +
            " FROM pending_portals WHERE player_uuid = ? AND status = 'pending' AND " + readyCond +
            " ORDER BY id DESC LIMIT 1";
        return sql.query(DATASOURCE, sqlStr,
            rs -> {
                if (!rs.next()) return null;
                return new PendingPortalRow(
                    rs.getInt("id"),
                    rs.getString("portal_name"),
                    rs.getString("direction"),
                    rs.getString(worldCol),
                    rs.getDouble(xCol),
                    rs.getDouble(yCol),
                    rs.getDouble(zCol),
                    rs.getInt("width"),
                    rs.getInt("height"));
            },
            playerUuid.toString());
    }

    public record PendingPortalRow(int id, String portalName, String direction, String worldName, double frameX, double frameY, double frameZ, int width, int height) {}

    public CompletableFuture<Void> deletePendingById(int id) {
        return sql.update(DATASOURCE, "DELETE FROM pending_portals WHERE id = ?", id).thenRun(() -> {});
    }

    /** List all portal rows for a player (for /portals and for /deleteportals block clear + delete). */
    public CompletableFuture<List<PortalListingRow>> listPortalsForPlayer(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT portal_name, direction, survival_world, amplified_world FROM portals WHERE player_uuid = ?",
            rs -> {
                List<PortalListingRow> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new PortalListingRow(
                        rs.getString("portal_name"),
                        rs.getString("direction"),
                        rs.getString("survival_world"),
                        rs.getString("amplified_world")));
                }
                return list;
            },
            playerUuid.toString());
    }

    public record PortalListingRow(String portalName, String direction, String survivalWorld, String amplifiedWorld) {}

    /** Rows with all coords for clearing blocks (deleteportals). */
    public CompletableFuture<List<PortalCoordinatesRow>> listPortalCoordinatesForPlayer(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT survival_world, survival_x, survival_y, survival_z, amplified_world, amplified_x, amplified_y, amplified_z, width, height, direction FROM portals WHERE player_uuid = ?",
            rs -> {
                List<PortalCoordinatesRow> list = new ArrayList<>();
                while (rs.next()) {
                    list.add(new PortalCoordinatesRow(
                        rs.getString("survival_world"), rs.getDouble("survival_x"), rs.getDouble("survival_y"), rs.getDouble("survival_z"),
                        rs.getString("amplified_world"), rs.getDouble("amplified_x"), rs.getDouble("amplified_y"), rs.getDouble("amplified_z"),
                        rs.getInt("width"), rs.getInt("height"), rs.getString("direction")));
                }
                return list;
            },
            playerUuid.toString());
    }

    public record PortalCoordinatesRow(String survivalWorld, double sX, double sY, double sZ,
                                       String amplifiedWorld, double aX, double aY, double aZ,
                                       int width, int height, String direction) {}

    public CompletableFuture<Void> deletePortalsAndPendingByPlayer(UUID playerUuid) {
        return sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ?", playerUuid.toString())
            .thenCompose(ignored -> sql.update(DATASOURCE, "DELETE FROM portals WHERE player_uuid = ?", playerUuid.toString()))
            .thenCompose(ignored -> sql.update(DATASOURCE, "DELETE FROM pending_portals WHERE player_uuid = ?", playerUuid.toString()))
            .thenRun(() -> {});
    }

    /** Find portal_name at frame in pending_portals (this side coords). */
    public CompletableFuture<String> findPortalNameAtPendingFrame(UUID playerUuid, boolean isSurvival,
                                                                   String worldName, double x, double y, double z) {
        String worldCol = isSurvival ? "survival_world" : "amplified_world";
        String xCol = isSurvival ? "survival_x" : "amplified_x";
        String yCol = isSurvival ? "survival_y" : "amplified_y";
        String zCol = isSurvival ? "survival_z" : "amplified_z";
        String sqlStr = "SELECT portal_name FROM pending_portals WHERE player_uuid = ? AND " + worldCol + " = ? AND " + xCol + " = ? AND " + yCol + " = ? AND " + zCol + " = ? ORDER BY id DESC LIMIT 1";
        return sql.query(DATASOURCE, sqlStr,
            rs -> rs.next() ? rs.getString("portal_name") : null,
            playerUuid.toString(), worldName, x, y, z);
    }

    public CompletableFuture<Void> saveTransferEntities(UUID playerUuid, String portalName, String entityDataJson) {
        return sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ? AND portal_name = ?", playerUuid.toString(), portalName)
            .thenCompose(ignored -> {
                if (entityDataJson == null || entityDataJson.isEmpty()) return CompletableFuture.completedFuture(0);
                return sql.update(DATASOURCE, "INSERT INTO portal_transfer_entities (player_uuid, portal_name, entity_data, created_at) VALUES (?, ?, ?, ?)",
                    playerUuid.toString(), portalName, entityDataJson, System.currentTimeMillis());
            })
            .thenRun(() -> {});
    }

    public CompletableFuture<String> getAndClearTransferEntities(UUID playerUuid, String portalName) {
        return sql.query(DATASOURCE, "SELECT entity_data FROM portal_transfer_entities WHERE player_uuid = ? AND portal_name = ? LIMIT 1",
            rs -> rs.next() ? rs.getString("entity_data") : null,
            playerUuid.toString(), portalName)
            .thenCompose(data -> sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ? AND portal_name = ?", playerUuid.toString(), portalName)
                .thenCompose(ignored -> sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ?", playerUuid.toString()))
                .thenApply(ignored -> data));
    }

    public CompletableFuture<Void> clearAllTransferEntitiesForPlayer(UUID playerUuid) {
        return sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ?", playerUuid.toString())
            .thenRun(() -> {});
    }

    /** Resolve (player_uuid, portal_name) for a frame at the given location (portals then pending). For break listener. */
    public CompletableFuture<PortalFrameOwner> findPortalOwnerAtFrame(boolean isSurvival, String worldName, double x, double y, double z) {
        String worldCol = isSurvival ? "survival_world" : "amplified_world";
        String xCol = isSurvival ? "survival_x" : "amplified_x";
        String yCol = isSurvival ? "survival_y" : "amplified_y";
        String zCol = isSurvival ? "survival_z" : "amplified_z";
        String sqlPortals = "SELECT player_uuid, portal_name FROM portals WHERE " + worldCol + " = ? AND " + xCol + " = ? AND " + yCol + " = ? AND " + zCol + " = ? LIMIT 1";
        return sql.query(DATASOURCE, sqlPortals, rs -> {
                if (rs.next()) return new PortalFrameOwner(rs.getString("player_uuid"), rs.getString("portal_name"));
                return null;
            }, worldName, x, y, z)
            .thenCompose(owner -> {
                if (owner != null) return CompletableFuture.completedFuture(owner);
                String sqlPending = "SELECT player_uuid, portal_name FROM pending_portals WHERE " + worldCol + " = ? AND " + xCol + " = ? AND " + yCol + " = ? AND " + zCol + " = ? ORDER BY id DESC LIMIT 1";
                return sql.query(DATASOURCE, sqlPending, rs -> {
                    if (rs.next()) return new PortalFrameOwner(rs.getString("player_uuid"), rs.getString("portal_name"));
                    return null;
                }, worldName, x, y, z);
            });
    }

    public record PortalFrameOwner(String playerUuid, String portalName) {}

    public CompletableFuture<Void> deletePortalByOwnerAndName(String playerUuid, String portalName) {
        return sql.update(DATASOURCE, "DELETE FROM portal_transfer_entities WHERE player_uuid = ? AND portal_name = ?", playerUuid, portalName)
            .thenCompose(ignored -> sql.update(DATASOURCE, "DELETE FROM portals WHERE player_uuid = ? AND portal_name = ?", playerUuid, portalName))
            .thenCompose(ignored -> sql.update(DATASOURCE, "DELETE FROM pending_portals WHERE player_uuid = ? AND portal_name = ?", playerUuid, portalName))
            .thenRun(() -> {});
    }
}
