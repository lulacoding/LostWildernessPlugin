package com.lostwilderness.rpgcore.story;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.List;

/**
 * Builds and detects the Amos Thornwell lodestone compass (PDC + {@link CompassMeta}).
 */
public final class ThornwellCompassItem {

    public static final String PDC_KEY = "thornwell_guide_compass";

    private ThornwellCompassItem() {}

    public static NamespacedKey key(Plugin plugin) {
        return new NamespacedKey(plugin, PDC_KEY);
    }

    public static ItemStack create(Plugin plugin, StoryCompassSettings settings) {
        World world = plugin.getServer().getWorld(settings.getWorldName());
        if (world == null) {
            return null;
        }
        Location lodestone = new Location(world,
                settings.getBlockX() + 0.5,
                settings.getBlockY(),
                settings.getBlockZ() + 0.5);
        ItemStack stack = new ItemStack(Material.COMPASS);
        CompassMeta meta = (CompassMeta) stack.getItemMeta();
        meta.setLodestone(lodestone);
        meta.setLodestoneTracked(true);
        meta.displayName(Component.text("Lodestone Compass", NamedTextColor.GOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("Points to Thornwell", NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text("Given by a traveller at the campsite", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false),
                Component.empty(),
                Component.text("Follow the road northeast.", NamedTextColor.DARK_GRAY)
                        .decoration(TextDecoration.ITALIC, false)
        ));
        meta.getPersistentDataContainer().set(key(plugin), PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
        return stack;
    }

    public static boolean isThornwellCompass(Plugin plugin, ItemStack stack) {
        if (stack == null || stack.getType() != Material.COMPASS || !stack.hasItemMeta()) {
            return false;
        }
        Byte mark = stack.getItemMeta().getPersistentDataContainer().get(key(plugin), PersistentDataType.BYTE);
        return mark != null && mark == 1;
    }

    /**
     * Removes every Thornwell guide compass from the player's inventory and equipment (best-effort).
     */
    public static int removeAllFrom(Plugin plugin, org.bukkit.entity.Player player) {
        int removed = 0;
        org.bukkit.inventory.PlayerInventory inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack slot = inv.getItem(i);
            if (isThornwellCompass(plugin, slot)) {
                inv.setItem(i, null);
                removed += slot.getAmount();
            }
        }
        ItemStack off = inv.getItemInOffHand();
        if (isThornwellCompass(plugin, off)) {
            removed += off.getAmount();
            inv.setItemInOffHand(null);
        }
        return removed;
    }
}
