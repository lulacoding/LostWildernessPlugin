package com.lostwilderness.rpgcore.portals.command;

import com.lostwilderness.rpgcore.portals.PortalRepository;
import com.lostwilderness.rpgcore.portals.PortalRepository.PortalCoordinatesRow;
import com.lostwilderness.rpgcore.portals.PortalService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public final class DeletePortalsCommand implements CommandExecutor {

    private static final String PERMISSION = "lw.portals.delete";

    private final Plugin plugin;
    private final PortalRepository repo;
    private final Material portalMaterial;

    public DeletePortalsCommand(Plugin plugin, PortalRepository repo, PortalService portalService) {
        this.plugin = plugin;
        this.repo = repo;
        this.portalMaterial = portalService.getPortalMaterial();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label,
            @NotNull String[] args) {
        if (!sender.hasPermission(PERMISSION)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /deleteportals <username>");
            return true;
        }

        Player target = Bukkit.getPlayerExact(args[0]);
        UUID uuid = target != null ? target.getUniqueId() : Bukkit.getOfflinePlayer(args[0]).getUniqueId();

        repo.listPortalCoordinatesForPlayer(uuid).thenAccept(rows -> {
            if (rows.isEmpty()) {
                sender.sendMessage(ChatColor.GRAY + "No portals found for " + args[0]);
                return;
            }
            Bukkit.getScheduler().runTask(plugin, () -> {
                for (PortalCoordinatesRow row : rows) {
                    boolean alongX = "x".equalsIgnoreCase(row.direction());
                    clearBlocksAt(row.survivalWorld(), row.sX(), row.sY(), row.sZ(), alongX, row.width(), row.height());
                    clearBlocksAt(row.amplifiedWorld(), row.aX(), row.aY(), row.aZ(), alongX, row.width(),
                            row.height());
                }
                repo.deletePortalsAndPendingByPlayer(uuid)
                        .thenRun(() -> sender.sendMessage(ChatColor.GREEN + "Deleted " + rows.size() + " portal(s)."));
            });
        });
        return true;
    }

    private void clearBlocksAt(String worldName, double x, double y, double z, boolean alongX, int width, int height) {
        if (worldName == null)
            return;
        var w = Bukkit.getWorld(worldName);
        if (w == null)
            return;
        Location loc = new Location(w, x, y, z);

        // Clear the actual portal area
        for (int dy = 0; dy < height; dy++) {
            for (int i = 0; i < width; i++) {
                Block b = loc.getBlock().getRelative(alongX ? i : 0, dy, alongX ? 0 : i);
                if (b.getType() == Material.CRYING_OBSIDIAN || b.getType() == portalMaterial) {
                    b.setType(Material.AIR);
                }
            }
        }
    }
}
