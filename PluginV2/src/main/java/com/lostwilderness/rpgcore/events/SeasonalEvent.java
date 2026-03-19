package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import org.bukkit.event.Listener;

/**
 * A calendar-driven event with explicit lifecycle: start, day tick, per-tick, end.
 * Selection is via canStartToday(snapshot, context); duration in days is min/max.
 */
public interface SeasonalEvent extends Listener {

    String getId();

    String getDisplayName();

    boolean isSeasonAllowed(CalendarServiceV2.Season season);

    int getMinDurationDays();

    int getMaxDurationDays();

    boolean canStartToday(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context);

    void onStart(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context);

    void onEnd(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context);

    default void onDayTick(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context) {}

    default void onTick(CalendarServiceV2.CalendarSnapshot snapshot, EventContext context) {}
}
