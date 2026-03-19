package com.lostwilderness.rpgcore.calendar;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Firework;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Spawns fireworks at (0,0) overworld when the calendar year rolls over.
 * Scale: YEAR (normal), DECADE, CENTURY, MILLENNIUM.
 */
final class NewYearFireworkHandler {

    private enum Milestone { YEAR, DECADE, CENTURY, MILLENNIUM }

    private final Plugin plugin;

    NewYearFireworkHandler(Plugin plugin) {
        this.plugin = plugin;
    }

    void onDayAdvanced(CalendarServiceV2.CalendarSnapshot newSnap, CalendarServiceV2.CalendarSnapshot prevSnap) {
        if (prevSnap == null) return;
        int newYear = newSnap.date().getYear();
        int oldYear = prevSnap.date().getYear();
        if (newYear <= oldYear) return;

        World overworld = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
        if (overworld == null) return;

        int y = overworld.getHighestBlockYAt(0, 0) + 1;
        if (y <= 0) y = 64;
        Location base = new Location(overworld, 0.5, y, 0.5);
        Milestone m = milestone(newYear);
        runDisplay(base, m);
    }

    private static Milestone milestone(int year) {
        if (year % 1000 == 0) return Milestone.MILLENNIUM;
        if (year % 100 == 0) return Milestone.CENTURY;
        if (year % 10 == 0) return Milestone.DECADE;
        return Milestone.YEAR;
    }

    private void runDisplay(Location base, Milestone m) {
        int rockets = rocketsFor(m);
        int spread = spreadFor(m);
        int power = powerFor(m);
        long delayBetween = 10L;

        new BukkitRunnable() {
            int launched = 0;
            @Override
            public void run() {
                if (launched >= rockets) {
                    cancel();
                    return;
                }
                double dx = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * spread;
                double dz = (ThreadLocalRandom.current().nextDouble() - 0.5) * 2 * spread;
                Location loc = base.clone().add(dx, 0, dz);
                launchFirework(loc, power);
                launched++;
            }
        }.runTaskTimer(plugin, 20L, delayBetween);
    }

    private static int rocketsFor(Milestone m) {
        return switch (m) {
            case YEAR -> 3;
            case DECADE -> 8;
            case CENTURY -> 20;
            case MILLENNIUM -> 50;
        };
    }

    private static int spreadFor(Milestone m) {
        return switch (m) {
            case YEAR -> 2;
            case DECADE -> 5;
            case CENTURY -> 10;
            case MILLENNIUM -> 25;
        };
    }

    private static int powerFor(Milestone m) {
        return switch (m) {
            case YEAR -> 1;
            case DECADE, CENTURY, MILLENNIUM -> 2;
        };
    }

    private void launchFirework(Location loc, int power) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.setPower(Math.min(2, Math.max(0, power)));
        meta.addEffect(FireworkEffect.builder()
            .withColor(Color.RED, Color.WHITE, Color.YELLOW)
            .withFade(Color.ORANGE, Color.AQUA)
            .with(FireworkEffect.Type.BALL_LARGE)
            .trail(true)
            .flicker(true)
            .build());
        fw.setFireworkMeta(meta);
    }
}
