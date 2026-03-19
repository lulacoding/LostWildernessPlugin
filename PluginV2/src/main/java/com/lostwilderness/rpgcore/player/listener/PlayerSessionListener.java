package com.lostwilderness.rpgcore.player.listener;

import com.lostwilderness.rpgcore.player.PlayerProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

/**
 * On join: ensure profile is in cache (already loaded in pre-login). On quit: save and evict.
 */
public final class PlayerSessionListener implements Listener {

    private final PlayerProfileService profileService;

    public PlayerSessionListener(PlayerProfileService profileService) {
        this.profileService = profileService;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (profileService.getProfile(uuid).isEmpty()) {
            // Fallback: load now (should be rare if pre-login succeeded)
            profileService.loadProfileAsync(uuid);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        profileService.saveProfileAsync(uuid);
    }
}
