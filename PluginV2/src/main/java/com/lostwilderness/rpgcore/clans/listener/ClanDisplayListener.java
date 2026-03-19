package com.lostwilderness.rpgcore.clans.listener;

import com.lostwilderness.rpgcore.clans.ClanService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * Updates clan display (scoreboard team prefix) on join.
 * Chat format is not set here (legacy AsyncPlayerChatEvent deprecated); use a chat plugin or Paper chat if needed.
 */
public final class ClanDisplayListener implements Listener {

    private final ClanService clanService;

    public ClanDisplayListener(ClanService clanService) {
        this.clanService = clanService;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        clanService.updatePlayerDisplay(e.getPlayer());
    }
}
