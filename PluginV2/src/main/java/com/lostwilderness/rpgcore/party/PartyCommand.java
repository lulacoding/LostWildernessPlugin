package com.lostwilderness.rpgcore.party;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

/**
 * /party command handler.
 * Commands: create, invite, accept, leave, disband, info, kick
 */
public final class PartyCommand implements CommandExecutor, TabCompleter {

    private final PartyService service;

    public PartyCommand(PartyService service) {
        this.service = service;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use this command.");
            return true;
        }

        if (args.length == 0) {
            sendUsage(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        try {
            switch (subcommand) {
                case "create" -> handleCreate(player, args);
                case "invite" -> handleInvite(player, args);
                case "accept" -> handleAccept(player, args);
                case "leave" -> handleLeave(player);
                case "disband" -> handleDisband(player);
                case "info" -> handleInfo(player);
                case "kick" -> handleKick(player, args);
                default -> sendUsage(player);
            }
        } catch (IllegalStateException e) {
            player.sendMessage("§c" + e.getMessage());
        } catch (Exception e) {
            player.sendMessage("§cAn error occurred: " + e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    private void handleCreate(Player player, String[] args) {
        String name = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
        Party party = service.createParty(player.getUniqueId(), name);
        player.sendMessage("§aCreated party: §e" + party.getName());
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party invite <player>");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            player.sendMessage("§cYou cannot invite yourself!");
            return;
        }

        service.invitePlayer(player.getUniqueId(), target.getUniqueId());
        player.sendMessage("§aInvited §e" + target.getName() + " §ato the party!");
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party accept <player>");
            return;
        }

        Player inviter = Bukkit.getPlayer(args[1]);
        if (inviter == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }

        Party party = service.getParty(inviter.getUniqueId());
        if (party == null) {
            player.sendMessage("§c" + inviter.getName() + " is not in a party.");
            return;
        }

        service.acceptInvite(player.getUniqueId(), party.getId());
        player.sendMessage("§aYou joined §e" + party.getName() + "§a!");

        // Notify party members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player)) {
                member.sendMessage("§e" + player.getName() + " §ajoined the party!");
            }
        }
    }

    private void handleLeave(Player player) {
        if (!service.isInParty(player.getUniqueId())) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        Party party = service.getParty(player.getUniqueId());
        String partyName = party != null ? party.getName() : "the party";

        service.leaveParty(player.getUniqueId());
        player.sendMessage("§aYou left " + partyName + ".");

        // Notify remaining members
        if (party != null) {
            for (UUID memberId : party.getMembers()) {
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    member.sendMessage("§e" + player.getName() + " §7left the party.");
                }
            }
        }
    }

    private void handleDisband(Player player) {
        Party party = service.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can disband the party.");
            return;
        }

        // Notify all members before disbanding
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage("§cThe party has been disbanded.");
            }
        }

        service.disbandParty(player.getUniqueId());
    }

    private void handleInfo(Player player) {
        Party party = service.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        player.sendMessage("§6=== Party Info ===");
        player.sendMessage("§eName: §f" + party.getName());

        Player leader = Bukkit.getPlayer(party.getLeader());
        String leaderName = leader != null ? leader.getName() : party.getLeader().toString();
        player.sendMessage("§eLeader: §f" + leaderName);

        player.sendMessage("§eMembers (" + party.size() + "/" + party.getMaxSize() + "):");
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            String memberName = member != null ? member.getName() : memberId.toString();
            String status = member != null && member.isOnline() ? "§a✓" : "§7✗";
            player.sendMessage("  " + status + " §f" + memberName);
        }

        if (!party.getInvites().isEmpty()) {
            player.sendMessage("§ePending Invites:");
            for (UUID inviteId : party.getInvites()) {
                Player invited = Bukkit.getPlayer(inviteId);
                String invitedName = invited != null ? invited.getName() : inviteId.toString();
                player.sendMessage("  §7- §f" + invitedName);
            }
        }
    }

    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cUsage: /party kick <player>");
            return;
        }

        Party party = service.getParty(player.getUniqueId());
        if (party == null) {
            player.sendMessage("§cYou are not in a party.");
            return;
        }

        if (!party.isLeader(player.getUniqueId())) {
            player.sendMessage("§cOnly the party leader can kick members.");
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            player.sendMessage("§cPlayer not found: " + args[1]);
            return;
        }

        service.kickMember(player.getUniqueId(), target.getUniqueId());
        player.sendMessage("§aKicked §e" + target.getName() + " §afrom the party.");
        target.sendMessage("§cYou were kicked from the party.");

        // Notify remaining members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline() && !member.equals(player) && !member.equals(target)) {
                member.sendMessage("§e" + target.getName() + " §7was kicked from the party.");
            }
        }
    }

    private void sendUsage(Player player) {
        player.sendMessage("§6=== Party Commands ===");
        player.sendMessage("§e/party create [name] §7- Create a party");
        player.sendMessage("§e/party invite <player> §7- Invite a player");
        player.sendMessage("§e/party accept <player> §7- Accept invite");
        player.sendMessage("§e/party leave §7- Leave the party");
        player.sendMessage("§e/party disband §7- Disband (leader only)");
        player.sendMessage("§e/party info §7- Show party info");
        player.sendMessage("§e/party kick <player> §7- Kick member (leader only)");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) return null;

        if (args.length == 1) {
            List<String> subcommands = List.of("create", "invite", "accept", "leave", "disband", "info", "kick");
            return subcommands.stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            if ("invite".equals(subcommand)) {
                // Suggest online players not in party
                return Bukkit.getOnlinePlayers().stream()
                    .filter(p -> !service.isInParty(p.getUniqueId()))
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            } else if ("accept".equals(subcommand)) {
                // Suggest players who invited you
                Set<UUID> invites = service.getPlayerInvites(player.getUniqueId());
                return invites.stream()
                    .map(Bukkit::getPlayer)
                    .filter(Objects::nonNull)
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
            } else if ("kick".equals(subcommand)) {
                // Suggest party members (excluding leader)
                Party party = service.getParty(player.getUniqueId());
                if (party != null && party.isLeader(player.getUniqueId())) {
                    return party.getMembers().stream()
                        .filter(uuid -> !uuid.equals(player.getUniqueId()))
                        .map(Bukkit::getPlayer)
                        .filter(Objects::nonNull)
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
            }
        }

        return null;
    }
}
