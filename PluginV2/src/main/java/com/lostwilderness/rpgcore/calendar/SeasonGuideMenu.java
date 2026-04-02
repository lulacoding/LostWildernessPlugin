package com.lostwilderness.rpgcore.calendar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * /season GUI — 54-slot chest showing current season, date, crops, wildlife, and weather.
 *
 * Layout:
 *   Row 0 (0–8):   glass border
 *   Row 1 (9–17):  season icon | gap | date | gap | next season | gap gap gap gap
 *   Row 2 (18–26): crops | gap | wildlife | gap | weather | gap | events | gap gap
 *   Row 3 (27–35): glass border
 *   Row 4 (36–44): gap gap gap gap | guide book button | gap gap gap gap
 *   Row 5 (45–53): glass border
 */
public final class SeasonGuideMenu implements Listener {

    static final String TITLE = ChatColor.DARK_GREEN + "Season Guide";

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MMMM d");

    private final CalendarServiceV2 calendarService;
    private final Plugin plugin;
    private final EquatorSettings equatorSettings;

    public SeasonGuideMenu(CalendarServiceV2 calendarService, Plugin plugin, EquatorSettings equatorSettings) {
        this.calendarService = calendarService;
        this.plugin = plugin;
        this.equatorSettings = equatorSettings != null ? equatorSettings : EquatorSettings.disabled();
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);

        CalendarServiceV2.CalendarSnapshot snap = calendarService.getCurrentSnapshot();
        CalendarServiceV2.Season season = snap.season();
        String dateStr = snap.date().format(DATE_FMT);
        long dayCount = snap.dayCount();
        boolean inEquator = EquatorZone.isInBand(player, equatorSettings);

        // ── Row 0: border ─────────────────────────────────────────────────────
        ItemStack border = glass(ChatColor.DARK_GREEN + " ");
        for (int i = 0; i < 9; i++) inv.setItem(i, border);

        // ── Row 1: season icon, date, next season ─────────────────────────────
        inv.setItem(9, inEquator ? equatorBeltItem() : seasonIcon(season));
        inv.setItem(11, dateItem(dateStr, dayCount));
        inv.setItem(13, inEquator ? equatorNextSeasonItem(season) : nextSeasonItem(season));

        // ── Row 2: crops, wildlife, weather, events ───────────────────────────
        inv.setItem(18, inEquator ? equatorCropsItem() : cropsItem(season));
        inv.setItem(20, inEquator ? equatorWildlifeItem() : wildlifeItem(season));
        inv.setItem(22, inEquator ? equatorWeatherItem() : weatherItem(season));
        inv.setItem(24, inEquator ? equatorEventsItem() : eventsItem(season));

        // ── Row 3: border ─────────────────────────────────────────────────────
        for (int i = 27; i < 36; i++) inv.setItem(i, border);

        // ── Row 4: guide book button ──────────────────────────────────────────
        inv.setItem(40, makeItem(Material.WRITTEN_BOOK,
                ChatColor.YELLOW + "" + ChatColor.BOLD + "Get Season Guide Book",
                List.of(ChatColor.GRAY + "Click to receive the full",
                        ChatColor.GRAY + "Seasonal Guide as a book.")));

