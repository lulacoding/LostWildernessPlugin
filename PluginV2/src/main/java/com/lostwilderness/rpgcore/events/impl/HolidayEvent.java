package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Calendar-driven holiday events: Halloween (Oct 31) and Christmas (Dec 25) with title and simple radius effects.
 */
public final class HolidayEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;

    public HolidayEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    private boolean isHalloweenEnabled() {
        return plugin.getConfig().getBoolean("events.holiday.halloween.enabled", true);
    }

    private boolean isChristmasEnabled() {
        return plugin.getConfig().getBoolean("events.holiday.christmas.enabled", true);
    }

    private int getHalloweenRadius() {
        return plugin.getConfig().getInt("events.holiday.halloween.mob-radius", 24);
    }

    private double getPumpkinChance() {
        return plugin.getConfig().getDouble("events.holiday.halloween.pumpkin-chance", 0.02);
    }

    private double getChristmasHealAmount() {
        return plugin.getConfig().getDouble("events.holiday.christmas.heal-amount", 2.0);
    }

    private static boolean isHalloween(LocalDate date) {
        return date.getMonthValue() == 10 && date.getDayOfMonth() == 31;
    }

    private static boolean isChristmas(LocalDate date) {
        return date.getMonthValue() == 12 && date.getDayOfMonth() == 25;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        LocalDate date = calendar.getCurrentSnapshot().date();
        World firstOverworld = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (firstOverworld == null || world != firstOverworld) return;

        if (isHalloween(date) && isHalloweenEnabled()) {
            active = true;
            for (World w : Bukkit.getWorlds()) {
                if (w.getEnvironment() != World.Environment.NORMAL) continue;
                for (Player p : w.getPlayers()) {
                    p.sendTitle("§6§lHalloween", "§7Spooky spirits are about…", 20, 80, 40);
                }
            }
            runHalloweenEffects();
            active = false;
        } else if (isChristmas(date) && isChristmasEnabled()) {
            active = true;
            for (World w : Bukkit.getWorlds()) {
                if (w.getEnvironment() != World.Environment.NORMAL) continue;
                for (Player p : w.getPlayers()) {
                    p.sendTitle("§c§lMerry Christmas!", "§fPeace and joy to you.", 20, 80, 40);
                }
            }
            runChristmasEffects();
            active = false;
        }
    }

    private void runHalloweenEffects() {
        int radius = getHalloweenRadius();
        double pumpkinChance = getPumpkinChance();
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player p : w.getPlayers()) {
                if (!p.isOnline()) continue;
                int px = p.getLocation().getBlockX();
                int pz = p.getLocation().getBlockZ();
                for (int i = 0; i < 5; i++) {
                    int dx = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                    int dz = ThreadLocalRandom.current().nextInt(-radius, radius + 1);
                    if (ThreadLocalRandom.current().nextDouble() >= pumpkinChance) continue;
                    int y = w.getHighestBlockYAt(px + dx, pz + dz);
                    Block top = w.getBlockAt(px + dx, y, pz + dz);
                    Block above = top.getRelative(BlockFace.UP);
                    if (above.getType().isAir() && top.getType().isSolid()) {
                        above.setType(Material.CARVED_PUMPKIN, false);
                    }
                }
            }
        }
    }

    private void runChristmasEffects() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player p : w.getPlayers()) {
                if (!p.isOnline()) continue;
                double heal = getChristmasHealAmount();
                if (heal > 0 && p.getHealth() < p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()) {
                    p.setHealth(Math.min(p.getHealth() + heal, p.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue()));
                }
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.COOKIE, 1 + ThreadLocalRandom.current().nextInt(2)));
                p.getInventory().addItem(new org.bukkit.inventory.ItemStack(Material.SNOWBALL, 2 + ThreadLocalRandom.current().nextInt(3)));
            }
        }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() { active = false; }

    @Override
    public String getDisplayName() { return "Holiday"; }
}
