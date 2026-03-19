package com.lostwilderness.rpgcore.zodiac.listener;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.zodiac.ZodiacProfile;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Listener for player join/quit events.
 * Assigns zodiac on first join and manages profile lifecycle.
 */
public final class ZodiacJoinListener implements Listener {

    private final ZodiacService zodiacService;
    private final CalendarServiceV2 calendarService;

    public ZodiacJoinListener(ZodiacService zodiacService, CalendarServiceV2 calendarService) {
        this.zodiacService = zodiacService;
        this.calendarService = calendarService;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        // Try to load from cache/DB
        Optional<ZodiacProfile> existing = zodiacService.getZodiacProfile(uuid);

        if (existing.isEmpty()) {
            // First join - assign zodiac asynchronously
            long epochDay = calendarService.getCurrentSnapshot().dayCount();

            CompletableFuture.runAsync(() -> {
                zodiacService.assignZodiac(uuid, epochDay);
            });
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        // Could invalidate cache to save memory, but we'll keep it for now
        // zodiacService.invalidateCache(event.getPlayer().getUniqueId());
    }
}
