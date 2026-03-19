package com.lostwilderness.rpgcore.party.listener;

import com.lostwilderness.rpgcore.party.Party;
import com.lostwilderness.rpgcore.party.PartyRepository;
import com.lostwilderness.rpgcore.party.PartyService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * Handles party session lifecycle:
 * - PlayerQuitEvent → leave party (full disconnect)
 * - PlayerJoinEvent → reload party from DB into cache
 * - Portal transfers DO NOT trigger quit/join, so party persists
 */
public final class PartySessionListener implements Listener {

    private final PartyService service;
    private final PartyRepository repo;
    private final SchedulerService scheduler;

    public PartySessionListener(PartyService service, PartyRepository repo, SchedulerService scheduler) {
        this.service = service;
        this.repo = repo;
        this.scheduler = scheduler;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Player fully disconnected → leave party
        UUID playerUuid = event.getPlayer().getUniqueId();

        scheduler.runAsync(() -> {
            try {
                service.leaveParty(playerUuid);
            } catch (Exception e) {
                // Silently ignore errors on disconnect (party might already be disbanded)
            }
        });
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Load player's party from DB into cache
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        scheduler.runAsync(() -> {
            repo.getPlayerParty(playerUuid).thenCompose(partyId -> {
                if (partyId == null) {
                    return java.util.concurrent.CompletableFuture.completedFuture(null);
                }
                return repo.getPartyById(partyId);
            }).thenAccept(party -> {
                if (party != null) {
                    scheduler.runSync(() -> {
                        service.cacheParty(party);
                        service.updatePlayerDisplay(player);
                    });
                }
            }).exceptionally(ex -> {
                player.sendMessage("§cFailed to load party data: " + ex.getMessage());
                return null;
            });
        });
    }
}