        // ── Row 5: border ─────────────────────────────────────────────────────
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!TITLE.equals(event.getView().getTitle())) return;
        event.setCancelled(true);

        // Guide book button
        if (event.getSlot() == 40) {
            player.closeInventory();
            ItemStack book = SeasonGuideBook.build(plugin);
            player.getInventory().addItem(book);
            player.sendMessage(ChatColor.GREEN + "You received the Seasonal Guide!");
        }
    }

    // ── Item builders ─────────────────────────────────────────────────────────

    private ItemStack equatorBeltItem() {
        int half = equatorSettings.halfWidthBlocks();
        return makeItem(Material.LIME_GLAZED_TERRACOTTA,
                ChatColor.GREEN + "" + ChatColor.BOLD + "Equator Belt",
                List.of(ChatColor.GRAY + "You are in the equatorial buffer",
                        ChatColor.GRAY + "(within " + half + " blocks of Z = 0).",
                        ChatColor.DARK_GRAY + "No hemispheric season here - mild zone.",
                        ChatColor.DARK_GRAY + "The world calendar still advances.",
                        equatorSettings.reduceWeather()
                                ? ChatColor.AQUA + "Storms are softened in this band."
                                : ChatColor.DARK_GRAY + "Weather follows the world."));
    }

    private ItemStack equatorNextSeasonItem(CalendarServiceV2.Season globalSeason) {
        CalendarServiceV2.Season next = switch (globalSeason) {
            case SPRING -> CalendarServiceV2.Season.SUMMER;
            case SUMMER -> CalendarServiceV2.Season.AUTUMN;
            case AUTUMN -> CalendarServiceV2.Season.WINTER;
            case WINTER -> CalendarServiceV2.Season.SPRING;
        };
        String color = seasonColor(next);
        return makeItem(Material.RECOVERY_COMPASS,
                ChatColor.GRAY + "Global calendar season",
                List.of(ChatColor.GRAY + "Next for the world: " + color + toTitleCase(next.name()),
                        ChatColor.DARK_GRAY + "Your local belt: no winter/summer split.",
                        ChatColor.DARK_GRAY + "Northern/southern seasons: not implemented."));
    }

    private ItemStack equatorCropsItem() {
        return makeItem(Material.WHEAT_SEEDS,
                ChatColor.GREEN + "Growing here",
                List.of(ChatColor.GRAY + "Biomes near spawn vary - check the ground.",
                        ChatColor.GRAY + "Global crop bonuses still follow the month."));
    }

    private ItemStack equatorWildlifeItem() {
        return makeItem(Material.FEATHER,
                ChatColor.YELLOW + "Wildlife",
                List.of(ChatColor.GRAY + "Spawns depend on biome and season.",
                        ChatColor.GRAY + "The belt is a travel bridge, not one biome."));
    }

    private ItemStack equatorWeatherItem() {
        return makeItem(Material.SUNFLOWER,
                ChatColor.GOLD + "Weather in the belt",
                List.of(ChatColor.GRAY + "Reduced harsh rain/storm on you",
                        ChatColor.GRAY + "when enabled in lw-climate.yml.",
                        ChatColor.DARK_GRAY + "Heat waves and jungles still use biome rules."));
    }

    private ItemStack equatorEventsItem() {
        return makeItem(Material.SNOWBALL,
                ChatColor.AQUA + "Cold events",
                List.of(ChatColor.GRAY + "Frost & blizzard skip ice/strays",
                        ChatColor.GRAY + "in this Z-band when enabled.",
                        ChatColor.DARK_GRAY + "Travel north/south for full winter."));
    }

    private ItemStack seasonIcon(CalendarServiceV2.Season season) {
        Material mat = switch (season) {
            case SPRING -> Material.PINK_TULIP;
            case SUMMER -> Material.SUNFLOWER;
            case AUTUMN -> Material.ORANGE_TULIP;
            case WINTER -> Material.BLUE_ICE;
        };
        String color = seasonColor(season);
        String name = color + ChatColor.BOLD + toTitleCase(season.name()) + " Season";
        List<String> lore = switch (season) {
            case SPRING -> List.of(
                    ChatColor.GRAY + "The world blooms anew.",
                    ChatColor.GRAY + "Rain and mild weather.",
                    ChatColor.GREEN + "Crops grow quickly.");
            case SUMMER -> List.of(
                    ChatColor.GRAY + "Heat beats down.",
                    ChatColor.GRAY + "Storms brew in the distance.",
                    ChatColor.YELLOW + "Melons and sugarcane thrive.");
            case AUTUMN -> List.of(
                    ChatColor.GRAY + "Leaves turn gold.",
                    ChatColor.GRAY + "Cool, foggy nights.",
                    ChatColor.GOLD + "Mushrooms and berries abound.");
            case WINTER -> List.of(
                    ChatColor.GRAY + "The land is frozen.",
                    ChatColor.GRAY + "Blizzards and frost.",
                    ChatColor.AQUA + "Crops struggle to grow.");
        };
        return makeItem(mat, name, lore);
    }

    private ItemStack dateItem(String dateStr, long dayCount) {
        return makeItem(Material.CLOCK,
                ChatColor.YELLOW + "Current Date",
                List.of(ChatColor.WHITE + dateStr,
                        ChatColor.GRAY + "Day #" + dayCount + " of the world"));
    }

    private ItemStack nextSeasonItem(CalendarServiceV2.Season current) {
        CalendarServiceV2.Season next = switch (current) {
            case SPRING -> CalendarServiceV2.Season.SUMMER;
            case SUMMER -> CalendarServiceV2.Season.AUTUMN;
            case AUTUMN -> CalendarServiceV2.Season.WINTER;
            case WINTER -> CalendarServiceV2.Season.SPRING;
        };
        String color = seasonColor(next);
        return makeItem(Material.COMPASS,
                ChatColor.GRAY + "Next Season",
                List.of(color + toTitleCase(next.name()),
                        ChatColor.DARK_GRAY + "Changes on the 1st of next month"));
    }

    private ItemStack cropsItem(CalendarServiceV2.Season season) {
        String crops = switch (season) {
            case SPRING -> "Wheat, Carrots, Potatoes, Pumpkins";
            case SUMMER -> "Melons, Sugarcane, Cactus";
            case AUTUMN -> "Mushrooms, Sweet Berries, Beetroot";
            case WINTER -> "Most crops grow slowly";
        };
        return makeItem(Material.WHEAT,
                ChatColor.GREEN + "Seasonal Crops",
                List.of(ChatColor.GRAY + crops));
    }

    private ItemStack wildlifeItem(CalendarServiceV2.Season season) {
        String wildlife = switch (season) {
            case SPRING -> "Bees, Sheep, Rabbits active";
            case SUMMER -> "Turtles, Fish, Dolphins spawn";
            case AUTUMN -> "Foxes, Mushroom Cows appear";
            case WINTER -> "Wolves, Polar Bears, Strays spawn";
        };
        return makeItem(Material.FEATHER,
                ChatColor.YELLOW + "Wildlife",
                List.of(ChatColor.GRAY + wildlife));
    }

    private ItemStack weatherItem(CalendarServiceV2.Season season) {
        String weather = switch (season) {
            case SPRING -> "Rain showers, mild temperatures";
            case SUMMER -> "Heat, occasional thunderstorms";
            case AUTUMN -> "Fog, wind, light rain";
            case WINTER -> "Snow, blizzards, frost";
        };
        Material mat = switch (season) {
            case SPRING -> Material.WATER_BUCKET;
            case SUMMER -> Material.FIRE_CHARGE;
            case AUTUMN -> Material.GRAY_DYE;
            case WINTER -> Material.SNOWBALL;
        };
        return makeItem(mat,
                ChatColor.AQUA + "Typical Weather",
                List.of(ChatColor.GRAY + weather));
    }

    private ItemStack eventsItem(CalendarServiceV2.Season season) {
        List<String> events = switch (season) {
            case SPRING -> List.of(ChatColor.GRAY + "Spring Bloom",
                    ChatColor.GRAY + "Easter (The Redeemer arrives)");
            case SUMMER -> List.of(ChatColor.GRAY + "Heatwave",
                    ChatColor.GRAY + "Jungle Monsoon",
                    ChatColor.GRAY + "Thunderstorm");
            case AUTUMN -> List.of(ChatColor.GRAY + "Autumn Leaves",
                    ChatColor.GRAY + "Fog events",
                    ChatColor.GRAY + "Eclipse");
            case WINTER -> List.of(ChatColor.GRAY + "Blizzard",
                    ChatColor.GRAY + "Frost",
                    ChatColor.GRAY + "New Year Fireworks");
        };
        return makeItem(Material.NETHER_STAR,
                ChatColor.LIGHT_PURPLE + "Seasonal Events",
                events);
    }

    private ItemStack glass(String name) {
        return makeItem(Material.GREEN_STAINED_GLASS_PANE, name, List.of());
    }

    private ItemStack makeItem(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;
        meta.setDisplayName(name);
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private static String seasonColor(CalendarServiceV2.Season season) {
        return switch (season) {
            case SPRING -> ChatColor.GREEN.toString();
            case SUMMER -> ChatColor.YELLOW.toString();
            case AUTUMN -> ChatColor.GOLD.toString();
            case WINTER -> ChatColor.AQUA.toString();
        };
    }

    private static String toTitleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}
