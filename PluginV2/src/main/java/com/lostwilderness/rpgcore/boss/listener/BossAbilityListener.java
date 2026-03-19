package com.lostwilderness.rpgcore.boss.listener;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BossAbilityListener implements Listener {

    private final Set<UUID> triggeredHorde = new HashSet<>();

    @EventHandler
    public void onElDiabloHealthChange(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Wither wither)) return;
        if (!wither.hasMetadata("diablo")) return;

        double maxHp = wither.getAttribute(Attribute.GENERIC_MAX_HEALTH).getBaseValue();
        double currentHp = wither.getHealth() - event.getFinalDamage();

        if (currentHp <= maxHp * 0.25 && !triggeredHorde.contains(wither.getUniqueId())) {
            triggeredHorde.add(wither.getUniqueId());
            spawnMiniHorde(wither.getLocation());
        }
    }

    private void spawnMiniHorde(Location center) {
        World world = center.getWorld();
        if (world == null) return;
        
        Location witherLoc = center.clone().add(0, -1, 0);
        witherLoc.setY(Math.max(0, witherLoc.getBlockY()));
        world.spawnEntity(witherLoc, EntityType.WITHER);

        int count = 3 + (int) (Math.random() * 3);
        for (int i = 0; i < count; i++) {
            WitherSkeleton skeleton = (WitherSkeleton) world.spawnEntity(
                    center.clone().add(Math.random() * 5 - 2.5, 1, Math.random() * 5 - 2.5),
                    EntityType.WITHER_SKELETON);
            skeleton.customName(Component.text("Diablo Minion", NamedTextColor.DARK_RED));
        }
        world.playSound(center, Sound.ENTITY_WITHER_SPAWN, 1.2f, 0.5f);
    }
}
