package com.lostwilderness.rpgcore.story;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.repo.ClanRepository;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

/**
 * Tracks standard Wither kills per clan (or per solo player) toward the Withering Council.
 *
 * Rules:
 * - Only standard Withers count. Devoiders (custom MythicMobs boss) are excluded by name check.
 * - If killer is in a clan: increments clan wither_kills in DB.
 * - If killer has no clan: increments personal COUNTER_WITHER_KILLS_SOLO via ProgressionService.
 * - Milestones:
 *     MILESTONE_WITHER_6  → solo threshold (equivalent to one clan's worth) or clan reaches 6
 *     MILESTONE_WITHER_36 → clan threshold, unlocks Devoid access
 *
 * Both Survival and Amplified worlds count — no world filter here.
 */
public final class WitherKillListener implements Listener {

    /** MythicMobs custom name prefix for the Devoider boss — kills with this name don't count. */
    private static final String DEVOIDER_NAME_PREFIX = "Devoider";

    private static final int CLAN_THRESHOLD = 36;
    private static final int SOLO_THRESHOLD = 6;

    private final ProgressionService progressionService;
    private final ClanService clanService;
    private final ClanRepository clanRepository;
    private final Plugin plugin;

    public WitherKillListener(ProgressionService progressionService,
                              ClanService clanService,
                              ClanRepository clanRepository,
                              Plugin plugin) {
        this.progressionService = progressionService;
        this.clanService = clanService;
        this.clanRepository = clanRepository;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWitherDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wither wither)) return;

        // Exclude Devoider boss — check custom name
        String customName = wither.getCustomName();
        if (customName != null && customName.contains(DEVOIDER_NAME_PREFIX)) return;

        Player killer = wither.getKiller();
        if (killer == null) return; // Unattributed kill (no player dealt final blow)

        UUID killerUuid = killer.getUniqueId();
        UUID clanId = clanService.getClanOfPlayer(killerUuid);

        if (clanId != null) {
            handleClanKill(killer, clanId);
        } else {
            handleSoloKill(killer);
        }
    }

    private void handleClanKill(Player killer, UUID clanId) {
        clanRepository.incrementWitherKills(clanId).thenAccept(newCount -> {
            plugin.getLogger().info("[story] Clan " + clanId + " wither kill count: " + newCount);

            Bukkit.getScheduler().runTask(plugin, () -> {
                if (!killer.isOnline()) return;

                killer.sendMessage(ChatColor.GRAY + "Wither slain. Clan kill count: " +
                    ChatColor.YELLOW + newCount + ChatColor.GRAY + "/" + CLAN_THRESHOLD);

                if (newCount == CLAN_THRESHOLD) {
                    onClanThresholdReached(clanId);
                } else if (newCount == SOLO_THRESHOLD) {
                    // Partial milestone for all clan members online
                    notifyClanOnlineMembers(clanId,
                        ChatColor.GOLD + "☠ Your clan has slain 6 Withers. The Withering Council grows...");
                }
            });
        });
    }

    private void handleSoloKill(Player killer) {
        UUID uuid = killer.getUniqueId();
        long newCount = progressionService.incrementCounter(uuid, AchievementKey.COUNTER_WITHER_KILLS_SOLO);

        plugin.getLogger().info("[story] Solo player " + killer.getName() + " wither kill count: " + newCount);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!killer.isOnline()) return;

            killer.sendMessage(ChatColor.GRAY + "Wither slain. Solo kill count: " +
                ChatColor.YELLOW + newCount + ChatColor.GRAY + "/" + SOLO_THRESHOLD);

            if (newCount >= SOLO_THRESHOLD && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_WITHER_6)) {
                progressionService.unlock(uuid, AchievementKey.MILESTONE_WITHER_6);
                killer.sendTitle(
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "Withering Complete",
                    ChatColor.GRAY + "The Devoid stirs... seek the portal.",
                    10, 80, 20
                );
                killer.playSound(killer.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.8f, 0.8f);
                Bukkit.broadcastMessage(ChatColor.DARK_GRAY + "" + ChatColor.ITALIC +
                    "A lone warrior has completed the Withering Council...");
            }
        });
    }

    private void onClanThresholdReached(UUID clanId) {
        // Unlock MILESTONE_WITHER_36 for all online clan members
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (clanId.equals(clanService.getClanOfPlayer(p.getUniqueId()))) {
                progressionService.unlock(p.getUniqueId(), AchievementKey.MILESTONE_WITHER_36);
                p.sendTitle(
                    ChatColor.DARK_RED + "" + ChatColor.BOLD + "The Devoid Opens",
                    ChatColor.GRAY + "36 Withers slain. The darkness beyond awaits.",
                    10, 80, 20
                );
                p.playSound(p.getLocation(), Sound.ENTITY_WITHER_DEATH, 1.0f, 0.5f);
            }
        }
        Bukkit.broadcastMessage(
            ChatColor.DARK_PURPLE + "" + ChatColor.BOLD + "⚡ The Withering Council is complete! " +
            ChatColor.GRAY + "The Devoid has been unsealed.");
    }

    private void notifyClanOnlineMembers(UUID clanId, String message) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (clanId.equals(clanService.getClanOfPlayer(p.getUniqueId()))) {
                p.sendMessage(message);
            }
        }
    }
}
