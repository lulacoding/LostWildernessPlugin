package com.lostwilderness.rpgcore.portals;

import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.function.Consumer;

/**
 * Portal frame validation, lighting, return portal building, and exit
 * clearance.
 * All DB work is delegated to PortalRepository (async); callbacks run on main
 * thread via scheduler.
 */
public final class PortalService {

    private final PortalRepository repo;
    private final PortalsConfig config;
    private final boolean isSurvival;
    private final SchedulerService scheduler;
    private final Plugin plugin;
    private final Material portalMaterial;

    public PortalService(PortalRepository repo, PortalsConfig config, boolean isSurvival,
            SchedulerService scheduler, Plugin plugin) {
        this.repo = repo;
        this.config = config;
        this.isSurvival = isSurvival;
        this.scheduler = scheduler;
        this.plugin = plugin;
        this.portalMaterial = config.getPortalBlock();
    }

    public int getMaxFrameWidth() {
        return config.getMaxFrameWidth();
    }

    public int getMaxFrameHeight() {
        return config.getMaxFrameHeight();
    }

    public Material getPortalMaterial() {
        return portalMaterial;
    }

    /**
     * Returns true if the block at origin is the lower corner of a valid crying
     * obsidian frame (any supported size).
     */
    public boolean isValidPortalFrame(Block origin, boolean alongX) {
        return findValidPortalFrameAtOrigin(origin, alongX) != null;
    }

