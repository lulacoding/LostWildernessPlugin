package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per tick: for each configured {@link WaypointZone}, players inside the horizontal radius who
 * have the Waypoints permission (or Amos compass when applicable) fire a BetonQuest arrival event.
 */
public final class WaypointArrivalListener {

    private static final long ARRIVAL_COOLDOWN_MS = 2500L;

    private final Plugin plugin;
    private final List<WaypointZone> zones;
    private final BetonQuestBridge betonQuest;
    /** Prevents double-firing before BQ clears permission/tags. Key: playerUuid:zoneId */
    private final Map<String, Long> arrivalCooldownUntil = new ConcurrentHashMap<>();
    private BukkitTask tickTask;

    public WaypointArrivalListener(Plugin plugin, List<WaypointZone> zones, BetonQuestBridge betonQuest) {
        this.plugin = plugin;
        this.zones = zones;
        this.betonQuest = betonQuest;
    }

    public void start() {
        tickTask = plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 60L, 15L);
    }

    public void stop() {
        if (tickTask != null) {
            tickTask.cancel();
            tickTask = null;
        }
        arrivalCooldownUntil.clear();
    }

    private void tick() {
        if (zones.isEmpty() || betonQuest == null) {
            return;
        }
        long now = System.currentTimeMillis();
        for (WaypointZone zone : zones) {
            World world = plugin.getServer().getWorld(zone.getWorldName());
            if (world == null) {
                continue;
            }
            double r = zone.getRadius();
            double rSq = r * r;
            double cx = zone.getBlockX() + 0.5;
            double cz = zone.getBlockZ() + 0.5;
            for (Player player : world.getPlayers()) {
                if (!player.isOnline()) {
                    continue;
                }
                UUID uuid = player.getUniqueId();
                String cooldownKey = uuid + ":" + zone.getId();
                Long until = arrivalCooldownUntil.get(cooldownKey);
                if (until != null && now < until) {
                    continue;
                }
                String skipTag = zone.getSkipIfPlayerHasTag();
                if (skipTag != null && betonQuest.hasTag(uuid, skipTag)) {
                    continue;
                }
                boolean hasPerm = player.hasPermission(zone.getWaypointPermission());
                boolean hasCompass = zone.isRemoveAmosCompass() && carriesAmosCompass(player);
                if (!hasPerm && !hasCompass) {
                    continue;
                }
                Location loc = player.getLocation();
                double dx = loc.getX() - cx;
                double dz = loc.getZ() - cz;
                if (dx * dx + dz * dz > rSq) {
                    continue;
                }
                if (zone.isRemoveAmosCompass()) {
                    ThornwellCompassItem.removeAllFrom(plugin, player);
                }
                betonQuest.fireEventForPlayer(player, zone.getBetonquestEvent());
                arrivalCooldownUntil.put(cooldownKey, now + ARRIVAL_COOLDOWN_MS);
            }
        }
    }

    private boolean carriesAmosCompass(Player player) {
        for (org.bukkit.inventory.ItemStack stack : player.getInventory().getContents()) {
            if (ThornwellCompassItem.isThornwellCompass(plugin, stack)) {
                return true;
            }
        }
        return ThornwellCompassItem.isThornwellCompass(plugin, player.getInventory().getItemInOffHand());
    }
}
