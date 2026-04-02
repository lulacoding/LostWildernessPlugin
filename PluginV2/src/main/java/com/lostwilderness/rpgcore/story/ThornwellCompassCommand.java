package com.lostwilderness.rpgcore.story;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

/**
 * {@code /v2thornwellcompass give <player>} — used by BetonQuest (console) when Amos gives the compass.
 */
public final class ThornwellCompassCommand implements CommandExecutor {

    private final Plugin plugin;
    private final StoryCompassSettings settings;

    public ThornwellCompassCommand(Plugin plugin, StoryCompassSettings settings) {
        this.plugin = plugin;
        this.settings = settings;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player
                && !player.hasPermission("rpgcore.thornwellcompass")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }
        if (args.length < 2 || !"give".equalsIgnoreCase(args[0])) {
            sender.sendMessage("Usage: /v2thornwellcompass give <player>");
            return true;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage("Player not found: " + args[1]);
            return true;
        }
        ItemStack stack = ThornwellCompassItem.create(plugin, settings);
        if (stack == null) {
            sender.sendMessage("World '" + settings.getWorldName() + "' not loaded — cannot build compass.");
            plugin.getLogger().warning("[story] Thornwell compass: world missing: " + settings.getWorldName());
            return true;
        }
        var leftover = target.getInventory().addItem(stack);
        for (ItemStack drop : leftover.values()) {
            target.getWorld().dropItemNaturally(target.getLocation(), drop);
        }
        return true;
    }
}
