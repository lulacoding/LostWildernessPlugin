
package com.lostwilderness.rpgcore.player.listener;

import com.lostwilderness.rpgcore.player.PlayerProfileService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Loads profile async before player joins. Must not block the login thread too
 * long.
 */
public final class PlayerProfilePreloadListener implements Listener {

    private final PlayerProfileService profileService;

    public PlayerProfilePreloadListener(PlayerProfileService profileService) {
        this.profileService = profileService;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onAsyncPreLogin(AsyncPlayerPreLoginEvent event) {
        UUID uuid = event.getUniqueId();
        try {
            profileService.loadProfileAsync(uuid).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            @SuppressWarnings("deprecation")
            var result = AsyncPlayerPreLoginEvent.Result.KICK_OTHER;
            event.disallow(result, "Profile load failed. Try again.");
        }
    }
}
