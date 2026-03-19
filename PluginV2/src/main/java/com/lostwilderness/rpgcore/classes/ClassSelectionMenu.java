package com.lostwilderness.rpgcore.classes;

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
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Simple one-page GUI that lets the player pick one of the lore classes.
 * Once chosen, the class is saved permanently (until an admin resets it).
 */
public final class ClassSelectionMenu implements Listener {

    private static final String TITLE = ChatColor.DARK_AQUA + "Choose Your Path";
    private static final int SIZE = 9 * 3;

    private final Plugin plugin;
    private final ClassService classService;
    private final Map<Integer, PlayerClass> slotToClass = new java.util.HashMap<>();

    public ClassSelectionMenu(Plugin plugin, ClassService classService) {
        this.plugin = plugin;
        this.classService = classService;
    }

    public void open(Player player) {
        classService.hasClass(player.getUniqueId()).thenAccept(has -> {
            runSync(() -> {
                if (!player.isOnline()) {
                    return;
                }
                if (has) {
                    player.sendMessage(ChatColor.RED + "You have already chosen a class.");
                    return;
                }
                Inventory inv = Bukkit.createInventory(player, SIZE, TITLE);

                setButton(inv, 10, PlayerClass.CELESTIAL_TEMPLAR,
                        Material.DIAMOND_SWORD,
                        ChatColor.GOLD + "Celestial Templar",
                        List.of(
                                ChatColor.GRAY + "Holy warrior, slayer of the corrupted.",
                                ChatColor.GREEN + "+ Bonus vs Undead and bosses",
                                ChatColor.GREEN + "+ Favors Celestial reputation"));

                setButton(inv, 12, PlayerClass.WILDLAND_RANGER,
                        Material.BOW,
                        ChatColor.GREEN + "Wildland Ranger",
                        List.of(
                                ChatColor.GRAY + "Hunter of the wilds, master of terrain.",
                                ChatColor.GREEN + "+ Faster in forests and jungles",
                                ChatColor.GREEN + "+ Favors Wildlands reputation"));

                setButton(inv, 14, PlayerClass.REDEEMED_ARTIFICER,
                        Material.IRON_PICKAXE,
                        ChatColor.AQUA + "Redeemed Artificer",
                        List.of(
                                ChatColor.GRAY + "Ancient engineer and master miner.",
                                ChatColor.GREEN + "+ Resource-focused bonuses",
                                ChatColor.GREEN + "+ Favors Celestial reputation"));

                setButton(inv, 16, PlayerClass.CORRUPTED_CULTIST,
                        Material.WITHER_ROSE,
                        ChatColor.DARK_PURPLE + "Corrupted Cultist",
                        List.of(
                                ChatColor.GRAY + "Servant of the void and darkness.",
                                ChatColor.RED + "+ Lifesteal & dark abilities",
                                ChatColor.RED + "+ Favors Corrupted reputation"));

                setButton(inv, 22, PlayerClass.DESTROYER_BERSERKER,
                        Material.NETHERITE_AXE,
                        ChatColor.RED + "Destroyer Berserker",
                        List.of(
                                ChatColor.GRAY + "Relentless war machine of chaos.",
                                ChatColor.RED + "+ Strong PvP focus",
                                ChatColor.RED + "+ Favors Destroyers reputation"));

                player.openInventory(inv);
            });
        }).exceptionally(ex -> {
            plugin.getLogger()
                    .warning("[classes] Failed to open class menu for " + player.getName() + ": " + ex.getMessage());
            runSync(() -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Could not open class menu right now. Please try again.");
                }
            });
            return null;
        });
    }

    private void setButton(Inventory inv, int slot, PlayerClass playerClass, Material material, String name,
            List<String> lore) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            meta.setLore(new ArrayList<>(lore));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS);
            stack.setItemMeta(meta);
        }
        inv.setItem(slot, stack);
        slotToClass.put(slot, playerClass);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        if (!TITLE.equals(event.getView().getTitle())) {
            return;
        }
        int rawSlot = event.getRawSlot();
        if (rawSlot < 0 || rawSlot >= event.getInventory().getSize()) {
            return;
        }
        event.setCancelled(true);
        PlayerClass chosen = slotToClass.get(rawSlot);
        if (chosen == null) {
            return;
        }
        UUID uuid = player.getUniqueId();
        classService.hasClass(uuid).thenAccept(has -> {
            if (has) {
                runSync(() -> {
                    if (!player.isOnline()) {
                        return;
                    }
                    player.sendMessage(ChatColor.RED + "You have already chosen a class.");
                    player.closeInventory();
                });
                return;
            }
            classService.setClass(uuid, chosen).thenRun(() -> runSync(() -> {
                if (!player.isOnline()) {
                    return;
                }
                player.sendMessage(
                        ChatColor.GOLD + "You are now a " + ChatColor.RESET + chosen.name() + ChatColor.GOLD + "!");
                player.closeInventory();
            })).exceptionally(ex -> {
                plugin.getLogger()
                        .warning("[classes] Failed to save class for " + player.getName() + ": " + ex.getMessage());
                runSync(() -> {
                    if (player.isOnline()) {
                        player.sendMessage(ChatColor.RED + "Could not save your class. Please try again.");
                    }
                });
                return null;
            });
        }).exceptionally(ex -> {
            plugin.getLogger().warning(
                    "[classes] Failed to verify existing class for " + player.getName() + ": " + ex.getMessage());
            runSync(() -> {
                if (player.isOnline()) {
                    player.sendMessage(ChatColor.RED + "Could not verify your class data. Please try again.");
                }
            });
            return null;
        });
    }

    private void runSync(Runnable action) {
        if (Bukkit.isPrimaryThread()) {
            action.run();
            return;
        }
        Bukkit.getScheduler().runTask(plugin, action);
    }
}
