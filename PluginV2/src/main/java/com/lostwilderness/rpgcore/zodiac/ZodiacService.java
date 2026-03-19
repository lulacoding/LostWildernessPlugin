package com.lostwilderness.rpgcore.zodiac;

import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing player zodiac signs and calculating bonuses.
 */
public interface ZodiacService {

    /**
     * Assign zodiac signs to a player based on their join date.
     * Called on first join.
     *
     * @param playerUuid Player UUID
     * @param epochDay   Day count since Epoch (Day 0)
     * @return The newly created zodiac profile
     */
    ZodiacProfile assignZodiac(UUID playerUuid, long epochDay);

    /**
     * Get a player's zodiac profile from cache or database.
     *
     * @param playerUuid Player UUID
     * @return Optional zodiac profile
     */
    Optional<ZodiacProfile> getZodiacProfile(UUID playerUuid);

    /**
     * Reveal an Epochian player's hidden second sign.
     * Requires Lord rank permission for the revealer.
     *
     * @param targetUuid UUID of player to reveal
     * @param lordUuid   UUID of Lord-rank player performing reveal
     */
    void revealSecondSign(UUID targetUuid, UUID lordUuid);

    /**
     * Reveal a player's hidden spirit animal.
     * Requires Lord rank permission for the revealer.
     *
     * @param targetUuid UUID of player to reveal
     * @param lordUuid   UUID of Lord-rank player performing reveal
     */
    void revealSpiritAnimal(UUID targetUuid, UUID lordUuid);

    /**
     * Get the current zodiac sign for the current MC calendar month.
     *
     * @return Current month's zodiac sign
     */
    ZodiacSign getCurrentMonthSign();

    /**
     * Get the current zodiac sign for the current MC calendar year.
     *
     * @return Current year's zodiac sign
     */
    ZodiacSign getCurrentYearSign();

    /**
     * Check if a player's month sign matches the current month's zodiac.
     *
     * @param playerUuid Player UUID
     * @return true if month signs match
     */
    boolean isSignMonthActive(UUID playerUuid);

    /**
     * Check if a player's year sign matches the current year's zodiac.
     *
     * @param playerUuid Player UUID
     * @return true if year signs match
     */
    boolean isSignYearActive(UUID playerUuid);

    /**
     * Check if both month and year signs are active (sync bonus).
     *
     * @param playerUuid Player UUID
     * @return true if both signs match current month and year
     */
    boolean isSyncActive(UUID playerUuid);

    /**
     * Check if a clan leader's year sign matches the current year (clan-wide bonus).
     *
     * @param clanLeaderUuid UUID of clan leader
     * @return true if leader's year sign matches current year
     */
    boolean isClanLeaderYearBonusActive(UUID clanLeaderUuid);

    /**
     * Invalidate cached profile for a player.
     *
     * @param playerUuid Player UUID
     */
    void invalidateCache(UUID playerUuid);
}
