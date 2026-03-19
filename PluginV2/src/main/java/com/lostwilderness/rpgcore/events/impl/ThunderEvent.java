package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieHorse;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Thunderstorm: base daily chance, or triggered by Eclipse cascade (eclipse storm with undead cavalry).
 */
public final class ThunderEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public ThunderEvent(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        int baseChance = plugin.getConfig().getInt("events.thunder.base-chance-pct", 15);
        if (baseChance <= 0 || ThreadLocalRandom.current().nextInt(100) >= baseChance) return;
        startStorm(world, false, day);
    }

    /** Called by Eclipse cascade to start eclipse thunderstorm with undead cavalry in the given world. */
    public void triggerEclipseStorm(int day, World world) {
        if (world == null) world = overworld();
        if (world == null) return;
        startStorm(world, true, day);
    }

    public int getEclipseChancePct() {
        return plugin.getConfig().getInt("events.thunder.eclipse-chance-pct", 40);
    }

    private void startStorm(World world, boolean eclipseMode, int day) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            world.setGameRule(GameRule.DO_WEATHER_CYCLE, true);
            world.setStorm(true);
            world.setThundering(true);
            active = true;
            if (eclipseMode) {
                Bukkit.broadcastMessage("§8☾ Thunderstorm Eclipse! Undead cavalry charges through the lightning! ☾");
                spawnUndeadCavalry(world);
            } else {
                Bukkit.broadcastMessage("§8☁ A thunderstorm rolls across the land ☁");
            }
            long now = world.getTime();
            long untilSixAM = (6000 - now + 24000) % 24000;
            if (untilSixAM == 0) untilSixAM = 24000;
            endTask = new BukkitRunnable() {
                @Override
                public void run() {
                    world.setStorm(false);
                    world.setThundering(false);
                    active = false;
                    endTask = null;
                    Bukkit.broadcastMessage("§7The thunderstorm subsides… the skies clear by dawn.");
                }
            };
            endTask.runTaskLater(plugin, untilSixAM);
        });
    }

    private void spawnUndeadCavalry(World world) {
        int horsesPerEclipse = plugin.getConfig().getInt("events.thunder.horses-per-eclipse", 3);
        var players = world.getPlayers();
        if (players.isEmpty()) return;
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (int i = 0; i < horsesPerEclipse; i++) {
            Player target = players.get(r.nextInt(players.size()));
            Location loc = target.getLocation().add(r.nextDouble() * 20 - 10, 0, r.nextDouble() * 20 - 10);
            loc.setY(world.getHighestBlockYAt(loc.getBlockX(), loc.getBlockZ()));
            ZombieHorse zHorse = (ZombieHorse) world.spawnEntity(loc, org.bukkit.entity.EntityType.ZOMBIE_HORSE);
            Zombie rider = (Zombie) world.spawnEntity(loc, org.bukkit.entity.EntityType.ZOMBIE);
            rider.getEquipment().setHelmet(new ItemStack(Material.GOLDEN_HELMET));
            rider.getEquipment().setChestplate(new ItemStack(Material.GOLDEN_CHESTPLATE));
            rider.getEquipment().setLeggings(new ItemStack(Material.GOLDEN_LEGGINGS));
            rider.getEquipment().setBoots(new ItemStack(Material.GOLDEN_BOOTS));
            ItemStack sword = new ItemStack(Material.GOLDEN_SWORD);
            sword.addEnchantment(Enchantment.SHARPNESS, 2);
            rider.getEquipment().setItemInMainHand(sword);
            zHorse.addPassenger(rider);
        }
    }

    private static World overworld() {
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (!active) return;
        if (endTask != null) {
            endTask.cancel();
            endTask = null;
        }
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() == World.Environment.NORMAL) {
                w.setStorm(false);
                w.setThundering(false);
            }
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Thunderstorm"; }
}
