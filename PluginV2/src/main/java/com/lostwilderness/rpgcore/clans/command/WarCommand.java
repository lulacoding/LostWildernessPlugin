package com.lostwilderness.rpgcore.clans.command;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.WarService;
import com.lostwilderness.rpgcore.clans.model.Clan;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /war declare <clan> | ceasefire [clan] | status
 * Requires leader rank; permission lw.clan.use (same as clan).
 */
public final class WarCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "lw.clan.use";

    private final ClanService clanService;
    private final WarService warService;

    public WarCommand(ClanService clanService, WarService warService) {
        this.clanService = clanService;
        this.warService = warService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players can use war commands.");
            return true;
        }
        if (!p.hasPermission(PERMISSION)) {
            p.sendMessage(ChatColor.RED + "You don't have permission to use war commands.");
            return true;
        }
        if (args.length < 1) {
            sendUsage(p);
            return true;
        }

        UUID myClanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (myClanId == null) {
            p.sendMessage(ChatColor.RED + "You must be in a clan to use war commands.");
            return true;
        }
        if (!"Leader".equals(clanService.getMemberRank(myClanId, p.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Only clan leaders can declare war or ceasefire.");
            return true;
        }

        String sub = args[0].toLowerCase();
        try {
            switch (sub) {
                case "declare" -> handleDeclare(p, args);
                case "ceasefire", "end" -> handleCeasefire(p, args);
                case "status" -> handleStatus(p);
                default -> sendUsage(p);
            }
        } catch (RuntimeException e) {
            p.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
        return true;
    }

    private void handleDeclare(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage(ChatColor.GRAY + "Usage: /war declare <clan name>");
            return;
        }
        String targetName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Clan target = clanService.getClanByName(targetName);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Clan not found: " + targetName);
            return;
        }
        if (target.getId().equals(clanService.getClanOfPlayer(p.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "You cannot declare war on your own clan.");
            return;
        }
        warService.declareWar(clanService.getClanOfPlayer(p.getUniqueId()), target.getId());
        p.sendMessage(ChatColor.RED + "War declared on " + target.getName() + ".");
    }

    private void handleCeasefire(Player p, String[] args) {
        UUID myClanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (args.length >= 2) {
            String targetName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            Clan target = clanService.getClanByName(targetName);
            if (target == null) {
                p.sendMessage(ChatColor.RED + "Clan not found: " + targetName);
                return;
            }
            if (!warService.areAtWar(myClanId, target.getId())) {
                p.sendMessage(ChatColor.GRAY + "Your clan is not at war with " + target.getName() + ".");
                return;
            }
            warService.ceasefire(myClanId, target.getId());
            p.sendMessage(ChatColor.GREEN + "Ceasefire with " + target.getName() + ".");
        } else {
            List<UUID[]> wars = warService.getActiveWars();
            for (UUID[] pair : wars) {
                if (pair[0].equals(myClanId) || pair[1].equals(myClanId)) {
                    UUID other = pair[0].equals(myClanId) ? pair[1] : pair[0];
                    warService.ceasefire(myClanId, other);
                    Clan otherClan = clanService.getClanById(other);
                    p.sendMessage(ChatColor.GREEN + "Ceasefire with " + (otherClan != null ? otherClan.getName() : other) + ".");
                    return;
                }
            }
            p.sendMessage(ChatColor.GRAY + "Your clan has no active wars. Use /war ceasefire <clan> to specify.");
        }
    }

    private void handleStatus(Player p) {
        List<UUID[]> wars = warService.getActiveWars();
        UUID myClanId = clanService.getClanOfPlayer(p.getUniqueId());
        List<String> atWarWith = new ArrayList<>();
        for (UUID[] pair : wars) {
            if (pair[0].equals(myClanId) || pair[1].equals(myClanId)) {
                UUID other = pair[0].equals(myClanId) ? pair[1] : pair[0];
                Clan c = clanService.getClanById(other);
                atWarWith.add(c != null ? c.getName() : other.toString());
            }
        }
        if (atWarWith.isEmpty()) {
            p.sendMessage(ChatColor.GREEN + "Your clan has no active wars.");
        } else {
            p.sendMessage(ChatColor.RED + "Your clan is at war with: " + String.join(", ", atWarWith));
        }
    }

    private void sendUsage(Player p) {
        p.sendMessage(ChatColor.GRAY + "Usage: /war declare <clan> | ceasefire [clan] | status");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission(PERMISSION)) return Collections.emptyList();
        if (args.length == 1) {
            return List.of("declare", "ceasefire", "end", "status").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length >= 2 && ("declare".equals(args[0].toLowerCase()) || "ceasefire".equals(args[0].toLowerCase()) || "end".equals(args[0].toLowerCase()))) {
            UUID myClanId = clanService.getClanOfPlayer(((Player) sender).getUniqueId());
            String partial = args.length == 2 ? args[1].toLowerCase() : "";
            return clanService.getAllClans().stream()
                .filter(c -> !c.getId().equals(myClanId))
                .map(Clan::getName)
                .filter(n -> n.toLowerCase().startsWith(partial))
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
