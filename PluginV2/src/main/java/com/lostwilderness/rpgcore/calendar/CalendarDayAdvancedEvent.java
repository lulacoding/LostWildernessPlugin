package com.lostwilderness.rpgcore.calendar;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Fired when the calendar advances to the next day (MC day change detected or admin /nextday).
 * Other modules (e.g. Events) can listen and run day-based logic without polling.
 */
public final class CalendarDayAdvancedEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final CalendarServiceV2.CalendarSnapshot newSnapshot;
    private final CalendarServiceV2.CalendarSnapshot previousSnapshot;

    public CalendarDayAdvancedEvent(CalendarServiceV2.CalendarSnapshot newSnapshot,
                                    CalendarServiceV2.CalendarSnapshot previousSnapshot) {
        super(false);
        this.newSnapshot = newSnapshot;
        this.previousSnapshot = previousSnapshot;
    }

    public CalendarServiceV2.CalendarSnapshot getNewSnapshot() {
        return newSnapshot;
    }

    public CalendarServiceV2.CalendarSnapshot getPreviousSnapshot() {
        return previousSnapshot;
    }

    public long getNewDayCount() {
        return newSnapshot.dayCount();
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
