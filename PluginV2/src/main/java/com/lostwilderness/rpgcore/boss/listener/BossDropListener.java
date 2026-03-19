package com.lostwilderness.rpgcore.boss.listener;

import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BossDropListener implements Listener {

    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Wither wither)) return;

        // Custom drops for Devoider
        if (wither.hasMetadata("devoider")) {
            handleDevoiderDrops(event);
        } else if (wither.hasMetadata("diablo")) {
            handleElDiabloDrops(event);
        }
    }

    private void handleDevoiderDrops(EntityDeathEvent event) {
        Wither wither = (Wither) event.getEntity();
        World world = wither.getWorld();
        Location loc = wither.getLocation();

        event.setDroppedExp(event.getDroppedExp() * 2);
        event.getDrops().clear();
        event.getDrops().add(new ItemStack(Material.NETHER_STAR, 2));

        for (int i = 0; i < 8; i++) {
            WitherSkeleton skeleton = (WitherSkeleton) world.spawnEntity(
                    loc.clone().add(randomOffset(), 1, randomOffset()), EntityType.WITHER_SKELETON);
            skeleton.customName(Component.text("Devoider Minion", NamedTextColor.DARK_GRAY));
            skeleton.setCustomNameVisible(true);
        }

        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 2.0f, 0.5f);
    }

    private void handleElDiabloDrops(EntityDeathEvent event) {
        Wither wither = (Wither) event.getEntity();
        Location loc = wither.getLocation();
        World world = wither.getWorld();

        event.setDroppedExp(event.getDroppedExp() * 4);
        event.getDrops().clear();
        event.getDrops().add(new ItemStack(Material.NETHER_STAR, 6));

        world.strikeLightningEffect(loc);
        world.playSound(loc, Sound.ENTITY_WITHER_DEATH, 2.0f, 0.3f);
    }

    private double randomOffset() {
        return (Math.random() - 0.5) * 6;
    }
}
