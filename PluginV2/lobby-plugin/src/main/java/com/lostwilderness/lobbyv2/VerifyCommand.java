package com.lostwilderness.lobbyv2;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class VerifyCommand implements CommandExecutor {
    private final VerificationRepository repo;

    public VerifyCommand(VerificationRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can run /verify.");
            return true;
        }
        String name = player.getName();
        if (repo.isVerified(name)) {
            player.sendMessage("§aYou are already verified. Please pick your class and step through the portal!");
            return true;
        }
        if (repo.addPendingVerification(name)) {
            player.sendMessage("§eVerification started. Join our Discord server and run:");
            player.sendMessage("§b/verify " + name);
            player.sendMessage("§7Then return here, pick your class via the NPCs, and go through the portal.");
        } else {
            player.sendMessage("§eYou already have a pending verification. In Discord run:");
            player.sendMessage("§b/verify " + name);
        }
        return true;
    }
}
