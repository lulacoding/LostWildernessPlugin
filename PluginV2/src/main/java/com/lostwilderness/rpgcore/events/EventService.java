package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;

import java.util.Set;

/**
 * Service that runs seasonal events: one active at a time, driven by calendar day advance.
 */
public interface EventService {

    SeasonalEvent getActive();

    int getDaysRemaining();

    SeasonalEvent getQueuedTomorrow();

    Set<String> getRegisteredEventIds();

    SeasonalEvent getEventById(String id);

    boolean forceStart(String id, Integer durationOverride);

    void forceStop();

    /**
     * Check if party requirements are met (e.g., minimum players in parties).
     */
    boolean meetsPartyRequirement(int minPlayers);

    /**
     * Apply a potion effect to all members of a player's party.
     */
    void applyBuffToParty(java.util.UUID playerUuid, org.bukkit.potion.PotionEffectType effectType,
                          int duration, int amplifier);
}
