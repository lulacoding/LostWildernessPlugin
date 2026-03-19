package com.lostwilderness.rpgcore.events;

import org.bukkit.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * Maps parent daily events to cascade triggers (child event or day+world callback + chance). After a parent runs onCalendarDay, triggers are evaluated.
 */
final class EventCascadeRegistry {

    private final Map<DailyWorldEvent, List<DailyEventTrigger>> triggers = new IdentityHashMap<>();

    void register(DailyWorldEvent parent, DailyWorldEvent child, double chance) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(child);
        triggers.computeIfAbsent(parent, p -> new ArrayList<>()).add(new DailyEventTrigger(child, chance));
    }

    /** Register a cascade that runs a custom action (e.g. triggerEclipseStorm(day, world)) with chance. */
    void register(DailyWorldEvent parent, BiConsumer<Integer, World> action, double chance) {
        Objects.requireNonNull(parent);
        Objects.requireNonNull(action);
        triggers.computeIfAbsent(parent, p -> new ArrayList<>()).add(new DailyEventTrigger(action, chance));
    }

    List<DailyEventTrigger> getTriggers(DailyWorldEvent parent) {
        List<DailyEventTrigger> list = triggers.get(parent);
        return list == null ? List.of() : Collections.unmodifiableList(list);
    }
}
