package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import com.lostwilderness.rpgcore.events.config.LWConfigs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Miner's Blessing: when active, extra ore chance and XP boost on ore break. Config from lw-events-extra.
 */
public final class MiningBlessingEvent implements DailyWorldEvent, Listener {

    private static final Set<Material> ORES = new HashSet<>(Arrays.asList(
        Material.COAL_ORE, Material.DEEPSLATE_COAL_ORE,
        Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE,
        Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE,
        Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE,
        Material.REDSTONE_ORE, Material.DEEPSLATE_REDSTONE_ORE,
        Material.LAPIS_ORE, Material.DEEPSLATE_LAPIS_ORE,
        Material.DIAMOND_ORE, Material.DEEPSLATE_DIAMOND_ORE,
        Material.NETHER_GOLD_ORE, Material.NETHER_QUARTZ_ORE
    ));

    private final Plugin plugin;
    private final LWConfigs lwConfigs;
    private volatile boolean active = false;
    private BukkitRunnable endTask;

    public MiningBlessingEvent(Plugin plugin, LWConfigs lwConfigs) {
        this.plugin = plugin;
        this.lwConfigs = lwConfigs;
    }

    private boolean isEnabled() {
        return lwConfigs != null && lwConfigs.getEventsExtra() != null
            && lwConfigs.getEventsExtra().getBoolean("mining.enabled", true);
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        if (!isEnabled()) return;
        double chance = lwConfigs.getEventsExtra().getDouble("mining.base_chance_per_day", 0.12);
        if (ThreadLocalRandom.current().nextDouble() >= chance) return;

        active = true;
        for (Player p : world.getPlayers()) {
            p.sendMessage("§eMiner's Blessing! Extra ore and XP from mining today.");
        }
        endTask = new BukkitRunnable() {
            @Override
            public void run() {
                active = false;
                endTask = null;
            }
        };
        endTask.runTaskLater(plugin, 24000L);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (!active) return;
        if (!ORES.contains(e.getBlock().getType())) return;
        double extraOre = lwConfigs.getEventsExtra().getDouble("mining.extra_ore_chance", 0.35);
        double expBoost = lwConfigs.getEventsExtra().getDouble("mining.exp_boost_chance", 0.40);
        if (ThreadLocalRandom.current().nextDouble() < extraOre) {
            ItemStack drop = e.getBlock().getDrops(e.getPlayer().getInventory().getItemInMainHand()).stream().findFirst().orElse(null);
            if (drop != null) e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation().add(0.5, 0.5, 0.5), drop);
        }
        if (ThreadLocalRandom.current().nextDouble() < expBoost) {
            e.getPlayer().giveExp(e.getExpToDrop() + 1);
        }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (endTask != null) { endTask.cancel(); endTask = null; }
        active = false;
    }

    @Override
    public String getDisplayName() { return "Miner's Blessing"; }
}
