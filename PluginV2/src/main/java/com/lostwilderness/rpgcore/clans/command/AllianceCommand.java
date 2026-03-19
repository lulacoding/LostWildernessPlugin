package com.lostwilderness.rpgcore.clans.command;

import com.lostwilderness.rpgcore.clans.AllianceService;
import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.model.Clan;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /alliance invite <clan> | accept [clan] | leave <clan> | info | list
 * Permission lw.clan.use. Leader for invite/accept/leave.
 */
public final class AllianceCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "lw.clan.use";

    private final ClanService clanService;
    private final AllianceService allianceService;

    public AllianceCommand(ClanService clanService, AllianceService allianceService) {
        this.clanService = clanService;
        this.allianceService = allianceService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(ChatColor.RED + "Only players can use alliance commands.");
            return true;
        }
        if (!p.hasPermission(PERMISSION)) {
            p.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }
        if (args.length < 1) {
            sendUsage(p);
            return true;
        }

        UUID myClanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (myClanId == null) {
            p.sendMessage(ChatColor.RED + "You must be in a clan.");
            return true;
        }

        String sub = args[0].toLowerCase();
        try {
            switch (sub) {
                case "invite" -> handleInvite(p, args, myClanId);
                case "accept" -> handleAccept(p, args, myClanId);
                case "leave" -> handleLeave(p, args, myClanId);
                case "info", "list" -> handleList(p, myClanId);
                default -> sendUsage(p);
            }
        } catch (RuntimeException e) {
            p.sendMessage(ChatColor.RED + "Error: " + e.getMessage());
        }
        return true;
    }

    private void handleInvite(Player p, String[] args, UUID myClanId) {
        if (!"Leader".equals(clanService.getMemberRank(myClanId, p.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Only clan leaders can invite to alliance.");
            return;
        }
        if (args.length < 2) {
            p.sendMessage(ChatColor.GRAY + "Usage: /alliance invite <clan name>");
            return;
        }
        String targetName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Clan target = clanService.getClanByName(targetName);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Clan not found: " + targetName);
            return;
        }
        if (target.getId().equals(myClanId)) {
            p.sendMessage(ChatColor.RED + "You cannot ally with yourself.");
            return;
        }
        allianceService.inviteToAlliance(myClanId, target.getId(), p.getUniqueId());
        p.sendMessage(ChatColor.GREEN + "Alliance invite sent to " + target.getName() + ".");
    }

    private void handleAccept(Player p, String[] args, UUID myClanId) {
        if (!"Leader".equals(clanService.getMemberRank(myClanId, p.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Only clan leaders can accept alliance invites.");
            return;
        }
        List<UUID> pending = allianceService.getPendingAllianceInvitesFor(myClanId);
        if (pending.isEmpty()) {
            p.sendMessage(ChatColor.GRAY + "Your clan has no pending alliance invites.");
            return;
        }
        UUID invitingClanId;
        if (args.length >= 2) {
            String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            Clan c = clanService.getClanByName(name);
            if (c == null || !pending.contains(c.getId())) {
                p.sendMessage(ChatColor.RED + "No pending invite from clan: " + name);
                return;
            }
            invitingClanId = c.getId();
        } else {
            invitingClanId = pending.get(0);
        }
        allianceService.acceptAllianceInvite(myClanId, invitingClanId);
        Clan c = clanService.getClanById(invitingClanId);
        p.sendMessage(ChatColor.GREEN + "Alliance formed with " + (c != null ? c.getName() : invitingClanId) + ".");
    }

    private void handleLeave(Player p, String[] args, UUID myClanId) {
        if (args.length < 2) {
            p.sendMessage(ChatColor.GRAY + "Usage: /alliance leave <clan name>");
            return;
        }
        if (!"Leader".equals(clanService.getMemberRank(myClanId, p.getUniqueId()))) {
            p.sendMessage(ChatColor.RED + "Only clan leaders can leave an alliance.");
            return;
        }
        String name = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Clan target = clanService.getClanByName(name);
        if (target == null) {
            p.sendMessage(ChatColor.RED + "Clan not found: " + name);
            return;
        }
        List<UUID> allies = allianceService.getAllies(myClanId);
        if (!allies.contains(target.getId())) {
            p.sendMessage(ChatColor.GRAY + "Your clan is not allied with " + target.getName() + ".");
            return;
        }
        allianceService.leaveAlliance(myClanId, target.getId());
        p.sendMessage(ChatColor.YELLOW + "Left alliance with " + target.getName() + ".");
    }

    private void handleList(Player p, UUID myClanId) {
        List<UUID> allies = allianceService.getAllies(myClanId);
        List<UUID> pending = allianceService.getPendingAllianceInvitesFor(myClanId);
        p.sendMessage(ChatColor.GOLD + "Alliances for your clan:");
        if (allies.isEmpty() && pending.isEmpty()) {
            p.sendMessage(ChatColor.GRAY + "  No allies or pending invites.");
            return;
        }
        for (UUID id : allies) {
            Clan c = clanService.getClanById(id);
            p.sendMessage(ChatColor.GREEN + "  Ally: " + (c != null ? c.getName() : id));
        }
        for (UUID id : pending) {
            Clan c = clanService.getClanById(id);
            p.sendMessage(ChatColor.YELLOW + "  Pending invite from: " + (c != null ? c.getName() : id));
        }
    }

    private void sendUsage(Player p) {
        p.sendMessage(ChatColor.GRAY + "Usage: /alliance invite <clan> | accept [clan] | leave <clan> | info | list");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission(PERMISSION)) return Collections.emptyList();
        if (args.length == 1) {
            return List.of("invite", "accept", "leave", "info", "list").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length >= 2) {
            UUID myClanId = clanService.getClanOfPlayer(((Player) sender).getUniqueId());
            if (myClanId == null) return Collections.emptyList();
            String partial = args.length == 2 ? args[1].toLowerCase() : "";
            if ("invite".equals(args[0].toLowerCase())) {
                return clanService.getAllClans().stream()
                    .filter(c -> !c.getId().equals(myClanId))
                    .map(Clan::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            }
            if ("leave".equals(args[0].toLowerCase())) {
                return allianceService.getAllies(myClanId).stream()
                    .map(id -> clanService.getClanById(id))
                    .filter(c -> c != null)
                    .map(Clan::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            }
            if ("accept".equals(args[0].toLowerCase())) {
                return allianceService.getPendingAllianceInvitesFor(myClanId).stream()
                    .map(id -> clanService.getClanById(id))
                    .filter(c -> c != null)
                    .map(Clan::getName)
                    .filter(n -> n.toLowerCase().startsWith(partial))
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
