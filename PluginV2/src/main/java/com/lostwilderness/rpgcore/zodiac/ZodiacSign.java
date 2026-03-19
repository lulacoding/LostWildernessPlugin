package com.lostwilderness.rpgcore.zodiac;

import java.util.concurrent.ThreadLocalRandom;

public enum ZodiacSign {
    ARIES("Aries", "♈", 3, 21, 4, 19),
    TAURUS("Taurus", "♉", 4, 20, 5, 20),
    GEMINI("Gemini", "♊", 5, 21, 6, 20),
    CANCER("Cancer", "♋", 6, 21, 7, 22),
    LEO("Leo", "♌", 7, 23, 8, 22),
    VIRGO("Virgo", "♍", 8, 23, 9, 22),
    LIBRA("Libra", "♎", 9, 23, 10, 22),
    SCORPIO("Scorpio", "♏", 10, 23, 11, 21),
    SAGITTARIUS("Sagittarius", "♐", 11, 22, 12, 21),
    CAPRICORN("Capricorn", "♑", 12, 22, 1, 19),
    AQUARIUS("Aquarius", "♒", 1, 20, 2, 18),
    PISCES("Pisces", "♓", 2, 19, 3, 20),
    OPHIUCHUS("Ophiuchus", "⛎", 11, 29, 12, 17);

    private final String displayName;
    private final String symbol;
    private final int startMonth;
    private final int startDay;
    private final int endMonth;
    private final int endDay;

    ZodiacSign(String displayName, String symbol, int startMonth, int startDay, int endMonth, int endDay) {
        this.displayName = displayName;
        this.symbol = symbol;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.endMonth = endMonth;
        this.endDay = endDay;
    }

    public String displayName() {
        return displayName;
    }

    public String symbol() {
        return symbol;
    }

    public int startMonth() {
        return startMonth;
    }

    public int startDay() {
        return startDay;
    }

    public int endMonth() {
        return endMonth;
    }

    public int endDay() {
        return endDay;
    }

    /**
     * Check if the given MC date falls within the Ophiuchus window (Nov 29 - Dec 17).
     */
    public static boolean isOphiuchusWindow(int month, int day) {
        return (month == 11 && day >= 29) || (month == 12 && day <= 17);
    }

    /**
     * Get the zodiac sign for a given MC calendar date.
     * Handles Ophiuchus overlap with 80/20 random assignment during Nov 29-Dec 17.
     *
     * @param month MC month (1-12)
     * @param day   MC day (1-30)
     * @param forceOphiuchus if true, skip randomness and return OPHIUCHUS (for Epoch Day 0)
     * @return The zodiac sign for this date
     */
    public static ZodiacSign getSignForMCDate(int month, int day, boolean forceOphiuchus) {
        if (forceOphiuchus) {
            return OPHIUCHUS;
        }

        ZodiacSign baseSign = null;

        for (ZodiacSign sign : values()) {
            if (sign == OPHIUCHUS) continue; // Skip Ophiuchus in normal calculation

            if (sign.startMonth == sign.endMonth) {
                if (month == sign.startMonth && day >= sign.startDay && day <= sign.endDay) {
                    baseSign = sign;
                    break;
                }
            } else {
                if ((month == sign.startMonth && day >= sign.startDay) ||
                    (month == sign.endMonth && day <= sign.endDay)) {
                    baseSign = sign;
                    break;
                }
            }
        }

        if (baseSign == null) {
            baseSign = CAPRICORN; // Default fallback
        }

        // During Ophiuchus window, 20% chance to override Sagittarius with Ophiuchus
        if (isOphiuchusWindow(month, day) && baseSign == SAGITTARIUS) {
            int roll = ThreadLocalRandom.current().nextInt(100);
            if (roll < 20) {
                return OPHIUCHUS;
            }
        }

        return baseSign;
    }

    /**
     * Get the zodiac sign for a given MC calendar date (no Epoch Day 0 handling).
     */
    public static ZodiacSign getSignForMCDate(int month, int day) {
        return getSignForMCDate(month, day, false);
    }

    /**
     * Get the zodiac sign for a given MC year.
     * Cycles through all 13 signs: Year 1 = Aries, Year 2 = Taurus, ..., Year 13 = Ophiuchus, Year 14 = Aries, etc.
     *
     * @param mcYear MC calendar year (1+)
     * @return The zodiac sign for this year
     */
    public static ZodiacSign getSignForYear(int mcYear) {
        ZodiacSign[] signs = values();
        int index = ((mcYear - 1) % signs.length);
        return signs[index];
    }
}
