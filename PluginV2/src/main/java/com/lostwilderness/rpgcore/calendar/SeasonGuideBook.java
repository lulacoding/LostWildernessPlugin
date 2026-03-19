package com.lostwilderness.rpgcore.calendar;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.Plugin;

/**
 * Builds the in-game seasonal guide book for /season guide.
 * Content is static; can later be driven by config or lang.
 */
public final class SeasonGuideBook {

    private SeasonGuideBook() {}

    public static ItemStack build(Plugin plugin) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return book;
        meta.setTitle("Seasonal Guide");
        meta.setAuthor("Lost Wilderness");
        meta.setPages(
            "Seasons\n\n" +
            "The world has four seasons: Spring, Summer, Autumn, and Winter. Each affects weather, crops, and wildlife.\n\n" +
            "Use /date and /season to see the current date and season.",
            "Events\n\n" +
            "World events can occur: Eclipse, Thunderstorms, Blizzard, Frost, Heatwave, Fog, Jungle Monsoon, Seasonal Storms, Spring Bloom, Autumn leaves, and more.\n\n" +
            "Use /event list and /event info to see active events.",
            "Seasonal Crops\n\n" +
            "Spring: wheat, carrots, potatoes, pumpkins.\n" +
            "Summer: melons, sugarcane, cactus.\n" +
            "Autumn: mushrooms, sweet berries.\n" +
            "Winter: frozen soil, scarce crops.\n\n" +
            "Ideal crops grow better in their season.",
            "Wildlife\n\n" +
            "Animals adapt to the season: bees and sheep in spring, turtles and fish in summer, foxes in autumn, wolves in winter.\n\n" +
            "Spawn rates may vary by season.",
            "Weather\n\n" +
            "Each season has typical weather: rain, snow, fog, or sun. Winter brings frost and snow; summer brings heat and storms.\n\n" +
            "The calendar drives seasonal weather."
        );
        book.setItemMeta(meta);
        return book;
    }
}
