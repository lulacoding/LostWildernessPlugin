package com.lostwilderness.rpgcore.boss.command;

import com.lostwilderness.rpgcore.boss.ArenaManager;
import com.lostwilderness.rpgcore.boss.BossArena;
import com.lostwilderness.rpgcore.boss.Boundary;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ArenaTestCommand implements CommandExecutor {

    private final ArenaManager manager;

    public ArenaTestCommand(ArenaManager manager) {
        this.manager = manager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        if (args.length < 1) {
            player.sendMessage(ChatColor.RED + "Usage: /arena-test <create|show|clear>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create":
                Location loc = player.getLocation();
                Boundary boundary = new Boundary(player.getWorld(),
                        loc.getBlockX() - 10, loc.getBlockY() - 2, loc.getBlockZ() - 10,
                        loc.getBlockX() + 10, loc.getBlockY() + 10, loc.getBlockZ() + 10);
                BossArena arena = new BossArena("test-" + player.getName(), "Test Arena", boundary, true, false);
                manager.register(arena);
                player.sendMessage(ChatColor.GREEN + "Created restricted test arena around you.");
                break;
            case "show":
                manager.getAll().forEach(a -> a.getBoundary().showEdges(2));
                player.sendMessage(ChatColor.YELLOW + "Showing arena edges.");
                break;
            case "clear":
                manager.clear();
                player.sendMessage(ChatColor.RED + "Cleared all arenas.");
                break;
            default:
                player.sendMessage(ChatColor.RED + "Unknown sub-command.");
        }

        return true;
    }
}
