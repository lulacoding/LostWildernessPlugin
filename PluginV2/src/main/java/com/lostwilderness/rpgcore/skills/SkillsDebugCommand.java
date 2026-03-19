package com.lostwilderness.rpgcore.skills;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * /v2skills – simple debug command to inspect AuraSkills levels for the player.
 * Currently focuses on the fighting skill, which is used for milestone rewards.
 */
public final class SkillsDebugCommand implements CommandExecutor {

    private final AuraSkillsBridge auraBridge;

    public SkillsDebugCommand(AuraSkillsBridge auraBridge) {
        this.auraBridge = auraBridge;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }
        if (auraBridge == null || !auraBridge.isAvailable()) {
            sender.sendMessage(ChatColor.RED + "AuraSkills bridge is not available right now.");
            return true;
        }
        UUID uuid = player.getUniqueId();
        int fightingLevel = auraBridge.getSkillLevel(uuid, "fighting");
        sender.sendMessage(ChatColor.GOLD + "[V2] AuraSkills debug:");
        sender.sendMessage(ChatColor.YELLOW + "  Fighting level: " + ChatColor.WHITE + fightingLevel);
        sender.sendMessage(ChatColor.DARK_GRAY + "(Milestone rewards grant Fighting XP via /v2claim)");
        return true;
    }
}

