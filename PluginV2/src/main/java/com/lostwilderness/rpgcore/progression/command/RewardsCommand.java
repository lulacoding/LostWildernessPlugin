package com.lostwilderness.rpgcore.progression.command;

import com.lostwilderness.rpgcore.progression.MilestonesMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * /rewards and /milestones – open the built-in milestones GUI.
 * Journal stays separate for BetonQuest; this command is for the milestones GUI
 * only.
 */
public final class RewardsCommand implements CommandExecutor {

    private final MilestonesMenu menu;

    public RewardsCommand(MilestonesMenu menu) {
        this.menu = menu;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can open the rewards menu.");
            return true;
        }
        menu.open(player);
        return true;
    }
}
