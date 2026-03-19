package com.lostwilderness.rpgcore.boss.command;

import com.lostwilderness.rpgcore.boss.Boundary;
import com.lostwilderness.rpgcore.boss.DiabloLairGenerator;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class DiabloLairCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (!player.hasPermission("rpgcore.admin.diablolair")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        Location loc = player.getLocation();
        Boundary boundary = DiabloLairGenerator.generatePlatform(player.getWorld(), 
                loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ(), 20);
        
        player.sendMessage(ChatColor.DARK_RED + "Diablo's platform generated below you (" + 
                boundary.getMinX() + "," + boundary.getMinZ() + ") to (" + 
                boundary.getMaxX() + "," + boundary.getMaxZ() + ")");
        
        return true;
    }
}
