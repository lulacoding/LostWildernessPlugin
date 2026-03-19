package com.lostwilderness.rpgcore.personality;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.*;

/**
 * Manages custom enchantments (Trait Enchants + Holy Enchants) via PDC.
 * Stores enchants in PersistentDataContainer and updates item lore for display.
 */
public class HolyEnchantService {

    private final Plugin plugin;
    private final NamespacedKey traitEnchantKey;
    private final NamespacedKey holyEnchantKey;

    public HolyEnchantService(Plugin plugin) {
        this.plugin = plugin;
        this.traitEnchantKey = new NamespacedKey(plugin, "trait_enchant");
        this.holyEnchantKey = new NamespacedKey(plugin, "holy_enchant");
    }

    // ========== Trait Enchants ==========

    /**
     * Apply trait enchant to item (one per item).
     */
    public void applyTraitEnchant(ItemStack item, PersonalityTrait trait) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        pdc.set(traitEnchantKey, PersistentDataType.STRING, trait.getTraitEnchant());

        updateLore(item, meta);
        item.setItemMeta(meta);
    }

    /**
     * Get trait enchant from item (or null if none).
     */
    public String getTraitEnchant(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        return pdc.get(traitEnchantKey, PersistentDataType.STRING);
    }

    /**
     * Check if item has a trait enchant.
     */
    public boolean hasTraitEnchant(ItemStack item) {
        return getTraitEnchant(item) != null;
    }

    // ========== Holy Enchants ==========

    /**
     * Apply holy enchant to item (can have multiple).
     */
    public void applyHolyEnchant(ItemStack item, HolyEnchant enchant) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        // Get existing holy enchants
        Set<HolyEnchant> existing = getHolyEnchants(item);
        existing.add(enchant);

        // Store as comma-separated string
        String value = existing.stream()
            .map(HolyEnchant::name)
            .reduce((a, b) -> a + "," + b)
            .orElse("");

        pdc.set(holyEnchantKey, PersistentDataType.STRING, value);

        updateLore(item, meta);
        item.setItemMeta(meta);
    }

    /**
     * Get all holy enchants from item.
     */
    public Set<HolyEnchant> getHolyEnchants(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return Set.of();

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String value = pdc.get(holyEnchantKey, PersistentDataType.STRING);
        if (value == null || value.isEmpty()) return new HashSet<>();

        Set<HolyEnchant> enchants = new HashSet<>();
        for (String name : value.split(",")) {
            try {
                enchants.add(HolyEnchant.valueOf(name.trim()));
            } catch (IllegalArgumentException e) {
                // Invalid enchant name, skip
            }
        }

        return enchants;
    }

    /**
     * Check if item has a specific holy enchant.
     */
    public boolean hasHolyEnchant(ItemStack item, HolyEnchant enchant) {
        return getHolyEnchants(item).contains(enchant);
    }

    // ========== Blessing ==========

    /**
     * Bless an item: double vanilla enchant levels + add random Holy Enchant.
     * Called by Lord command.
     */
    public void blessItem(ItemStack item, HolyEnchant randomEnchant) {
        if (item == null || !item.hasItemMeta()) return;

        ItemMeta meta = item.getItemMeta();

        // Double all vanilla enchant levels (up to hard cap)
        Map<Enchantment, Integer> enchants = new HashMap<>(meta.getEnchants());
        for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
            Enchantment ench = entry.getKey();
            int currentLevel = entry.getValue();
            int newLevel = Math.min(currentLevel * 2, ench.getMaxLevel() * 2); // Allow 2× vanilla max

            meta.removeEnchant(ench);
            meta.addEnchant(ench, newLevel, true); // Unsafe to allow beyond normal max
        }

        item.setItemMeta(meta);

        // Add Holy Enchant
        applyHolyEnchant(item, randomEnchant);
    }

    // ========== Lore Management ==========

    /**
     * Update item lore to display trait + holy enchants.
     */
    private void updateLore(ItemStack item, ItemMeta meta) {
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();

        // Remove existing enchant lore lines
        lore.removeIf(line -> line.contains("⚡ Trait Enchant:") || line.contains("⚡ Holy Enchant:"));

        // Add trait enchant lore
        String traitEnchant = getTraitEnchant(item);
        if (traitEnchant != null) {
            lore.add(ChatColor.LIGHT_PURPLE + "⚡ Trait Enchant: " + formatEnchantName(traitEnchant));
        }

        // Add holy enchant lore
        Set<HolyEnchant> holyEnchants = getHolyEnchants(item);
        for (HolyEnchant enchant : holyEnchants) {
            lore.add(enchant.getColoredName());
        }

        meta.setLore(lore);
    }

    /**
     * Format enchant name for display (e.g., MAGE_FOCUS → Mage Focus).
     */
    private String formatEnchantName(String name) {
        return Arrays.stream(name.split("_"))
            .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
            .reduce((a, b) -> a + " " + b)
            .orElse(name);
    }

    // ========== Utility ==========

    /**
     * Check if item is eligible for a holy enchant.
     */
    public boolean canApplyHolyEnchant(ItemStack item, HolyEnchant enchant) {
        return enchant.isApplicableTo(item.getType());
    }

    /**
     * Get a random applicable Holy Enchant for an item.
     */
    public HolyEnchant getRandomApplicableEnchant(ItemStack item) {
        return HolyEnchant.getRandomFor(item.getType());
    }
}
