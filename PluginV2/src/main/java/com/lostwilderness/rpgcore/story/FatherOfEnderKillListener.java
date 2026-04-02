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
 * Detects the death of the Father of Ender MythicMobs boss.
 *
 * Detection: EntityDeathEvent where entity is an EnderDragon with a custom name
 * containing "Father of Ender". MythicMobs sets the custom name on the entity.
 *
 * On kill:
 * - Unlocks MILESTONE_FATHER_OF_ENDER for all present players (128-block radius)
 * - Sets FLAG_HEAVENS_GATE_PENDING (Heaven's Gate becomes buildable)
 * - Awards Celestial + Epochian rep
 * - Server broadcast
 */
public final class FatherOfEnderKillListener implements Listener {

    /** Must match the MythicMobs mob name (display name) configured in mobs/father_of_ender.yml */
    static final String FATHER_OF_ENDER_NAME = "Father of Ender";

    private static final int PRESENT_RADIUS = 128;

    private final ProgressionService progressionService;
    private final ReputationService reputationService;
    private final Plugin plugin;

    public FatherOfEnderKillListener(ProgressionService progressionService,
                                     ReputationService reputationService,
                                     Plugin plugin) {
        this.progressionService = progressionService;
        this.reputationService = reputationService;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onFatherOfEnderDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;

        String name = dragon.getCustomName();
        if (name == null || !name.contains(FATHER_OF_ENDER_NAME)) return;

        Set<UUID> present = getPresentPlayerUuids(dragon.getLocation());
        Player killer = dragon.getKiller();
        if (killer != null) present.add(killer.getUniqueId());

        if (present.isEmpty()) return;

        plugin.getLogger().info("[story] Father of Ender slain. Crediting " + present.size() + " players.");

        for (UUID uuid : present) {
            boolean alreadyDone = progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_FATHER_OF_ENDER);
            progressionService.unlock(uuid, AchievementKey.MILESTONE_FATHER_OF_ENDER);
            progressionService.unlock(uuid, AchievementKey.FLAG_HEAVENS_GATE_PENDING);

            if (!alreadyDone) {
                reputationService.addReputation(uuid, Faction.CELESTIAL, 500);
                reputationService.addReputation(uuid, Faction.EPOCHIANS, 300);
            }
        }

        String killerName = killer != null ? killer.getName() : "a brave hero";
        Bukkit.broadcastMessage(
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "⚡ " + killerName +
            ChatColor.LIGHT_PURPLE + " has slain the Father of Ender! " +
            ChatColor.GRAY + "The firmament cracks open...");

        for (Player p : Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_DEATH, 0.5f, 0.6f);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (UUID uuid : present) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;
                p.sendTitle(
                    ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "Father Slain",
                    ChatColor.LIGHT_PURPLE + "Heaven's Gate may now be built at Y=602.",
                    10, 80, 20
                );
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
