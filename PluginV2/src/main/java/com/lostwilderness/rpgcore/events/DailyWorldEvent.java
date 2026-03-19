package com.lostwilderness.rpgcore.events;

import org.bukkit.World;

/**
 * Old-plugin-style event: notified every calendar day via onCalendarDay(day, world).
 * Dispatched once per overworld/dimension so Survival and Amplified can have events at different times.
 * Can be region-aware (stays active across days) with isActive() and forceEndEarly().
 */
public interface DailyWorldEvent {

    /**
     * Called each time the calendar advances, once per world. Use the given world for all effects (weather, players, etc.).
     * @param day effective day for this world (global day + world offset)
     * @param world the overworld/dimension this dispatch is for (e.g. Survival or Amplified)
     */
    void onCalendarDay(int day, World world);

    /**
     * True if the event is currently active (e.g. Eclipse in progress).
     */
    boolean isActive();

    /**
     * Force end this event early (e.g. on day change before evaluating new events).
     */
    void forceEndEarly();

    /**
     * Display name for boss bar and commands.
     */
    String getDisplayName();
}
