package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.EventContext;
import com.lostwilderness.rpgcore.events.SeasonalEvent;
import org.bukkit.plugin.Plugin;

/**
 * Minimal event for testing: never auto-starts (canStartToday = false), can be force-started via /event start test.
 */
public final class TestEvent implements SeasonalEvent {

    private final Plugin plugin;

    public TestEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getId() {
        return "test";
    }

    @Override
    public String getDisplayName() {
        return "Test Event";
    }

    @Override
    public boolean isSeasonAllowed(CalendarServiceV2.Season season) {
        return true;
    }

    @Override
    public int getMinDurationDays() {
        return 1;
    }

    @Override
    public int getMaxDurationDays() {
        return 1;
    }

    @Override
    public boolean canStartToday(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context) {
        return false;
    }

    @Override
    public void onStart(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context) {
        context.getEligiblePlayers().forEach(p ->
            p.sendMessage("§a[Events] Test event started (day " + snapshot.dayCount() + ")."));
    }

    @Override
    public void onEnd(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context) {
        context.getEligiblePlayers().forEach(p ->
            p.sendMessage("§7[Events] Test event ended."));
    }
}