    /**
     * Given a block that is part of a portal frame (crying obsidian or interior),
     * find the frame's lower-corner origin and axis.
     * Returns null if not found.
     */
    public FrameOrigin findFrameOriginContaining(Block block) {
        if (block.getType() != Material.CRYING_OBSIDIAN && block.getType() != portalMaterial
                && block.getType() != Material.AIR)
            return null;
        int maxBackX = Math.max(0, config.getMaxFrameWidth() - 1);
        int maxBackY = Math.max(0, config.getMaxFrameHeight() - 1);
        for (boolean alongX : new boolean[] { true, false }) {
            for (int dx = 0; dx <= maxBackX; dx++) {
                for (int dy = 0; dy <= maxBackY; dy++) {
                    Block origin = block.getRelative(alongX ? -dx : 0, -dy, alongX ? 0 : -dx);
                    PortalFrame frame = findValidPortalFrameAtOrigin(origin, alongX);
                    if (frame == null)
                        continue;
                    int width = frame.width();
                    int height = frame.height();
                    for (int y = 0; y < height; y++) {
                        for (int i = 0; i < width; i++) {
                            Block b = origin.getRelative(alongX ? i : 0, y, alongX ? 0 : i);
                            if (b.equals(block)) {
                                return new FrameOrigin(origin.getLocation(), alongX);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public record FrameOrigin(Location location, boolean alongX) {
    }

    /** Finds a valid frame dimensions at this origin, or null. */
    public PortalFrame findValidPortalFrameAtOrigin(Block origin, boolean alongX) {
        for (int h = config.getMinFrameHeight(); h <= config.getMaxFrameHeight(); h++) {
            for (int w = config.getMinFrameWidth(); w <= config.getMaxFrameWidth(); w++) {
                if (isValidPortalFrame(origin, alongX, w, h)) {
                    return new PortalFrame(w, h);
                }
            }
        }
        return null;
    }

    private boolean isValidPortalFrame(Block origin, boolean alongX, int width, int height) {
        if (width < 3 || height < 3)
            return false;
        for (int y = 0; y < height; y++) {
            for (int i = 0; i < width; i++) {
                Block b = origin.getRelative(alongX ? i : 0, y, alongX ? 0 : i);
                boolean edge = (y == 0 || y == height - 1 || i == 0 || i == width - 1);
                if (edge) {
                    if (b.getType() != Material.CRYING_OBSIDIAN)
                        return false;
                } else {
                    Material type = b.getType();
                    if (type != Material.AIR && type != portalMaterial && type != Material.FIRE)
                        return false;
                }
            }
        }
        return true;
    }

    public record PortalFrame(int width, int height) {
    }

    /** Fills interior of frame with configured portal material. */
    public void fillPortal(Block origin, boolean alongX, int width, int height) {
        for (int y = 1; y < height - 1; y++) {
            for (int i = 1; i < width - 1; i++) {
                origin.getRelative(alongX ? i : 0, y, alongX ? 0 : i).setType(portalMaterial);
            }
        }
    }

    /** Clears all portal blocks (frame and interior) and sets them to AIR. */
    public void clearPortalBlocks(Location frameOrigin, boolean alongX, int width, int height) {
        World world = frameOrigin.getWorld();
        if (world == null)
            return;
        Block origin = frameOrigin.getBlock();
        for (int y = 0; y < height; y++) {
            for (int i = 0; i < width; i++) {
                Block b = origin.getRelative(alongX ? i : 0, y, alongX ? 0 : i);
                if (b.getType() == Material.CRYING_OBSIDIAN || b.getType() == portalMaterial) {
                    b.setType(Material.AIR);
                }
            }
        }
    }

    /**
     * Try to light a portal at the clicked location. Runs cap check async, then
     * finds frame and fills on main thread,
     * then saves to DB async and calls onResult on main thread.
     */
    public void tryToLightPortal(Location clickLoc, Player player, Consumer<Boolean> onResult) {
        UUID uuid = player.getUniqueId();
        repo.getPlayerPortalCount(uuid).thenAccept(count -> scheduler.runSync(() -> {
            if (count >= config.getMaxPairs()) {
                player.sendMessage("§cYou can only create " + config.getMaxPairs() + " portal pairs.");
                onResult.accept(false);
                return;
            }
            Block frameOrigin = null;
            boolean alongX = false;
            PortalFrame frame = null;
            int maxBackX = Math.max(0, config.getMaxFrameWidth() - 1);
            int maxBackY = Math.max(0, config.getMaxFrameHeight() - 1);

            plugin.getLogger().info("Portal lighting attempt at " + clickLoc + " by " + player.getName());

            outer: for (boolean axis : new boolean[] { true, false }) {
                for (int dx = -maxBackX; dx <= 0; dx++) {
                    for (int dy = -maxBackY; dy <= 0; dy++) {
                        Block cand = clickLoc.getBlock().getRelative(axis ? dx : 0, dy, axis ? 0 : dx);
                        PortalFrame f = findValidPortalFrameAtOrigin(cand, axis);
                        if (f != null) {
                            frameOrigin = cand;
                            alongX = axis;
                            frame = f;
                            break outer;
                        }
                    }
                }
            }
            if (frameOrigin == null) {
                plugin.getLogger().info("No valid portal frame found around " + clickLoc);
                onResult.accept(false);
                return;
            }
            plugin.getLogger().info("Valid frame found! Origin: " + frameOrigin.getLocation() + " AxisX: " + alongX
                    + " Size: " + frame.width() + "x" + frame.height());
            fillPortal(frameOrigin, alongX, frame.width(), frame.height());
            String dir = alongX ? "x" : "z";
            String name = "Portal_" + System.currentTimeMillis();
            Location originLoc = frameOrigin.getLocation();
            int width = frame.width();
            int height = frame.height();
            repo.saveLinkedPortal(uuid, name, dir, originLoc, originLoc, width, height)
                    .thenCompose(
                            ignored -> repo.addPendingPortal(uuid, name, dir, originLoc, isSurvival, width, height))
                    .thenRun(() -> scheduler.runSync(() -> {
                        player.sendMessage("§aPortal linked! Step through to connect.");
                        onResult.accept(true);
                    }));
        }));
    }

    /**
     * Build return portal frame and fill with portal material at the given origin. Does
     * not teleport or update DB.
     */
    public void buildReturnPortalFrame(Location frameOrigin, boolean alongX, int width, int height) {
        World world = frameOrigin.getWorld();
        if (world == null)
            return;
        Block origin = frameOrigin.getBlock();
        for (int y = 0; y < height; y++) {
            for (int i = 0; i < width; i++) {
                Block b = origin.getRelative(alongX ? i : 0, y, alongX ? 0 : i);
                boolean edge = (y == 0 || y == height - 1 || i == 0 || i == width - 1);
                b.setType(edge ? Material.CRYING_OBSIDIAN : portalMaterial);
            }
        }
    }

    public static Location computeExitSpot(Location frameOrigin, boolean alongX, int width, int height) {
        int centerOffset = Math.max(1, (width - 1) / 2);
        if (alongX) {
            return frameOrigin.clone().add(centerOffset, 1, -1);
        }
        return frameOrigin.clone().add(-1, 1, centerOffset);
    }

    public void prepareSafeExit(Location exit, Entity rootVehicle, int frameWidth, int frameHeight) {
        if (exit == null || exit.getWorld() == null)
            return;
        PortalsConfig.ExitClearance cfg = config.getExitClearance();
        if (!cfg.enabled())
            return;
        boolean large = cfg.largeEnabled() && isLargeMount(rootVehicle);
        int radius = large ? cfg.largeRadius() : cfg.radius();
        int height = large ? cfg.largeHeight() : cfg.height();
        scheduler.runSync(() -> clearArea(exit, radius, height));
    }

    private void clearArea(Location exit, int radius, int height) {
        World world = exit.getWorld();
        if (world == null)
            return;
        int baseX = exit.getBlockX();
        int baseY = exit.getBlockY();
        int baseZ = exit.getBlockZ();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = 0; dy < height; dy++) {
                    Block b = world.getBlockAt(baseX + dx, baseY + dy, baseZ + dz);
                    Material t = b.getType();
                    if (t == Material.CRYING_OBSIDIAN || t == portalMaterial)
                        continue;
                    if (t != Material.AIR)
                        b.setType(Material.AIR);
                }
            }
        }
    }

    private static boolean isLargeMount(Entity rootVehicle) {
        if (rootVehicle == null)
            return false;
        return rootVehicle.getType().name().contains("GHAST");
    }

    public PortalRepository getRepo() {
        return repo;
    }

    public PortalsConfig getConfig() {
        return config;
    }

    public boolean isSurvival() {
        return isSurvival;
    }
}
