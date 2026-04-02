package com.lostwilderness.rpgcore.progression;

/**
 * Constants for milestone/achievement keys. Namespaced strings so new keys can be added without code change.
 */
public final class AchievementKey {

    // Join & Loyalty
    public static final String MILESTONE_FIRST_JOIN = "milestone:first_join";
    public static final String MILESTONE_JOIN_3_TIMES = "milestone:join_3_times";
    public static final String MILESTONE_JOIN_10_TIMES = "milestone:join_10_times";
    public static final String MILESTONE_JOIN_25_TIMES = "milestone:join_25_times";
    public static final String MILESTONE_JOIN_50_TIMES = "milestone:join_50_times";
    public static final String MILESTONE_JOIN_100_TIMES = "milestone:join_100_times";

    public static final String CLAIMED_FIRST_JOIN = "claimed:first_join";
    public static final String CLAIMED_JOIN_3_TIMES = "claimed:join_3_times";
    public static final String CLAIMED_JOIN_10_TIMES = "claimed:join_10_times";
    public static final String CLAIMED_JOIN_25_TIMES = "claimed:join_25_times";
    public static final String CLAIMED_JOIN_50_TIMES = "claimed:join_50_times";
    public static final String CLAIMED_JOIN_100_TIMES = "claimed:join_100_times";

    // Playtime (seconds)
    public static final String MILESTONE_PLAYTIME_1H = "milestone:playtime_1h";
    public static final String MILESTONE_PLAYTIME_5H = "milestone:playtime_5h";
    public static final String MILESTONE_PLAYTIME_24H = "milestone:playtime_24h";
    public static final String MILESTONE_PLAYTIME_100H = "milestone:playtime_100h";
    public static final String CLAIMED_PLAYTIME_1H = "claimed:playtime_1h";
    public static final String CLAIMED_PLAYTIME_5H = "claimed:playtime_5h";
    public static final String CLAIMED_PLAYTIME_24H = "claimed:playtime_24h";
    public static final String CLAIMED_PLAYTIME_100H = "claimed:playtime_100h";

    // Seasons
    public static final String MILESTONE_FIRST_SEASON_SPRING = "milestone:first_season_spring";
    public static final String MILESTONE_FIRST_SEASON_SUMMER = "milestone:first_season_summer";
    public static final String MILESTONE_FIRST_SEASON_AUTUMN = "milestone:first_season_autumn";
    public static final String MILESTONE_FIRST_SEASON_WINTER = "milestone:first_season_winter";
    public static final String MILESTONE_NEW_YEAR_LOGIN = "milestone:new_year_login";
    public static final String CLAIMED_FIRST_SEASON_SPRING = "claimed:first_season_spring";
    public static final String CLAIMED_FIRST_SEASON_SUMMER = "claimed:first_season_summer";
    public static final String CLAIMED_FIRST_SEASON_AUTUMN = "claimed:first_season_autumn";
    public static final String CLAIMED_FIRST_SEASON_WINTER = "claimed:first_season_winter";
    public static final String CLAIMED_NEW_YEAR_LOGIN = "claimed:new_year_login";

    // Deaths
    public static final String MILESTONE_FIRST_DEATH = "milestone:first_death";
    public static final String MILESTONE_DEATHS_10 = "milestone:deaths_10";
    public static final String CLAIMED_FIRST_DEATH = "claimed:first_death";
    public static final String CLAIMED_DEATHS_10 = "claimed:deaths_10";

    // Clans
    public static final String MILESTONE_JOINED_CLAN = "milestone:joined_clan";
    public static final String CLAIMED_JOINED_CLAN = "claimed:joined_clan";

    // Bosses
    public static final String MILESTONE_DEVOIDER_1 = "milestone:boss_devoider_1";
    public static final String MILESTONE_DEVOIDER_2 = "milestone:boss_devoider_2";
    public static final String MILESTONE_DEVOIDER_3 = "milestone:boss_devoider_3";
    public static final String MILESTONE_DEVOIDER_4 = "milestone:boss_devoider_4";
    public static final String MILESTONE_DEVOIDER_5 = "milestone:boss_devoider_5";
    public static final String MILESTONE_DEVOIDER_6 = "milestone:boss_devoider_6";
    public static final String MILESTONE_EL_DIABLO = "milestone:boss_el_diablo";

    // Story Progression
    public static final String MILESTONE_NETHER_PORTAL = "milestone:nether_portal";
    public static final String MILESTONE_ENDER_DRAGON = "milestone:ender_dragon";
    public static final String MILESTONE_FATHER_OF_ENDER = "milestone:father_of_ender";
    public static final String MILESTONE_WITHER_6 = "milestone:wither_6";
    public static final String MILESTONE_WITHER_36 = "milestone:wither_36";
    public static final String MILESTONE_HEAVENLY_TOWER = "milestone:heavenly_tower";
    public static final String MILESTONE_LORDS_PLATEAU = "milestone:lords_plateau";

    // Story Flags (server-state markers)
    public static final String FLAG_INTRO_SEEN = "flag:intro_seen";
    public static final String FLAG_REDEEMER_PENDING = "flag:redeemer_pending";
    public static final String FLAG_HEAVENS_GATE_PENDING = "flag:heavens_gate_pending";

    // Elemental Temples
    public static final String MILESTONE_TEMPLE_FIRE = "milestone:temple_fire";
    public static final String MILESTONE_TEMPLE_EARTH = "milestone:temple_earth";
    public static final String MILESTONE_TEMPLE_WIND = "milestone:temple_wind";
    public static final String MILESTONE_TEMPLE_WATER = "milestone:temple_water";
    public static final String MILESTONE_TEMPLE_AETHER = "milestone:temple_aether";

    public static final String COUNTER_JOIN_COUNT = "join_count";
    public static final String COUNTER_PLAYTIME_SECONDS = "playtime_seconds";
    public static final String COUNTER_DEATH_COUNT = "death_count";
    public static final String COUNTER_WITHER_KILLS_SOLO = "wither_kills_solo";

    private AchievementKey() {}
}
