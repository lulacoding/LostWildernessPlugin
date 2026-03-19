package com.lostwilderness.rpgcore.progression;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Category-based, paginated milestones GUI. Main menu shows categories; each category
 * opens a paged list of milestones (7 per page). Clicking a milestone runs /v2claim and closes.
 */
public final class MilestonesMenu implements Listener {

    private static final String TITLE_MAIN = ChatColor.GOLD + "Milestones";
    private static final String TITLE_PREFIX = ChatColor.GOLD + "Milestones " + ChatColor.DARK_GRAY + "> ";
    private static final int ROWS = 3;
    private static final int SIZE = ROWS * 9;
    private static final int SLOT_PREV = 0;
    private static final int SLOT_BACK = 4;
    private static final int SLOT_NEXT = 8;
    private static final int SLOT_FIRST_ITEM = 10;
    private static final int ITEMS_PER_PAGE = 7;
    private static final int SLOT_LAST_ITEM = SLOT_FIRST_ITEM + ITEMS_PER_PAGE - 1;

    private final Plugin plugin;

    public MilestonesMenu(Plugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player) {
        openMain(player);
    }

    private void openMain(Player player) {
        Inventory inv = Bukkit.createInventory(player, SIZE, TITLE_MAIN);
        int slot = 10;
        for (Category cat : Category.ALL) {
            inv.setItem(slot++, makeButton(cat.icon, ChatColor.YELLOW + cat.displayName,
                    List.of(ChatColor.GRAY + "" + cat.milestones.size() + " milestone(s)", ChatColor.GREEN + "Click to open.")));
        }
        player.openInventory(inv);
    }

