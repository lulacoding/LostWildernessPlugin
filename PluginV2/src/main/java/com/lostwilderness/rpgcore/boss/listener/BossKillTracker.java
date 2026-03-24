package com.lostwilderness.rpgcore.boss.listener;

import com.lostwilderness.rpgcore.reputation.Faction;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import com.lostwilderness.rpgcore.boss.BossKillRepository;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.core.ModuleContext;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

public class BossKillTracker implements Listener {

    private static final int PRESENT_RADIUS_BLOCKS = 64;

    private final JavaPlugin plugin;
    private final BossKillRepository repository;
    private final ModuleContext context;

    public BossKillTracker(JavaPlugin plugin, BossKillRepository repository, ModuleContext context) {
        this.plugin = plugin;
        this.repository = repository;
        this.context = context;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWitherDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Wither w)) return;

        boolean isDevoider = w.hasMetadata("devoider");
        boolean isDiablo = w.hasMetadata("diablo");

        Player killer = w.getKiller();
        if (!isDevoider && !isDiablo) {
            recordKill(w.getLocation(), true, killer);
            return;
        }
        if (isDevoider) {
            recordKill(w.getLocation(), false, killer);
            return;
        }
        
        if (isDiablo) {
            ProgressionService ps = context.getServiceRegistry().get(ProgressionService.class);
            if (ps == null) return;

            Set<UUID> presentUuids = getPresentPlayerUuids(w.getLocation());
            if (killer != null) presentUuids.add(killer.getUniqueId());

            for (UUID uuid : presentUuids) {
                ps.unlock(uuid, AchievementKey.MILESTONE_EL_DIABLO);
                
                // Reputation: El Diablo death yields CELESTIAL/EPOCHIANS favour
                ReputationService repService = context.getServiceRegistry().get(ReputationService.class);
                if (repService != null) {
                    repService.addReputation(uuid, Faction.CELESTIAL, 500);
                    repService.addReputation(uuid, Faction.EPOCHIANS, 200);
                    repService.addReputation(uuid, Faction.CORRUPTED, -500);
                }
            }
        }
    }

    private void recordKill(Location deathLoc, boolean witherKill, Player killer) {
        Set<UUID> presentUuids = getPresentPlayerUuids(deathLoc);

        if (killer != null) {
            presentUuids.add(killer.getUniqueId());

            // Party integration: Add party members within 64 blocks
            com.lostwilderness.rpgcore.party.PartyService partyService =
                context.getServiceRegistry().get(com.lostwilderness.rpgcore.party.PartyService.class);
            if (partyService != null) {
                Set<UUID> partyMembers = partyService.getPartyMembers(killer.getUniqueId());
                for (UUID memberId : partyMembers) {
                    if (memberId.equals(killer.getUniqueId())) continue; // Already added

                    Player member = org.bukkit.Bukkit.getPlayer(memberId);
                    if (member == null || !member.isOnline()) continue;

                    Location memberLoc = member.getLocation();
                    if (memberLoc.getWorld() != deathLoc.getWorld()) continue;

                    // Check if within 64 blocks
                    if (memberLoc.distanceSquared(deathLoc) <= PRESENT_RADIUS_BLOCKS * PRESENT_RADIUS_BLOCKS) {
                        presentUuids.add(memberId);
                    }
                }
            }
        }

        repository.incrementPlayerWitherKills(killer != null ? killer.getUniqueId() : null) // Handle null killer if needed
            .thenRun(() -> {
                // Record clan kills
                ClanService clanService = context.getServiceRegistry().get(ClanService.class);
                if (clanService != null) {
                    Set<UUID> clanIds = new HashSet<>();
                    for (UUID playerUuid : presentUuids) {
                        UUID clanId = clanService.getClanOfPlayer(playerUuid);
                        if (clanId != null) clanIds.add(clanId);
                    }
                    for (UUID clanId : clanIds) {
                        if (witherKill) {
                            repository.incrementClanWitherKills(clanId).thenRun(() ->
                                checkWitherMilestone(clanId, presentUuids));
                        } else {
                            repository.incrementClanDevoiderKills(clanId);
                        }
                    }
                }

                // Progression integration
                if (!witherKill && killer != null) {
                    ProgressionService ps = context.getServiceRegistry().get(ProgressionService.class);
                    if (ps != null) {
                        repository.getPlayerDevoiderKills(killer.getUniqueId()).thenAccept(kills -> {
                            String key = switch (Math.min(Math.max(kills, 1), 6)) {
                                case 1 -> AchievementKey.MILESTONE_DEVOIDER_1;
                                case 2 -> AchievementKey.MILESTONE_DEVOIDER_2;
                                case 3 -> AchievementKey.MILESTONE_DEVOIDER_3;
                                case 4 -> AchievementKey.MILESTONE_DEVOIDER_4;
                                case 5 -> AchievementKey.MILESTONE_DEVOIDER_5;
                                default -> AchievementKey.MILESTONE_DEVOIDER_6;
                            };
                            ps.unlock(killer.getUniqueId(), key);
                        });
                    }
                    
                    // Reputation: Devoider kill = favour with EPOCHIANS/WILDLANDS, enmity with CORRUPTED
                    ReputationService repService = context.getServiceRegistry().get(ReputationService.class);
                    if (repService != null) {
                        repService.addReputation(killer.getUniqueId(), Faction.EPOCHIANS, 200);
                        repService.addReputation(killer.getUniqueId(), Faction.WILDLANDS, 100);
                        repService.addReputation(killer.getUniqueId(), Faction.CORRUPTED, -300);
                    }
                }
            }).exceptionally(ex -> {
                plugin.getLogger().log(Level.SEVERE, "Error recording boss kill", ex);
                return null;
            });
    }

    private void checkWitherMilestone(UUID clanId, Set<UUID> presentUuids) {
        repository.getClanWitherKills(clanId).thenAccept(count -> {
            if (count != 6) return; // Only fire exactly at 6

            ProgressionService ps = context.getServiceRegistry().get(ProgressionService.class);
            if (ps == null) return;

            // Unlock for all present players
            for (UUID uuid : presentUuids) {
                ps.unlock(uuid, com.lostwilderness.rpgcore.progression.AchievementKey.MILESTONE_WITHER_6);
            }

            // Server-wide broadcast on main thread
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                ClanService cs = context.getServiceRegistry().get(ClanService.class);
                com.lostwilderness.rpgcore.clans.model.Clan clan = cs != null ? cs.getClanById(clanId) : null;
                String clanName = clan != null ? clan.getName() : "A clan";
                org.bukkit.Bukkit.broadcastMessage(
                    org.bukkit.ChatColor.DARK_PURPLE + "" + org.bukkit.ChatColor.BOLD +
                    "⚔ " + clanName + org.bukkit.ChatColor.LIGHT_PURPLE +
                    " has broken the Withering Council's threshold!" +
                    org.bukkit.ChatColor.GRAY + " The Devoid stirs...");
            });
        });
    }

    private Set<UUID> getPresentPlayerUuids(Location center) {
        Set<UUID> out = new HashSet<>();
        World world = center.getWorld();
        if (world == null) return out;
        for (Player p : world.getPlayers()) {
            if (p.getLocation().getWorld() != world) continue;
            if (p.getLocation().distanceSquared(center) <= PRESENT_RADIUS_BLOCKS * PRESENT_RADIUS_BLOCKS) {
                out.add(p.getUniqueId());
            }
        }
        return out;
    }
}
