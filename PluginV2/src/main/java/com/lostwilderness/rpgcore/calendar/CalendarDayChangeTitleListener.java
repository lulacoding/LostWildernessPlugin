package com.lostwilderness.rpgcore.calendar;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.time.Month;
import java.time.format.TextStyle;
import java.util.Locale;

/**
 * Sends day-change title and subtitle to overworld players when the calendar advances,
 * matching old plugin behaviour: "March 1, 1MC" + season or "Day X". Also triggers
 * New Year fireworks when the year rolls over.
 */
public final class CalendarDayChangeTitleListener implements Listener {

    private static final String[] SEASON_TITLES = {
        "Spring has sprung! 🌱",
        "Summer is here! ☀️",
        "Autumn leaves are falling 🍂",
        "Winter's chill arrives ❄️"
    };

    private final Plugin plugin;
    private final NewYearFireworkHandler newYearFireworks;

    public CalendarDayChangeTitleListener(Plugin plugin) {
        this.plugin = plugin;
        this.newYearFireworks = new NewYearFireworkHandler(plugin);
    }

    @EventHandler
    public void onDayAdvanced(CalendarDayAdvancedEvent e) {
        CalendarServiceV2.CalendarSnapshot newSnap = e.getNewSnapshot();
        CalendarServiceV2.CalendarSnapshot prevSnap = e.getPreviousSnapshot();
        long dayCount = newSnap.dayCount();

        newYearFireworks.onDayAdvanced(newSnap, prevSnap);

        String title = formatDate(newSnap.date());
        String subtitle = buildSubtitle(newSnap, prevSnap, dayCount);

        World overworld = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
        if (overworld == null) return;

        for (Player p : overworld.getPlayers()) {
            try {
                p.sendTitle(title, subtitle, 10, 60, 10);
            } catch (Exception ex) {
                plugin.getLogger().warning("Day-change title failed for " + p.getName() + ": " + ex.getMessage());
            }
        }
    }

    private static String formatDate(java.time.LocalDate date) {
        Month month = date.getMonth();
        String monthName = month.getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        return monthName + " " + date.getDayOfMonth() + ", " + date.getYear() + "MC";
    }

    private static String buildSubtitle(CalendarServiceV2.CalendarSnapshot newSnap,
                                        CalendarServiceV2.CalendarSnapshot prevSnap,
                                        long dayCount) {
        if (dayCount == 0) {
            return "A new era begins (Day 0)";
        }
        String easterSubtitle = EasterWeekHelper.getSubtitleForDate(newSnap.date());
        if (easterSubtitle != null) {
            return easterSubtitle + " (Day " + dayCount + ")";
        }
        CalendarServiceV2.Season current = newSnap.season();
        CalendarServiceV2.Season previous = prevSnap != null ? prevSnap.season() : null;
        if (previous != null && current != previous) {
            return SEASON_TITLES[current.ordinal()] + " (Day " + dayCount + ")";
        }
        return "Day " + dayCount;
    }
}
