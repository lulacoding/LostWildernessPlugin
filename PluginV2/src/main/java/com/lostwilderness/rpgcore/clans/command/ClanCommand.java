package com.lostwilderness.rpgcore.clans.command;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.model.Clan;
import com.lostwilderness.rpgcore.clans.model.Invite;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * /clan create|invite|accept|deny|leave|color|promote|demote|enemy|opposition|info|list
 * Permission: lw.clan.use
 */
public final class ClanCommand implements CommandExecutor, TabCompleter {

    private static final String PERMISSION = "lw.clan.use";

    private final ClanService clanService;

    public ClanCommand(ClanService clanService) {
        this.clanService = clanService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cOnly players can use clan commands.");
            return true;
        }
        if (!p.hasPermission(PERMISSION)) {
            p.sendMessage("§cYou don't have permission to use clan commands.");
            return true;
        }
        if (args.length < 1) {
            sendHelpMessage(p);
            return true;
        }

        try {
            String sub = args[0].toLowerCase();
            switch (sub) {
                case "create" -> handleCreate(p, args);
                case "invite" -> handleInvite(p, args);
                case "accept" -> handleAccept(p);
                case "deny" -> handleDeny(p);
                case "leave" -> handleLeave(p);
                case "color" -> handleColor(p, args);
                case "promote" -> handlePromote(p, args);
                case "demote" -> handleDemote(p, args);
                case "enemy", "opposition" -> handleEnemy(p, args, sub);
                case "info" -> handleInfo(p, args);
                case "list" -> handleList(p);
                default -> p.sendMessage("§cUnknown sub-command. Use /clan for help.");
            }
        } catch (RuntimeException e) {
            p.sendMessage("§cError: " + e.getMessage());
            if (e.getCause() != null) {
                p.sendMessage("§7" + e.getCause().getMessage());
            }
        }
        return true;
    }

    private void handleCreate(Player p, String[] args) {
        if (args.length < 3) {
            p.sendMessage("§cUsage: /clan create <Clan Name> <#ColorHex>");
            return;
        }
        if (clanService.getClanOfPlayer(p.getUniqueId()) != null) {
            p.sendMessage("§cYou're already in a clan. Use /clan leave first.");
            return;
        }
        String colorHex = args[args.length - 1];
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length - 1)).trim();
        if (name.isEmpty() || name.length() > 32) {
            p.sendMessage("§cInvalid clan name (1–32 chars).");
            return;
        }
        if (!colorHex.matches("#[0-9A-Fa-f]{6}")) {
            p.sendMessage("§cInvalid hex. Use format #RRGGBB (e.g. #FFAA00).");
            return;
        }
        Clan clan = clanService.createClan(name, colorHex, p.getUniqueId());
        p.sendMessage("§aClan '" + name + "' created!");
        clanService.updatePlayerDisplay(p);
    }

    private void handleInvite(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /clan invite <player>");
            return;
        }
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null) {
            p.sendMessage("§cYou're not in a clan.");
            return;
        }
        if (!"Leader".equals(clanService.getMemberRank(clanId, p.getUniqueId()))) {
            p.sendMessage("§cOnly clan leaders can invite.");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            p.sendMessage("§cPlayer not online.");
            return;
        }
        clanService.invitePlayerToClan(clanId, target.getUniqueId(), p.getUniqueId());
        p.sendMessage("§aInvite sent to " + target.getName());
        Clan c = clanService.getClanById(clanId);
        target.sendMessage("§bYou've been invited to clan '" + (c != null ? c.getName() : "") + "'. Use /clan accept or /clan deny.");
    }

    private void handleAccept(Player p) {
        if (clanService.getClanOfPlayer(p.getUniqueId()) != null) {
            p.sendMessage("§cYou're already in a clan. Use /clan leave first.");
            return;
        }
        List<Invite> invs = clanService.getPendingInvites(p.getUniqueId());
        if (invs.isEmpty()) {
            p.sendMessage("§cNo pending invites.");
            return;
        }
        Invite inv = invs.get(0);
        clanService.addMemberToClan(inv.getClanId(), p.getUniqueId(), "Member");
        clanService.removeInvitesForPlayer(p.getUniqueId());
        p.sendMessage("§aYou joined the clan!");
        clanService.updatePlayerDisplay(p);
    }

    private void handleDeny(Player p) {
        clanService.removeInvitesForPlayer(p.getUniqueId());
        p.sendMessage("§eClan invite denied.");
    }

    private void handleLeave(Player p) {
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null) {
            p.sendMessage("§cYou're not in a clan.");
            return;
        }
        clanService.removeMemberFromClan(clanId, p.getUniqueId());
        if (clanService.getClanMemberCount(clanId) == 0) {
            clanService.removeClanAndData(clanId);
        }
        p.sendMessage("§eYou left your clan.");
        clanService.updatePlayerDisplay(p);
    }

    private void handleColor(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /clan color <#hex>");
            return;
        }
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null) {
            p.sendMessage("§cYou're not in a clan.");
            return;
        }
        if (!"Leader".equals(clanService.getMemberRank(clanId, p.getUniqueId()))) {
            p.sendMessage("§cOnly clan leaders can change color.");
            return;
        }
        String hex = args[1];
        if (!hex.matches("#[0-9A-Fa-f]{6}")) {
            p.sendMessage("§cInvalid hex. Use format #RRGGBB.");
            return;
        }
        clanService.updateClanColor(clanId, hex);
        p.sendMessage("§aClan color set to " + hex);
        clanService.refreshClanDisplay(clanId);
    }

    private void handlePromote(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /clan promote <player>");
            return;
        }
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null || !"Leader".equals(clanService.getMemberRank(clanId, p.getUniqueId()))) {
            p.sendMessage("§cOnly clan leaders can promote.");
            return;
        }
        Player t = Bukkit.getPlayerExact(args[1]);
        if (t == null || !clanId.equals(clanService.getClanOfPlayer(t.getUniqueId()))) {
            p.sendMessage("§cThat player isn't in your clan.");
            return;
        }
        clanService.addMemberToClan(clanId, t.getUniqueId(), "Leader");
        p.sendMessage("§aPromoted " + t.getName() + " to Leader.");
        t.sendMessage("§6You have been promoted to Leader of your clan.");
    }

    private void handleDemote(Player p, String[] args) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /clan demote <player>");
            return;
        }
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null || !"Leader".equals(clanService.getMemberRank(clanId, p.getUniqueId()))) {
            p.sendMessage("§cOnly clan leaders can demote.");
            return;
        }
        Player t = Bukkit.getPlayerExact(args[1]);
        if (t == null || !clanId.equals(clanService.getClanOfPlayer(t.getUniqueId()))) {
            p.sendMessage("§cThat player isn't in your clan.");
            return;
        }
        if (t.getUniqueId().equals(p.getUniqueId())) {
            p.sendMessage("§cYou can't demote yourself.");
            return;
        }
        clanService.addMemberToClan(clanId, t.getUniqueId(), "Member");
        p.sendMessage("§eDemoted " + t.getName() + " to Member.");
        t.sendMessage("§cYou have been demoted to Member of your clan.");
    }

    private void handleEnemy(Player p, String[] args, String sub) {
        if (args.length < 2) {
            p.sendMessage("§cUsage: /clan " + sub + " <clanName>");
            return;
        }
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (clanId == null || !"Leader".equals(clanService.getMemberRank(clanId, p.getUniqueId()))) {
            p.sendMessage("§cOnly clan leaders can declare enemies.");
            return;
        }
        String name = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
        Clan target = clanService.getClanByName(name);
        if (target == null) {
            p.sendMessage("§cClan not found: " + name);
            return;
        }
        clanService.addClanRelation(clanId, target.getId(), "ENEMY");
        p.sendMessage("§cYour clan is now enemies with " + target.getName());
    }

    private void handleInfo(Player p, String[] args) {
        UUID clanId = clanService.getClanOfPlayer(p.getUniqueId());
        if (args.length > 1) {
            String infoName = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            Clan c = clanService.getClanByName(infoName);
            if (c == null) {
                p.sendMessage("§cClan not found: " + infoName);
                return;
            }
            clanId = c.getId();
        }
        if (clanId == null) {
            p.sendMessage("§cYou're not in a clan and no valid clan name provided.");
            return;
        }
        Clan c = clanService.getClanById(clanId);
        if (c == null) {
            p.sendMessage("§cClan not found.");
            return;
        }
        p.sendMessage("§6Clan: " + c.getName());
        p.sendMessage("§eColor: " + c.getColorHex());
        var members = clanService.getClanMembersWithRanks(clanId);
        StringBuilder sb = new StringBuilder("§bMembers: ");
        for (var entry : members) {
            String name = Bukkit.getOfflinePlayer(entry.getKey()).getName();
            sb.append("[").append(entry.getValue()).append(": ").append(name != null ? name : "?").append("] ");
        }
        p.sendMessage(sb.toString());
    }

    private void handleList(Player p) {
        List<Clan> clans = clanService.getAllClans();
        if (clans.isEmpty()) {
            p.sendMessage("§eNo clans exist yet.");
            return;
        }
        p.sendMessage("§6Clans (" + clans.size() + "):");
        for (Clan clan : clans) {
            int count = clanService.getClanMemberCount(clan.getId());
            p.sendMessage("  §e" + clan.getName() + " §7(" + clan.getColorHex() + ") §f- " + count + " member(s)");
        }
    }

    private void sendHelpMessage(Player p) {
        p.sendMessage("§6==== Clan Commands ====");
        p.sendMessage("§e/clan create <name> <#color> §f- Create a clan (e.g. #FFAA00)");
        p.sendMessage("§e/clan invite <player> §f- Invite a player");
        p.sendMessage("§e/clan accept §f- Accept your most recent invite");
        p.sendMessage("§e/clan deny §f- Deny your most recent invite");
        p.sendMessage("§e/clan leave §f- Leave your clan");
        p.sendMessage("§e/clan promote <player> §f- Promote to leader (leader only)");
        p.sendMessage("§e/clan demote <player> §f- Demote to member (leader only)");
        p.sendMessage("§e/clan color <#hex> §f- Set clan color (leader only)");
        p.sendMessage("§e/clan enemy <clan> §f- Declare enemy (leader only)");
        p.sendMessage("§e/clan info [name] §f- Show your clan or another clan's info");
        p.sendMessage("§e/clan list §f- List all clans");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission(PERMISSION)) return Collections.emptyList();
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "deny", "leave", "color", "promote", "demote", "enemy", "opposition", "info", "list")
                .stream().filter(s -> s.startsWith(args[0].toLowerCase())).collect(Collectors.toList());
        }
        if (args.length >= 2) {
            String sub = args[0].toLowerCase();
            if (("invite".equals(sub) || "promote".equals(sub) || "demote".equals(sub)) && args.length == 2) {
                return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            }
            if (("info".equals(sub) || "enemy".equals(sub) || "opposition".equals(sub)) && args.length >= 2) {
                return clanService.getAllClans().stream()
                    .map(Clan::getName)
                    .filter(n -> n.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                    .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }
}
