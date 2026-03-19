package com.lostwilderness.lobbyv2;

import com.lostwilderness.rpgcore.classes.ClassService;
import com.lostwilderness.rpgcore.personality.TraitRepository;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.concurrent.CompletableFuture;

public class LobbyTeleportListener implements Listener {
    private final Plugin plugin;
    private final VerificationRepository repo;
    private final TraitRepository traitRepository;
    private final ClassService classService;
    private final String targetServer;
    private final Material teleporterBlock;
    private final Set<UUID> inTransit = Collections.newSetFromMap(new WeakHashMap<>());

    public LobbyTeleportListener(Plugin plugin, VerificationRepository repo, TraitRepository traitRepository,
            ClassService classService, String targetServer, Material teleporterBlock) {
        this.plugin = plugin;
        this.repo = repo;
        this.traitRepository = traitRepository;
        this.classService = classService;
        this.targetServer = targetServer;
        this.teleporterBlock = teleporterBlock;
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null || e.getFrom().getBlock().equals(e.getTo().getBlock()))
            return;
        Player p = e.getPlayer();
        if (e.getTo().getBlock().getType() != teleporterBlock)
            return;
        UUID uuid = p.getUniqueId();
        if (inTransit.contains(uuid))
            return;

        // Prevent rapid re-triggering
        inTransit.add(uuid);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                boolean verified = repo.isVerified(p.getName());
                if (!verified) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        p.sendMessage("§cYou must verify first.");
                        p.sendMessage("§7Run §f/verify §7in-game, then finish verification in Discord.");
                        bounceBack(p, e.getFrom());
                    });
                    return;
                }

                // Check quiz completion
                boolean completedQuiz = traitRepository.hasCompletedQuiz(uuid).exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to check quiz completion for " + p.getName() + ": " + ex.getMessage());
                    return false;
                }).join();

                if (!completedQuiz) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        p.sendMessage("§cYou must complete the personality quiz first.");
                        p.sendMessage("§7The quiz will start automatically - answer all questions.");
                        bounceBack(p, e.getFrom());
                    });
                    return;
                }

                if (classService == null) {
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        p.sendMessage("§cClass service is currently unavailable.");
                        p.sendMessage("§7Please contact staff - classes must be enabled before travel.");
                        bounceBack(p, e.getFrom());
                    });
                    return;
                }

                CompletableFuture<Boolean> hasClassFuture = classService.hasClass(uuid);
                boolean hasClass = hasClassFuture.exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to read class for " + p.getName() + ": " + ex.getMessage());
                    return false;
                }).join();

                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (!hasClass) {
                        p.sendMessage("§cYou must pick a class before entering the portal.");
                        p.sendMessage("§7Speak to the Class Guide NPC or run §f/class§7.");
                        bounceBack(p, e.getFrom());
                        return;
                    }

                    p.sendMessage("§aTeleporting to " + targetServer + "...");
                    sendToServer(p, targetServer);

                    // Keep in transit for a few seconds to prevent spam
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> inTransit.remove(uuid), 60L);
                });
            } catch (Exception ex) {
                plugin.getLogger().warning("Teleport gate check failed for " + p.getName() + ": " + ex.getMessage());
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    p.sendMessage("§cCould not validate your travel status right now. Please try again.");
                    bounceBack(p, e.getFrom());
                });
            }
        });
    }

    private void bounceBack(Player p, org.bukkit.Location from) {
        // Simple bounce back
        org.bukkit.util.Vector dir = p.getLocation().getDirection().multiply(-0.5).setY(0.2);
        p.setVelocity(dir);
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> inTransit.remove(p.getUniqueId()), 40L);
    }

    private void sendToServer(Player player, String serverName) {
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(b);
            out.writeUTF("Connect");
            out.writeUTF(serverName);
            player.sendPluginMessage(plugin, "BungeeCord", b.toByteArray());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
