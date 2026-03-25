package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import com.lostwilderness.rpgcore.reputation.Faction;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Detects Ender Dragon death and wires it into story progression.
 *
 * On kill:
 * - Unlocks MILESTONE_ENDER_DRAGON for all present players
 * - Sets FLAG_REDEEMER_PENDING (The Redeemer will appear next Easter)
 * - Awards Celestial + Epochian rep
 * - Broadcasts server-wide announcement
 */
public final class EnderDragonKillListener implements Listener {

    private static final int PRESENT_RADIUS = 128;

    private final ProgressionService progressionService;
    private final ReputationService reputationService;
    private final Plugin plugin;

    public EnderDragonKillListener(ProgressionService progressionService,
                                   ReputationService reputationService,
                                   Plugin plugin) {
        this.progressionService = progressionService;
        this.reputationService = reputationService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnderDragonDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        Set<UUID> present = getPresentPlayerUuids(dragon.getLocation());
        Player killer = dragon.getKiller();
        if (killer != null) present.add(killer.getUniqueId());

        if (present.isEmpty()) return;

        plugin.getLogger().info("[story] Ender Dragon slain. Crediting " + present.size() + " players.");

        for (UUID uuid : present) {
            boolean alreadyDone = progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_ENDER_DRAGON);
            progressionService.unlock(uuid, AchievementKey.MILESTONE_ENDER_DRAGON);
            // Set Redeemer pending flag — The Redeemer will appear next Easter
            progressionService.unlock(uuid, AchievementKey.FLAG_REDEEMER_PENDING);

            if (!alreadyDone) {
                reputationService.addReputation(uuid, Faction.CELESTIAL, 300);
                reputationService.addReputation(uuid, Faction.EPOCHIANS, 150);
            }
        }

        // Broadcast
        String killerName = killer != null ? killer.getName() : "a brave hero";
        String broadcast = ChatColor.GOLD + "" + ChatColor.BOLD + "⚡ " + killerName +
            ChatColor.YELLOW + " has slain the Ender Dragon! " +
            ChatColor.GRAY + "The world trembles...";
        Bukkit.broadcastMessage(broadcast);

        // Play ominous sound for all online players
        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 0.8f);
        }

        // Notify involved players specifically
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : present) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                if (progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_ENDER_DRAGON)) {
                    p.sendTitle(
                        ChatColor.GOLD + "" + ChatColor.BOLD + "Dragon Slain",
                        ChatColor.YELLOW + "The Redeemer will come on the next Easter...",
                        10, 80, 20
                    );
                }
            }
        }, 60L);
    }

    private Set<UUID> getPresentPlayerUuids(Location center) {
        Set<UUID> out = new HashSet<>();
        World world = center.getWorld();
        if (world == null) return out;
        for (Player p : world.getPlayers()) {
            if (p.getLocation().distanceSquared(center) <= (double) PRESENT_RADIUS * PRESENT_RADIUS) {
                out.add(p.getUniqueId());
            }
        }
        return out;
    }
}
