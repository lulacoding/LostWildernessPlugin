package com.lostwilderness.rpgcore.portals;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Loads config/portals.yml (frame rules, target-server, exit-clearance).
 * Server role (survival vs amplified) comes from core.yml via ConfigService.
 */
public final class PortalsConfig {

    private final String targetServer;
    private final int maxPairs;
    private final int minFrameWidth;
    private final int minFrameHeight;
    private final int maxFrameWidth;
    private final int maxFrameHeight;
    private final Material portalBlock;
    private final ExitClearance exitClearance;

    public PortalsConfig(Plugin plugin, boolean isSurvival) {
        File configFolder = new File(plugin.getDataFolder(), "config");
        configFolder.mkdirs();
        File file = new File(configFolder, "portals.yml");
        if (!file.exists()) {
            try (InputStream in = plugin.getResource("config/portals.yml")) {
                if (in != null)
                    Files.copy(in, file.toPath());
            } catch (Exception e) {
                plugin.getLogger().warning("Could not save default portals.yml: " + e.getMessage());
            }
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        String defaultTarget = isSurvival ? "amplified-1" : "survival-1";
        this.targetServer = cfg.getString("target-server", defaultTarget);
        this.maxPairs = cfg.getInt("max-pairs", 5);
        this.minFrameWidth = cfg.getInt("frame.min-width", 4);
        this.minFrameHeight = cfg.getInt("frame.min-height", 5);
        this.maxFrameWidth = cfg.getInt("frame.max-width", 7);
        this.maxFrameHeight = cfg.getInt("frame.max-height", 7);
        String portalBlockName = cfg.getString("portal-block", "NETHER_PORTAL");
        Material parsedPortalBlock = Material.matchMaterial(portalBlockName == null ? "NETHER_PORTAL" : portalBlockName);
        this.portalBlock = parsedPortalBlock != null ? parsedPortalBlock : Material.END_GATEWAY;

        boolean clearanceEnabled = cfg.getBoolean("exit-clearance.enabled", true);
        int radius = cfg.getInt("exit-clearance.radius", 1);
        int height = cfg.getInt("exit-clearance.height", 3);
        boolean largeEnabled = cfg.getBoolean("exit-clearance.large-enabled", true);
        int largeRadius = cfg.getInt("exit-clearance.large-radius", 3);
        int largeHeight = cfg.getInt("exit-clearance.large-height", 6);
        this.exitClearance = new ExitClearance(clearanceEnabled, radius, height, largeEnabled, largeRadius,
                largeHeight);
    }

    public String getTargetServer() {
        return targetServer;
    }

    public int getMaxPairs() {
        return maxPairs;
    }

    public int getMinFrameWidth() {
        return minFrameWidth;
    }

    public int getMinFrameHeight() {
        return minFrameHeight;
    }

    public int getMaxFrameWidth() {
        return maxFrameWidth;
    }

    public int getMaxFrameHeight() {
        return maxFrameHeight;
    }

    public Material getPortalBlock() {
        return portalBlock;
    }

    public ExitClearance getExitClearance() {
        return exitClearance;
    }

    public record ExitClearance(boolean enabled, int radius, int height,
            boolean largeEnabled, int largeRadius, int largeHeight) {
    }
}
