package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * New Year (first day of Spring): title, gifts (fireworks, gunpowder), optional firework phase for overworld players.
 */
public final class NewYearEvent implements DailyWorldEvent {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile boolean active = false;
    private BukkitRunnable fireworkPhaseTask;

    public NewYearEvent(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    private boolean isEnabled() {
        return plugin.getConfig().getBoolean("events.new-year.enabled", true);
    }

    private int getGiftFireworksMin() {
        return plugin.getConfig().getInt("events.new-year.gift-fireworks-min", 3);
    }

    private int getGiftFireworksMax() {
        return plugin.getConfig().getInt("events.new-year.gift-fireworks-max", 8);
    }

    private int getGiftGunpowderMin() {
        return plugin.getConfig().getInt("events.new-year.gift-gunpowder-min", 1);
    }

    private int getGiftGunpowderMax() {
        return plugin.getConfig().getInt("events.new-year.gift-gunpowder-max", 3);
    }

    private long getFireworkPhaseDurationTicks() {
        return plugin.getConfig().getLong("events.new-year.firework-phase-duration-ticks", 60L);
    }

    private static boolean isFirstDayOfSpring(LocalDate date) {
        return date.getMonthValue() == 3 && date.getDayOfMonth() == 1;
    }

    @Override
    public void onCalendarDay(int day, World world) {
        if (world == null) world = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (world == null) return;
        CalendarServiceV2.CalendarSnapshot snap = calendar.getCurrentSnapshot();
        if (!isFirstDayOfSpring(snap.date())) return;
        if (!isEnabled()) return;
        World firstOverworld = Bukkit.getWorlds().stream().filter(w -> w.getEnvironment() == World.Environment.NORMAL).findFirst().orElse(null);
        if (firstOverworld == null || world != firstOverworld) return;

        active = true;
        int fMin = getGiftFireworksMin();
        int fMax = Math.max(fMin, getGiftFireworksMax());
        int gMin = getGiftGunpowderMin();
        int gMax = Math.max(gMin, getGiftGunpowderMax());
        int fireworksCount = fMin + (fMax > fMin ? ThreadLocalRandom.current().nextInt(fMax - fMin + 1) : 0);
        int gunpowderCount = gMin + (gMax > gMin ? ThreadLocalRandom.current().nextInt(gMax - gMin + 1) : 0);

        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            for (Player p : w.getPlayers()) {
                p.sendTitle("§e§lHappy New Year!", "§6Spring has begun! §7Enjoy the festivities.", 20, 80, 40);
                p.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET, fireworksCount));
                p.getInventory().addItem(new ItemStack(Material.GUNPOWDER, gunpowderCount));
            }
        }

        long duration = Math.max(20L, getFireworkPhaseDurationTicks());
        List<Player> overworldPlayers = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .flatMap(w -> w.getPlayers().stream())
            .collect(Collectors.toList());
        if (!overworldPlayers.isEmpty()) {
            fireworkPhaseTask = new BukkitRunnable() {
                long ticksLeft = duration;
                @Override
                public void run() {
                    if (ticksLeft <= 0 || overworldPlayers.isEmpty()) {
                        cancel();
                        fireworkPhaseTask = null;
                        return;
                    }
                    Player pick = overworldPlayers.get(ThreadLocalRandom.current().nextInt(overworldPlayers.size()));
                    if (pick.isOnline() && pick.getWorld().getEnvironment() == World.Environment.NORMAL) {
                        org.bukkit.Location loc = pick.getLocation().add(0, 1, 0);
                        spawnCelebrationFirework(loc);
                    }
                    ticksLeft -= 40L;
                }
            };
            fireworkPhaseTask.runTaskTimer(plugin, 40L, 40L);
        }
        active = false;
    }

    private void spawnCelebrationFirework(org.bukkit.Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(1);
        meta.addEffect(org.bukkit.FireworkEffect.builder()
            .withColor(org.bukkit.Color.RED, org.bukkit.Color.WHITE, org.bukkit.Color.YELLOW)
            .with(org.bukkit.FireworkEffect.Type.BALL)
            .trail(true)
            .build());
        fw.setFireworkMeta(meta);
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public void forceEndEarly() {
        if (fireworkPhaseTask != null) {
            fireworkPhaseTask.cancel();
            fireworkPhaseTask = null;
        }
        active = false;
    }

    @Override
    public String getDisplayName() { return "New Year"; }
}