    private void openCategory(Player player, Category category, int pageIndex) {
        List<MilestoneEntry> entries = category.milestones;
        int totalPages = Math.max(1, (entries.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
        int page = Math.max(0, Math.min(pageIndex, totalPages - 1));
        String title = TITLE_PREFIX + ChatColor.WHITE + category.displayName;
        if (totalPages > 1) {
            title += ChatColor.GRAY + " (" + (page + 1) + "/" + totalPages + ")";
        }
        Inventory inv = Bukkit.createInventory(player, SIZE, title);
        int start = page * ITEMS_PER_PAGE;
        for (int i = 0; i < ITEMS_PER_PAGE && (start + i) < entries.size(); i++) {
            MilestoneEntry e = entries.get(start + i);
            inv.setItem(SLOT_FIRST_ITEM + i, makeButton(e.material, e.displayName, e.lore));
        }
        if (page > 0) {
            inv.setItem(SLOT_PREV, makeButton(Material.ARROW, ChatColor.GRAY + "Previous page", List.of()));
        }
        inv.setItem(SLOT_BACK, makeButton(Material.BARRIER, ChatColor.RED + "Back to categories", List.of()));
        if (page < totalPages - 1) {
            inv.setItem(SLOT_NEXT, makeButton(Material.ARROW, ChatColor.GRAY + "Next page", List.of()));
        }
        player.openInventory(inv);
    }

    private static ItemStack makeButton(Material type, String name, List<String> lore) {
        ItemStack stack = new ItemStack(type);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(lore != null && !lore.isEmpty() ? new ArrayList<>(lore) : null);
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        return stack;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        String title = event.getView().getTitle();

        if (title.equals(TITLE_MAIN)) {
            int rawSlot = event.getRawSlot();
            if (rawSlot >= 0 && rawSlot < SIZE) {
                event.setCancelled(true);
                int catIndex = rawSlot - 10;
                if (catIndex >= 0 && catIndex < Category.ALL.size()) {
                    openCategory(player, Category.ALL.get(catIndex), 0);
                }
            }
            return;
        }

        if (!title.startsWith(TITLE_PREFIX)) {
            return;
        }

        int rawSlot = event.getRawSlot();
        if (rawSlot >= 0 && rawSlot < SIZE) {
            event.setCancelled(true);
            String rest = ChatColor.stripColor(title).replace("Milestones > ", "").trim();
            int pageIndex = 0;
            if (rest.matches(".* \\(\\d+/\\d+\\)")) {
                int start = rest.lastIndexOf('(');
                String num = rest.substring(start + 1, rest.indexOf('/', start)).trim();
                try {
                    pageIndex = Integer.parseInt(num) - 1;
                } catch (NumberFormatException ignored) {}
                rest = rest.substring(0, start).trim();
            }
            Category category = null;
            for (Category c : Category.ALL) {
                if (rest.startsWith(c.displayName)) {
                    category = c;
                    break;
                }
            }
            if (category == null) return;

            if (rawSlot == SLOT_BACK) {
                openMain(player);
                return;
            }
            if (rawSlot == SLOT_PREV && pageIndex > 0) {
                openCategory(player, category, pageIndex - 1);
                return;
            }
            if (rawSlot == SLOT_NEXT) {
                int totalPages = Math.max(1, (category.milestones.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE);
                if (pageIndex < totalPages - 1) {
                    openCategory(player, category, pageIndex + 1);
                }
                return;
            }
            if (rawSlot >= SLOT_FIRST_ITEM && rawSlot <= SLOT_LAST_ITEM) {
                int index = pageIndex * ITEMS_PER_PAGE + (rawSlot - SLOT_FIRST_ITEM);
                if (index < category.milestones.size()) {
                    String claimKey = category.milestones.get(index).claimKey;
                    plugin.getServer().dispatchCommand(player, "v2claim " + claimKey.toLowerCase(Locale.ROOT));
                    player.closeInventory();
                }
            }
        }
    }

    private static final class MilestoneEntry {
        final String claimKey;
        final String displayName;
        final List<String> lore;
        final Material material;

        MilestoneEntry(String claimKey, String displayName, List<String> lore, Material material) {
            this.claimKey = claimKey;
            this.displayName = displayName;
            this.lore = lore;
            this.material = material;
        }
    }

    private static final class Category {
        final String displayName;
        final Material icon;
        final List<MilestoneEntry> milestones;

        Category(String displayName, Material icon, List<MilestoneEntry> milestones) {
            this.displayName = displayName;
            this.icon = icon;
            this.milestones = milestones;
        }

        static final List<Category> ALL = List.of(
            new Category("Join & Loyalty", Material.BOOK, List.of(
                entry("first_join", "First Join", "50 Fighting XP", Material.PAPER),
                entry("join_3_times", "Join 3 Times", "25 Fighting XP", Material.GOLD_INGOT),
                entry("join_10_times", "Join 10 Times", "100 Fighting XP", Material.EMERALD),
                entry("join_25_times", "Join 25 Times", "50 Fighting XP", Material.GOLDEN_APPLE),
                entry("join_50_times", "Join 50 Times", "75 Fighting XP", Material.ENCHANTED_GOLDEN_APPLE),
                entry("join_100_times", "Join 100 Times", "150 Fighting XP", Material.NETHER_STAR)
            )),
            new Category("Playtime", Material.CLOCK, List.of(
                entry("playtime_1h", "1 Hour Online", "25 Fighting XP", Material.IRON_INGOT),
                entry("playtime_5h", "5 Hours Online", "50 Fighting XP", Material.GOLD_INGOT),
                entry("playtime_24h", "24 Hours Online", "100 Fighting XP", Material.DIAMOND),
                entry("playtime_100h", "100 Hours Online", "250 Fighting XP", Material.EMERALD)
            )),
            new Category("Seasons", Material.SUNFLOWER, List.of(
                entry("first_season_spring", "First Spring", "30 Farming XP", Material.PINK_TULIP),
                entry("first_season_summer", "First Summer", "30 Farming XP", Material.SUNFLOWER),
                entry("first_season_autumn", "First Autumn", "30 Farming XP", Material.OAK_LEAVES),
                entry("first_season_winter", "First Winter", "30 Farming XP", Material.SNOWBALL),
                entry("new_year_login", "New Year Login", "100 Fighting XP", Material.FIREWORK_ROCKET)
            )),
            new Category("Events", Material.BEACON, List.of(
                // Placeholder for future: eclipse_survivor, festival_participant, etc.
            )),
            new Category("Deaths", Material.SKELETON_SKULL, List.of(
                entry("first_death", "First Death", "10 Fighting XP", Material.BONE),
                entry("deaths_10", "10 Deaths", "25 Fighting XP", Material.SKELETON_SKULL)
            )),
            new Category("Clans", Material.WHITE_BANNER, List.of(
                entry("joined_clan", "Joined a Clan", "50 Fighting XP", Material.WHITE_BANNER)
            ))
        );

        private static MilestoneEntry entry(String claimKey, String displayName, String rewardLine, Material material) {
            return new MilestoneEntry(claimKey, ChatColor.YELLOW + displayName,
                    List.of(ChatColor.GRAY + rewardLine, ChatColor.GREEN + "Click to claim."), material);
        }
    }
}
