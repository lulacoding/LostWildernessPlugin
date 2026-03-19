package com.lostwilderness.rpgcore.events.impl;

import com.lostwilderness.rpgcore.events.DailyWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Paranoia: Eclipse atmosphere; plays random horror sounds. Started via Eclipse cascade or when Eclipse is active.
 */
public final class ParanoiaEvent implements DailyWorldEvent, Listener {

    private final Plugin plugin;
    private final EclipseEvent eclipseEvent;
    private volatile boolean active = false;
    private final Map<UUID, BukkitRunnable> tasks = new ConcurrentHashMap<>();
    private BukkitRunnable stopTask;

    public ParanoiaEvent(Plugin plugin, EclipseEvent eclipseEvent) {
        this.plugin = plugin;
        this.eclipseEvent = eclipseEvent;
    }

    @Override
    public void onCalendarDay(int day, org.bukkit.World world) {
        if (active) return;
        if (eclipseEvent != null && !eclipseEvent.isActive()) return;
        start();
    }

    private void start() {
        World world = Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .findFirst().orElse(null);
        if (world == null) return;
        active = true;
        for (Player p : world.getPlayers()) startFor(p);
        if (stopTask != null) stopTask.cancel();
        stopTask = new BukkitRunnable() {
            @Override
            public void run() {
                forceEndEarly();
            }
        };
        stopTask.runTaskLater(plugin, 24000L);
    }

    private void startFor(Player player) {
        if (tasks.containsKey(player.getUniqueId())) return;
        int minSec = plugin.getConfig().getInt("events.paranoia.min-interval-seconds", 20);
        int maxSec = plugin.getConfig().getInt("events.paranoia.max-interval-seconds", 60);
        if (maxSec < minSec) { int t = maxSec; maxSec = minSec; minSec = t; }
        List<Sound> soundList = loadSounds();
        if (soundList.isEmpty()) {
            soundList = List.of(Sound.AMBIENT_CAVE, Sound.ENTITY_ENDERMAN_STARE, Sound.ENTITY_WITHER_AMBIENT);
        }
        final List<Sound> sounds = soundList;
        int delaySec = minSec + (int) (Math.random() * (maxSec - minSec + 1));
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!active || !player.isOnline()) {
                    cancelFor(player.getUniqueId());
                    return;
                }
                Sound s = sounds.get((int) (Math.random() * sounds.size()));
                player.playSound(player.getLocation(), s, 1.0f, 1.0f);
                cancelFor(player.getUniqueId());
                startFor(player);
            }
        };
        task.runTaskLater(plugin, delaySec * 20L);
        tasks.put(player.getUniqueId(), task);
    }

    private List<Sound> loadSounds() {
        List<String> names = plugin.getConfig().getStringList("events.paranoia.sounds");
        List<Sound> out = new ArrayList<>();
        if (names == null) return out;
        for (String name : names) {
            if (name == null) continue;
            try {
                out.add(Sound.valueOf(name.trim().toUpperCase()));
            } catch (IllegalArgumentException ignored) {}
        }
        return out;
    }

    private void cancelFor(UUID uuid) {
        BukkitRunnable t = tasks.remove(uuid);
        if (t != null) t.cancel();
    }

    @Override
    public void forceEndEarly() {
        if (!active && tasks.isEmpty()) return;
        active = false;
        for (UUID id : List.copyOf(tasks.keySet())) cancelFor(id);
        if (stopTask != null) { stopTask.cancel(); stopTask = null; }
    }

    @Override
    public boolean isActive() { return active; }

    @Override
    public String getDisplayName() { return "Paranoia"; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        if (active) startFor(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        cancelFor(e.getPlayer().getUniqueId());
    }
}
