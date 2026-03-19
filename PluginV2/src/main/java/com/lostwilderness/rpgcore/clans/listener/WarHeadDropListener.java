package com.lostwilderness.rpgcore.clans.listener;

import com.lostwilderness.rpgcore.clans.ClanService;
import com.lostwilderness.rpgcore.clans.WarService;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;

/**
 * When a player kills another in PvP and their clans are at war, drop the
 * victim's head.
 */
public final class WarHeadDropListener implements Listener {

    private final ClanService clanService;
    private final WarService warService;

    public WarHeadDropListener(ClanService clanService, WarService warService) {
        this.clanService = clanService;
        this.warService = warService;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player victim = e.getEntity();
        Player killer = victim.getKiller();
        if (killer == null || killer.equals(victim))
            return;

        UUID killerClanId = clanService.getClanOfPlayer(killer.getUniqueId());
        UUID victimClanId = clanService.getClanOfPlayer(victim.getUniqueId());
        if (killerClanId == null || victimClanId == null)
            return;
        if (!warService.areAtWar(killerClanId, victimClanId))
            return;

        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(victim);
            meta.setDisplayName("§c" + victim.getName() + "'s Head");
            head.setItemMeta(meta);
        }
        victim.getWorld().dropItemNaturally(victim.getLocation(), head);
    }
}
