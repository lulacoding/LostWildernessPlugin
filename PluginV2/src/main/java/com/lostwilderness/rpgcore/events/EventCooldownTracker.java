package com.lostwilderness.rpgcore.events;

import java.util.HashMap;
import java.util.Map;

/**
 * Day-based cooldowns per event and per world (by class name + world name).
 * Allows events to run at different times in Survival vs Amplified (or other dimensions).
 */
final class EventCooldownTracker {

    private final Map<String, Integer> nextAllowedDayByKey = new HashMap<>();

    boolean canRun(DailyWorldEvent ev, String worldName, int day) {
        int next = nextAllowedDayByKey.getOrDefault(key(ev, worldName), Integer.MIN_VALUE);
        return day >= next;
    }

    void setCooldown(DailyWorldEvent ev, String worldName, int currentDay, int cooldownDays) {
        if (cooldownDays <= 0) return;
        nextAllowedDayByKey.put(key(ev, worldName), currentDay + cooldownDays);
    }

    private static String key(DailyWorldEvent ev, String worldName) {
        return ev.getClass().getName() + ":" + (worldName != null ? worldName : "");
    }
}
