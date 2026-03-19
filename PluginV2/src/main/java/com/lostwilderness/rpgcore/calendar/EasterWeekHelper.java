package com.lostwilderness.rpgcore.calendar;

import java.time.LocalDate;

/**
 * Easter week titles for the day-change subtitle (Palm Sunday through Easter Sunday).
 * Uses Gregorian Easter algorithm for the calendar year; matches old plugin behaviour.
 */
public final class EasterWeekHelper {

    private EasterWeekHelper() {}

    /**
     * Returns the Easter week subtitle for the given date, or null if not in Easter week.
     * Easter week = Palm Sunday (Easter-7) through Easter Sunday.
     */
    public static String getSubtitleForDate(LocalDate date) {
        int year = date.getYear();
        LocalDate easterSunday = computeEasterSunday(year);
        if (easterSunday == null) return null;
        LocalDate palmSunday = easterSunday.minusDays(7);
        if (date.isBefore(palmSunday) || date.isAfter(easterSunday)) return null;
        int delta = (int) java.time.temporal.ChronoUnit.DAYS.between(palmSunday, date);
        return switch (delta) {
            case 0 -> "§d🌿 Palm Sunday: Prepare the way! 🌿";
            case 1 -> "§eHoly Monday: The journey begins.";
            case 2 -> "§eHoly Tuesday: Watch and pray.";
            case 3 -> "§eHoly Wednesday: Betrayal in the air.";
            case 4 -> "§6Maundy Thursday: The Last Supper.";
            case 5 -> "§c✝ Good Friday — The Lord was forsaken… ✝";
            case 6 -> "§7Holy Saturday: The tomb is silent.";
            case 7 -> "§a☀ He is risen! Happy Easter Sunday! ☀";
            default -> null;
        };
    }

    /**
     * Anonymous Gregorian algorithm: returns Easter Sunday for the given year.
     */
    public static LocalDate computeEasterSunday(int year) {
        int a = year % 19;
        int b = year / 100;
        int c = year % 100;
        int d = b / 4;
        int e = b % 4;
        int f = (b + 8) / 25;
        int g = (b - f + 1) / 3;
        int h = (19 * a + b - d - g + 15) % 30;
        int i = c / 4;
        int k = c % 4;
        int l = (32 + 2 * e + 2 * i - h - k) % 7;
        int m = (a + 11 * h + 22 * l) / 451;
        int month = (h + l - 7 * m + 114) / 31;
        int day = ((h + l - 7 * m + 114) % 31) + 1;
        return LocalDate.of(year, month, day);
    }
}
