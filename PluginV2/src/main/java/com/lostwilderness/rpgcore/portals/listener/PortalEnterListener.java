package com.lostwilderness.rpgcore.portals.listener;

import com.lostwilderness.rpgcore.portals.BungeeMessenger;
import com.lostwilderness.rpgcore.portals.PortalEntitySerializer;
import com.lostwilderness.rpgcore.portals.PortalRepository;
import com.lostwilderness.rpgcore.portals.PortalService;
import com.lostwilderness.rpgcore.portals.PortalsConfig;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

public final class PortalEnterListener implements Listener {

    private final Plugin plugin;
    private final PortalService portalService;
    private final PortalRepository repo;
    private final String targetServer;
    private final boolean isSurvival;
    private final Material portalMaterial;
    private final Set<UUID> inTransit = Collections.newSetFromMap(new WeakHashMap<>());

    public PortalEnterListener(Plugin plugin, PortalService portalService, PortalRepository repo) {
        this.plugin = plugin;
        this.portalService = portalService;
        this.repo = repo;
        this.targetServer = portalService.getConfig().getTargetServer();
        this.isSurvival = portalService.isSurvival();
        this.portalMaterial = portalService.getPortalMaterial();
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null || e.getFrom().getBlock().equals(e.getTo().getBlock()))
            return;
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        if (inTransit.contains(uuid))
            return;

        Block stepped = findIntersectedPortalBlock(e.getTo());
        if (stepped == null)
            return;

        Location frameOrigin = null;
        boolean alongX = false;
        int frameWidth = 4;
        int frameHeight = 5;
        int maxBack = Math.max(0, portalService.getMaxFrameWidth() - 1);
        int maxBackY = Math.max(0, portalService.getMaxFrameHeight() - 1);
        outer: for (boolean axis : new boolean[] { true, false }) {
            for (int dx = -maxBack; dx <= 0; dx++) {
                for (int dy = -maxBackY; dy <= 0; dy++) {
                    Block cand = stepped.getRelative(axis ? dx : 0, dy, axis ? 0 : dx);
                    var frame = portalService.findValidPortalFrameAtOrigin(cand, axis);
                    if (frame != null) {
                        frameOrigin = cand.getLocation();
                        alongX = axis;
                        frameWidth = frame.width();
                        frameHeight = frame.height();
                        break outer;
                    }
                }
            }
        }
        if (frameOrigin == null)
            return;

        Location frameOriginFinal = frameOrigin.clone();
        boolean alongXFinal = alongX;
        int widthFinal = frameWidth;
        int heightFinal = frameHeight;

        repo.hasPermanentPortalAt(uuid, isSurvival,
                frameOriginFinal.getWorld().getName(),
                frameOriginFinal.getX(), frameOriginFinal.getY(), frameOriginFinal.getZ())
                .thenCompose(permanent -> {
                    if (permanent) {
                        return repo.getPortalNameAtLocation(uuid, isSurvival,
                                frameOriginFinal.getWorld().getName(),
                                frameOriginFinal.getX(), frameOriginFinal.getY(), frameOriginFinal.getZ())
                                .thenCompose(name -> {
                                    if (name == null)
                                        name = "Portal_" + System.currentTimeMillis();
                                    final String finalName = name;
                                    // For permanent portals, we still add to pending so the target server knows to
                                    // expect someone,
                                    // but we use the existing frame origin.
                                    return repo
                                            .addPendingPortal(uuid, finalName, alongXFinal ? "x" : "z",
                                                    frameOriginFinal, isSurvival, widthFinal, heightFinal)
                                            .thenApply(v -> finalName);
                                });
                    }
                    return repo.findPortalNameAtPendingFrame(uuid, isSurvival,
                            frameOriginFinal.getWorld().getName(),
                            frameOriginFinal.getX(), frameOriginFinal.getY(), frameOriginFinal.getZ())
                            .thenCompose(name -> {
                                if (name != null)
                                    return CompletableFuture.completedFuture(name);
                                String newName = "Portal_" + System.currentTimeMillis();
                                return repo
                                        .addPendingPortal(uuid, newName, alongXFinal ? "x" : "z", frameOriginFinal,
                                                isSurvival, widthFinal, heightFinal)
                                        .thenApply(v -> newName);
                            });
                })
                .thenCompose(portalName -> {
                    String entityJson = PortalEntitySerializer.serialize(p);
                    return repo.saveTransferEntities(uuid, portalName, entityJson).thenApply(ignored -> portalName);
                })
                .thenAccept(portalName -> plugin.getServer().getScheduler().runTask(plugin, () -> {
                    plugin.getLogger().info(
                            "Player " + p.getName() + " entered portal " + portalName + ". Sending to " + targetServer);

                    // Party notification: Notify party members
                    com.lostwilderness.rpgcore.party.PartyService partyService = null;
                    try {
                        partyService = plugin.getServer().getServicesManager()
                            .load(com.lostwilderness.rpgcore.party.PartyService.class);
                    } catch (Exception ignored) {
                        // PartyService not available
                    }

                    if (partyService != null) {
                        com.lostwilderness.rpgcore.party.Party party = partyService.getParty(uuid);
                        if (party != null) {
                            String serverName = targetServer != null ? targetServer : "another server";
                            net.kyori.adventure.text.Component message = net.kyori.adventure.text.Component.text()
                                .append(net.kyori.adventure.text.Component.text(p.getName())
                                    .color(net.kyori.adventure.text.format.NamedTextColor.YELLOW))
                                .append(net.kyori.adventure.text.Component.text(" is using a portal to ")
                                    .color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
                                .append(net.kyori.adventure.text.Component.text(serverName)
                                    .color(net.kyori.adventure.text.format.NamedTextColor.GREEN))
                                .append(net.kyori.adventure.text.Component.text("!")
                                    .color(net.kyori.adventure.text.format.NamedTextColor.GRAY))
                                .build();

                            for (UUID memberId : party.getMembers()) {
                                if (memberId.equals(uuid)) continue; // Don't notify the player using the portal
                                Player member = org.bukkit.Bukkit.getPlayer(memberId);
                                if (member != null && member.isOnline()) {
                                    member.sendMessage(message);
                                }
                            }
                        }
                    }

                    inTransit.add(uuid);
                    p.sendPluginMessage(plugin, "BungeeCord", BungeeMessenger.createConnectMessage(targetServer));
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> inTransit.remove(uuid), 20L);
                }));
    }

    private Block findIntersectedPortalBlock(Location to) {
        if (to == null)
            return null;
        Block feet = to.getBlock();
        if (feet.getType() == portalMaterial)
            return feet;
        Block body = to.clone().add(0, 1, 0).getBlock();
        if (body.getType() == portalMaterial)
            return body;
        Block below = to.clone().add(0, -1, 0).getBlock();
        if (below.getType() == portalMaterial)
            return below;
        return null;
    }
}
