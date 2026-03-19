package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.model.Clan;

import java.util.List;
import java.util.UUID;

/**
 * War between clans: declare, ceasefire, status. Used for head drops when killing enemy in war.
 */
public interface WarService {

    boolean areAtWar(UUID clanId1, UUID clanId2);

    void declareWar(UUID clanId1, UUID clanId2);

    void ceasefire(UUID clanId1, UUID clanId2);

    /** Pairs of clan IDs currently at war. */
    List<UUID[]> getActiveWars();
}
