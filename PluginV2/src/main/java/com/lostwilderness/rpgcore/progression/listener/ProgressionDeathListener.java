package com.lostwilderness.rpgcore.progression.listener;

import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import com.lostwilderness.rpgcore.progression.AchievementKey;
import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;

import java.util.UUID;
import java.util.logging.Level;

/**
 * On death: increment death_count; unlock first_death and deaths_10 milestones.
 */
public final class ProgressionDeathListener implements Listener {

    private final ProgressionService progressionService;
    private final SchedulerService scheduler;
    private final Plugin plugin;

    public ProgressionDeathListener(ProgressionService progressionService, SchedulerService scheduler, Plugin plugin) {
        this.progressionService = progressionService;
        this.scheduler = scheduler;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        scheduler.runAsync(() -> {
            try {
                long count = progressionService.incrementCounter(uuid, AchievementKey.COUNTER_DEATH_COUNT);
                if (count >= 1 && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_FIRST_DEATH)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_FIRST_DEATH);
                }
                if (count >= 10 && !progressionService.hasUnlocked(uuid, AchievementKey.MILESTONE_DEATHS_10)) {
                    progressionService.unlock(uuid, AchievementKey.MILESTONE_DEATHS_10);
                }
            } catch (Throwable t) {
                plugin.getLogger().log(Level.WARNING, "Progression death failed for " + uuid, t);
            }
        });
    }
}
