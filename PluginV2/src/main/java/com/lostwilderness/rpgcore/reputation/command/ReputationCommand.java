package com.lostwilderness.rpgcore.reputation.command;

import com.lostwilderness.rpgcore.reputation.Faction;
import com.lostwilderness.rpgcore.reputation.ReputationService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ReputationCommand implements CommandExecutor {

    private final ReputationService service;

    public ReputationCommand(ReputationService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        service.getHonorScore(player.getUniqueId()).thenAccept(score -> {
            String title = service.getHonorTitle(score);
            ChatColor color = getHonorColor(score);

            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "=== Your Reputation ===");
            player.sendMessage(ChatColor.YELLOW + "Honor Score: " + color + score + ChatColor.GRAY + " (" + title + ")");
            
            // Build progress bar
            String bar = buildHonorBar(score);
            player.sendMessage(ChatColor.GRAY + "[" + bar + ChatColor.GRAY + "]");

            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Faction Standing:");
            for (Faction faction : Faction.values()) {
                int points = service.getPoints(player.getUniqueId(), faction);
                if (points > 0) {
                    player.sendMessage(ChatColor.translateAlternateColorCodes('&', 
                        " &7- " + faction.getColor() + faction.getDisplayName() + ": &e" + points));
                }
            }
            player.sendMessage("");
        });

        return true;
    }

    private ChatColor getHonorColor(int score) {
        if (score >= 400) return ChatColor.GREEN;
        if (score > -400) return ChatColor.WHITE;
        return ChatColor.RED;
    }

    private String buildHonorBar(int score) {
        // -1000 to 1000. 20 segments. Each segment = 100 points.
        // Center is 0.
        StringBuilder sb = new StringBuilder();
        int segments = 20;
        int center = segments / 2;
        int activeSegment = (score + 1000) / 100;
        
        for (int i = 0; i < segments; i++) {
            if (i == activeSegment) {
                sb.append(ChatColor.GOLD).append("┃");
            } else if (i < center) {
                sb.append(ChatColor.RED).append("•");
            } else if (i > center) {
                sb.append(ChatColor.GREEN).append("•");
            } else {
                sb.append(ChatColor.WHITE).append("⊹");
            }
        }
        return sb.toString();
    }
}
