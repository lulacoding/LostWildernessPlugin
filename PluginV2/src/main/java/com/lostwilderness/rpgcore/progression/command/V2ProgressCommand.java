package com.lostwilderness.rpgcore.progression.command;

import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

/**
 * /v2progress – lists unlocked progression milestones for the executing player.
 */
public final class V2ProgressCommand implements CommandExecutor {

    private final ProgressionService progressionService;
    private final Plugin plugin;

    public V2ProgressCommand(ProgressionService progressionService, Plugin plugin) {
        this.progressionService = progressionService;
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(ChatColor.RED + "Only players can use this command.");
                return true;
            }
            UUID uuid = player.getUniqueId();
            List<String> keys = progressionService.getUnlockedKeys(uuid);
            if (keys.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "Unlocked milestones: none yet.");
            } else {
                player.sendMessage(ChatColor.GREEN + "Unlocked milestones: " + String.join(", ", keys));
            }
        } catch (Throwable t) {
            plugin.getLogger().log(Level.WARNING, "v2progress failed", t);
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + "Progression error. Check console.");
            }
        }
        return true;
    }
}
