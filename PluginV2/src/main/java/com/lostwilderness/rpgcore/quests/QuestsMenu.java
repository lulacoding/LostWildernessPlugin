package com.lostwilderness.rpgcore.quests;

import com.lostwilderness.rpgcore.progression.BetonQuestBridge;
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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * /quests chest GUI — 54-slot read-only display of NPC quest progress.
 *
 * Layout (slots 0–53, 9 per row):
 *   Row 0 (0–8):  Elder icon | spacer | q1 | q2 | q3 | q4 | spacer | spacer | spacer
 *   Row 1 (9–17): Blacksmith icon | spacer | q1 | q2 | q3 | spacer | spacer | spacer | spacer
 *   Row 2 (18–26): Herbalist icon | spacer | q1 | q2 | spacer | spacer | spacer | spacer | spacer
 *   Row 3 (27–35): Shrine icon | spacer | q1 | spacer | … | spacer | spacer | spacer | spacer
 *   Row 4 (36–44): Professor icon | spacer | q1 | spacer | … | spacer | spacer | spacer | spacer
 *   Row 5 (45–53): glass pane border
 */
public class QuestsMenu implements Listener {

    private static final String TITLE = ChatColor.DARK_PURPLE + "Quest Progress";

    private final BetonQuestBridge bq;

    public QuestsMenu(BetonQuestBridge bq) {
        this.bq = bq;
    }

    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, TITLE);
        UUID uuid = player.getUniqueId();

        // ── Row 0: Village Elder ──────────────────────────────────────────────
        inv.setItem(0, npcIcon(Material.VILLAGER_SPAWN_EGG, "&6Village Elder",
                List.of("&7Intro + starter kit", "&7Total XP: &e1400")));
        inv.setItem(2, questStep(uuid, "lw_elder.elder_intro_done",
                "&aElder: Intro & Kit", List.of("&7Talk to the Elder and receive a starter kit.", "&eReward: intro kit")));
        inv.setItem(3, questStep(uuid, "lw_elder.elder_q2_done",
                "&aElder: Pickaxe", List.of("&7Craft a wooden pickaxe.", "&eReward: 200 XP")));
        inv.setItem(4, questStep(uuid, "lw_elder.elder_q3_done",
                "&aElder: First Blood", List.of("&7Kill 10 mobs.", "&eReward: 400 XP")));
        inv.setItem(5, questStep(uuid, "lw_elder.elder_q4_done",
                "&aElder: Enter Nether", List.of("&7Venture into the Nether.", "&eReward: 800 XP")));

        // ── Row 1: Blacksmith ─────────────────────────────────────────────────
        inv.setItem(9, npcIcon(Material.FURNACE, "&6Blacksmith",
                List.of("&7Crafting & combat quests", "&7Total XP: &e1300")));
        inv.setItem(11, questStep(uuid, "lw_blacksmith.smith_intro_done",
                "&aSmith: Gather Iron", List.of("&7Collect 32 iron ore.", "&eReward: 300 XP")));
        inv.setItem(12, questStep(uuid, "lw_blacksmith.smith_q1_done",
                "&aSmith: Craft Sword", List.of("&7Craft an iron sword.", "&eReward: 400 XP")));
        inv.setItem(13, questStep(uuid, "lw_blacksmith.smith_q2_done",
                "&aSmith: Kill 5 Mobs", List.of("&7Kill 5 mobs with your sword.", "&eReward: 600 XP")));

        // ── Row 2: Herbalist ──────────────────────────────────────────────────
        inv.setItem(18, npcIcon(Material.FLOWER_POT, "&6Herbalist",
                List.of("&7Alchemy quests", "&7Total XP: &e750")));
        inv.setItem(20, questStep(uuid, "lw_herbalist.herb_intro_done",
                "&aHerb: Mushroom Stew", List.of("&7Craft a mushroom stew.", "&eReward: 250 XP")));
        inv.setItem(21, questStep(uuid, "lw_herbalist.herb_q1_done",
                "&aHerb: Healing Potion", List.of("&7Brew a Potion of Healing.", "&eReward: 500 XP")));

        // ── Row 3: Shrine Keeper ──────────────────────────────────────────────
        inv.setItem(27, npcIcon(Material.CONDUIT, "&6Shrine Keeper",
                List.of("&7Grants zodiac charm", "&7Total XP: &e0")));
        inv.setItem(29, questStep(uuid, "lw_shrine.shrine_visited",
                "&aShrine: Visit the Shrine", List.of("&7Find and activate the shrine.", "&eReward: Zodiac Charm")));

        // ── Row 4: Professor Craft ────────────────────────────────────────────
        inv.setItem(36, npcIcon(Material.BOOKSHELF, "&6Professor Craft",
                List.of("&7Hidden lab discovery", "&7Total XP: &e0")));
        inv.setItem(38, questStep(uuid, "lw_professor.prof_met",
                "&aProf: Find the Lab", List.of("&7Discover Professor Craft's laboratory.", "&eReward: story progress")));

        // ── Row 5: border ─────────────────────────────────────────────────────
        ItemStack pane = makeItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 45; slot < 54; slot++) {
            inv.setItem(slot, pane);
        }

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (TITLE.equals(event.getView().getTitle())) {
            event.setCancelled(true);
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private ItemStack questStep(UUID uuid, String fullTag, String name, List<String> lore) {
        boolean done = bq.hasTag(uuid, fullTag);
        // "In progress" detection not possible from tags alone — show yellow for first
        // incomplete step in a chain. For simplicity: done = green, not done = grey.
        Material mat = done ? Material.LIME_DYE : Material.GRAY_DYE;
        String prefix = done ? ChatColor.GREEN + "✔ " : ChatColor.GRAY + "○ ";
        String displayName = prefix + ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', name));
        List<String> coloredLore = lore.stream()
                .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                .collect(java.util.stream.Collectors.toList());
        if (done) coloredLore.add(ChatColor.GREEN + "Complete!");
        return makeItem(mat, displayName, coloredLore);
    }

    private ItemStack npcIcon(Material mat, String name, List<String> lore) {
        return makeItem(mat,
                ChatColor.translateAlternateColorCodes('&', "&6&l" + ChatColor.stripColor(
                        ChatColor.translateAlternateColorCodes('&', name))),
                lore.stream()
                        .map(l -> ChatColor.translateAlternateColorCodes('&', l))
                        .collect(java.util.stream.Collectors.toList()));
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
}
