package com.lostwilderness.rpgcore.calendar;

import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

final class CalendarServiceV2Impl implements CalendarServiceV2 {

    private static final long FOLLOWER_POLL_TICKS = 200L; // ~10s

    private final Plugin plugin;
    private final CalendarRepository repo;
    private final SchedulerService scheduler;
    private final boolean isCalendarLeader;
    private final AtomicReference<CalendarSnapshot> snapshotRef = new AtomicReference<>();

    CalendarServiceV2Impl(Plugin plugin, CalendarRepository repo, SchedulerService scheduler, boolean isCalendarLeader) {
        this.plugin = plugin;
        this.repo = repo;
        this.scheduler = scheduler;
        this.isCalendarLeader = isCalendarLeader;
    }

    CompletableFuture<Void> load() {
        return repo.loadStateOrDefault().thenCompose(snapshot -> {
            snapshotRef.set(snapshot);
            return repo.saveState(snapshot); // ensure row exists
        });
    }

    void startDayChangeTask() {
        if (isCalendarLeader) {
            // Leader (survival): advance calendar when MC day increases, save to DB
            scheduler.runSyncRepeating(() -> {
                try {
                    World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
                    if (world == null) return;
                    long mcDay = world.getFullTime() / 24000L;
                    CalendarSnapshot current = snapshotRef.get();
                    if (mcDay > current.mcDay()) {
                        advanceToNextDayInternal(mcDay);
                    }
                } catch (Throwable t) {
                    plugin.getLogger().warning("Calendar day-change task failed: " + t.getMessage());
                }
            }, FOLLOWER_POLL_TICKS, FOLLOWER_POLL_TICKS);
        } else {
            // Follower (e.g. amplified): poll DB and fire CalendarDayAdvancedEvent when day increases
            scheduler.runSyncRepeating(() -> {
                repo.loadStateOrDefault().thenAccept(loaded -> {
                    scheduler.runSync(() -> {
                        CalendarSnapshot current = snapshotRef.get();
                        if (loaded.dayCount() > current.dayCount()) {
                            snapshotRef.set(loaded);
                            Bukkit.getPluginManager().callEvent(new CalendarDayAdvancedEvent(loaded, current));
                        }
                    });
                }).exceptionally(t -> {
                    plugin.getLogger().warning("Calendar follower poll failed: " + t.getMessage());
                    return null;
                });
            }, FOLLOWER_POLL_TICKS, FOLLOWER_POLL_TICKS);
        }
    }

    @Override
    public CalendarSnapshot getCurrentSnapshot() {
        return snapshotRef.get();
    }

    @Override
    public LocalDate getPlayerJoinDate(UUID playerUuid) {
        CalendarSnapshot s = snapshotRef.get();
        try {
            return repo.getPlayerJoinDate(playerUuid, s.date()).join();
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to load join date for " + playerUuid + ": " + t.getMessage());
            return s.date();
        }
    }

    @Override
    public void recordFirstJoinIfAbsent(UUID playerUuid) {
        CalendarSnapshot s = snapshotRef.get();
        repo.recordFirstJoinIfAbsent(playerUuid, s.date())
            .exceptionally(t -> {
                plugin.getLogger().warning("Failed to record join date for " + playerUuid + ": " + t.getMessage());
                return null;
            });
    }

    @Override
    public void advanceToNextDay() {
        World world = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        long mcDay = world != null ? world.getFullTime() / 24000L + 1 : snapshotRef.get().mcDay() + 1;
        advanceToNextDayInternal(mcDay);
    }

    private void advanceToNextDayInternal(long newMcDay) {
        CalendarSnapshot current = snapshotRef.get();
        long newEpoch = current.dayCount() + 1;
        LocalDate newDate = current.date().plusDays(1);
        Season newSeason = computeSeason(newDate);
        CalendarSnapshot updated = new CalendarSnapshot(newDate, newEpoch, newSeason, newMcDay);
        snapshotRef.set(updated);
        repo.saveState(updated)
            .exceptionally(t -> {
                plugin.getLogger().warning("Failed to save calendar state: " + t.getMessage());
                return null;
            });
        Bukkit.getPluginManager().callEvent(new CalendarDayAdvancedEvent(updated, current));
    }

    @Override
    public void resetCalendar() {
        CalendarSnapshot reset = new CalendarSnapshot(LocalDate.of(1, 1, 1), 0L, Season.SPRING, 0L);
        snapshotRef.set(reset);
        repo.saveState(reset)
            .exceptionally(t -> {
                plugin.getLogger().warning("Failed to save reset calendar state: " + t.getMessage());
                return null;
            });
    }

    private static Season computeSeason(LocalDate date) {
        int month = date.getMonthValue();
        return switch (month) {
            case 3, 4, 5 -> Season.SPRING;
            case 6, 7, 8 -> Season.SUMMER;
            case 9, 10, 11 -> Season.AUTUMN;
            default -> Season.WINTER;
        };
    }
}

