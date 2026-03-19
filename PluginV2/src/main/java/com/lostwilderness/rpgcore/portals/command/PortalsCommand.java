package com.lostwilderness.rpgcore.portals.command;

import com.lostwilderness.rpgcore.portals.PortalRepository;
import com.lostwilderness.rpgcore.portals.PortalRepository.PortalListingRow;
import com.lostwilderness.rpgcore.portals.PortalService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class PortalsCommand implements CommandExecutor {

    private final PortalRepository repo;
    private final PortalService portalService;

    public PortalsCommand(PortalRepository repo, PortalService portalService) {
        this.repo = repo;
        this.portalService = portalService;
    }

    private static final String PERMISSION = "lw.portals.use";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this.");
            return true;
        }
        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }

        repo.listPortalsForPlayer(player.getUniqueId()).thenAccept(list -> {
            player.sendMessage(ChatColor.GOLD + "Your linked portals:");
            if (list.isEmpty()) {
                player.sendMessage(ChatColor.GRAY + "You don't have any linked portals.");
                return;
            }
            for (PortalListingRow row : list) {
                String loc = portalService.isSurvival() ? row.survivalWorld() : row.amplifiedWorld();
                player.sendMessage(ChatColor.GRAY + " - " + row.portalName() + " at " + ChatColor.AQUA + loc + ChatColor.GRAY + " facing " + row.direction());
            }
        });
        return true;
    }
}
