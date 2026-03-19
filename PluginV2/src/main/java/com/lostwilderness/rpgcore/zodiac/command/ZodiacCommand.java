package com.lostwilderness.rpgcore.zodiac.command;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.zodiac.ZodiacProfile;
import com.lostwilderness.rpgcore.zodiac.ZodiacService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Command handler for /zodiac.
 * Subcommands: info, reveal, clanbonus
 */
public final class ZodiacCommand implements CommandExecutor {

    private final ZodiacService zodiacService;
    private final ClanService clanService;

    public ZodiacCommand(ZodiacService zodiacService, ClanService clanService) {
        this.zodiacService = zodiacService;
        this.clanService = clanService;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                           @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.GOLD + "=== Zodiac Commands ===");
            sender.sendMessage(ChatColor.YELLOW + "/zodiac info [player]" + ChatColor.GRAY + " - View zodiac signs");
            sender.sendMessage(ChatColor.YELLOW + "/zodiac reveal <player>" + ChatColor.GRAY + " - Reveal hidden signs (Lord only)");
            sender.sendMessage(ChatColor.YELLOW + "/zodiac clanbonus" + ChatColor.GRAY + " - Check clan leader year bonus");
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "info":
                return handleInfo(sender, args);
            case "reveal":
                return handleReveal(sender, args);
            case "clanbonus":
                return handleClanBonus(sender);
            default:
                sender.sendMessage(ChatColor.RED + "Unknown subcommand. Use /zodiac for help.");
                return true;
        }
    }

    private boolean handleInfo(CommandSender sender, String[] args) {
        Player target;

        if (args.length >= 2) {
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }
        } else {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Console must specify a player name.");
                return true;
            }
            target = (Player) sender;
        }

        Optional<ZodiacProfile> profileOpt = zodiacService.getZodiacProfile(target.getUniqueId());
        if (profileOpt.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No zodiac profile found for " + target.getName());
            return true;
        }

        ZodiacProfile profile = profileOpt.get();
        boolean isSelf = sender.equals(target);

        sender.sendMessage(ChatColor.GOLD + "=== Zodiac Info: " + target.getName() + " ===");
        sender.sendMessage(ChatColor.YELLOW + "Month Sign: " + ChatColor.WHITE +
            profile.monthSign().displayName() + " " + profile.monthSign().symbol());
        sender.sendMessage(ChatColor.YELLOW + "Year Sign: " + ChatColor.WHITE +
            profile.yearSign().displayName() + " " + profile.yearSign().symbol());

        if (profile.isEpochian()) {
            sender.sendMessage(ChatColor.LIGHT_PURPLE + "✦ Epochian (The Chosen) ✦");
            if (profile.secondSignRevealed() || isSelf) {
                sender.sendMessage(ChatColor.YELLOW + "Second Sign: " + ChatColor.WHITE +
                    profile.secondSign().displayName() + " " + profile.secondSign().symbol());
            } else {
                sender.sendMessage(ChatColor.GRAY + "Second Sign: ??? (Hidden)");
            }
        }

        if (profile.spiritAnimalRevealed() || isSelf) {
            sender.sendMessage(ChatColor.YELLOW + "Spirit Animal: " + ChatColor.WHITE +
                profile.spiritAnimal().displayName());
            if (isSelf) {
                sender.sendMessage(ChatColor.GRAY + "  " + profile.spiritAnimal().description());
            }
        } else {
            sender.sendMessage(ChatColor.GRAY + "Spirit Animal: Hidden");
        }

        // Show active bonuses
        if (zodiacService.isSignMonthActive(target.getUniqueId())) {
            sender.sendMessage(ChatColor.GREEN + "✓ Month sign is currently active!");
        }
        if (zodiacService.isSignYearActive(target.getUniqueId())) {
            sender.sendMessage(ChatColor.GREEN + "✓ Year sign is currently active!");
        }
        if (zodiacService.isSyncActive(target.getUniqueId())) {
            sender.sendMessage(ChatColor.GOLD + "★ SYNC BONUS ACTIVE ★");
        }

        return true;
    }

    private boolean handleReveal(CommandSender sender, String[] args) {
        if (!(sender instanceof Player lord)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (!lord.hasPermission("lw.rank.lord")) {
            sender.sendMessage(ChatColor.RED + "You must be a Lord to reveal hidden zodiac signs.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /zodiac reveal <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        // Reveal both second sign and spirit animal
        zodiacService.revealSecondSign(target.getUniqueId(), lord.getUniqueId());
        zodiacService.revealSpiritAnimal(target.getUniqueId(), lord.getUniqueId());

        return true;
    }

    private boolean handleClanBonus(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        UUID clanId = clanService.getClanOfPlayer(player.getUniqueId());
        if (clanId == null) {
            sender.sendMessage(ChatColor.RED + "You are not in a clan.");
            return true;
        }

        // Find clan leader
        List<Map.Entry<UUID, String>> members = clanService.getClanMembersWithRanks(clanId);
        UUID leaderUuid = members.stream()
            .filter(e -> "Leader".equals(e.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        if (leaderUuid == null) {
            sender.sendMessage(ChatColor.RED + "Clan has no leader.");
            return true;
        }

        String leaderName = Bukkit.getOfflinePlayer(leaderUuid).getName();
        boolean bonusActive = zodiacService.isClanLeaderYearBonusActive(leaderUuid);

        sender.sendMessage(ChatColor.GOLD + "=== Clan Leader Year Bonus ===");
        sender.sendMessage(ChatColor.YELLOW + "Leader: " + ChatColor.WHITE + leaderName);

        if (bonusActive) {
            sender.sendMessage(ChatColor.GREEN + "✓ Year bonus is ACTIVE!");
            sender.sendMessage(ChatColor.GRAY + "All clan members receive:");
            sender.sendMessage(ChatColor.GRAY + "  • Luck I");
            sender.sendMessage(ChatColor.GRAY + "  • +5% XP gain");
        } else {
            sender.sendMessage(ChatColor.GRAY + "Year bonus is not currently active.");
        }

        return true;
    }
}
