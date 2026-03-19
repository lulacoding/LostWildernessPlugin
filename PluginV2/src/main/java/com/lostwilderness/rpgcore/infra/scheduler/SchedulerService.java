package com.lostwilderness.rpgcore.infra.scheduler;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

/**
 * Wraps the Bukkit scheduler so all heavy work can be run async.
 */
public final class SchedulerService {

    private final Plugin plugin;
    private final BukkitScheduler scheduler;

    public SchedulerService(Plugin plugin) {
        this.plugin = plugin;
        this.scheduler = plugin.getServer().getScheduler();
    }

    public void runSync(Runnable task) {
        scheduler.runTask(plugin, task);
    }

    public void runAsync(Runnable task) {
        scheduler.runTaskAsynchronously(plugin, task);
    }

    public void runSyncDelayed(Runnable task, long delayTicks) {
        scheduler.runTaskLater(plugin, task, delayTicks);
    }

    public void runAsyncDelayed(Runnable task, long delayTicks) {
        scheduler.runTaskLaterAsynchronously(plugin, task, delayTicks);
    }

    public void runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimerAsynchronously(plugin, task, delayTicks, periodTicks);
    }

    /** Repeating task on the main thread (e.g. day-change check, event tick). */
    public void runSyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        scheduler.runTaskTimer(plugin, task, delayTicks, periodTicks);
    }
}
