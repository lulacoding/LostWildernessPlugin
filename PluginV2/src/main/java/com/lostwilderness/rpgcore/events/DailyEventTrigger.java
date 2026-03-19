package com.lostwilderness.rpgcore.events;

import org.bukkit.World;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;

/**
 * Probabilistic trigger: when a parent daily event runs, maybe trigger a child (or run action) with a chance.
 * Receives world so cascades run in the same dimension as the parent.
 */
final class DailyEventTrigger {

    private final DailyWorldEvent child;
    private final BiConsumer<Integer, World> action; // optional; if null use child.onCalendarDay
    private final double chance; // 0.0–1.0

    DailyEventTrigger(DailyWorldEvent child, double chance) {
        this.child = Objects.requireNonNull(child);
        this.action = null;
        this.chance = Math.max(0.0, Math.min(1.0, chance));
    }

    DailyEventTrigger(BiConsumer<Integer, World> action, double chance) {
        this.child = null;
        this.action = Objects.requireNonNull(action);
        this.chance = Math.max(0.0, Math.min(1.0, chance));
    }

    void maybeTrigger(int newDay, World world) {
        if (chance <= 0.0) return;
        if (ThreadLocalRandom.current().nextDouble() > chance) return;
        if (action != null) {
            action.accept(newDay, world);
        } else {
            child.onCalendarDay(newDay, world);
        }
    }
}
