package com.lostwilderness.rpgcore.events;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared context for seasonal events: calendar, overworlds, disabled worlds, eligible players.
 * Supports per-world day offsets so Survival and Amplified (or other dimensions) can have
 * events at different effective days. Informs event logic without reaching into globals.
 */
public final class EventContext {

    private final Plugin plugin;
    private final CalendarServiceV2 calendar;
    private volatile Set<String> disabledWorlds = Set.of();
    private volatile Map<String, Integer> worldDayOffsets = Map.of();
    private volatile World currentWorld;

    public EventContext(Plugin plugin, CalendarServiceV2 calendar) {
        this.plugin = plugin;
        this.calendar = calendar;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public CalendarServiceV2 getCalendar() {
        return calendar;
    }

    public void setDisabledWorlds(Collection<String> worlds) {
        if (worlds == null || worlds.isEmpty()) {
            this.disabledWorlds = Set.of();
            return;
        }
        this.disabledWorlds = worlds.stream()
            .filter(Objects::nonNull)
            .map(s -> s.trim().toLowerCase(Locale.ROOT))
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toUnmodifiableSet());
    }

    public void setWorldDayOffsets(Map<String, Integer> offsets) {
        this.worldDayOffsets = offsets != null ? Map.copyOf(offsets) : Map.of();
    }

    /** Effective day for this world = global calendar day + offset (default 0). */
    public int getWorldDayOffset(World w) {
        if (w == null) return 0;
        return worldDayOffsets.getOrDefault(w.getName().toLowerCase(Locale.ROOT), 0);
    }

    /** Set during per-world event dispatch so events know which dimension they're running for. */
    public void setCurrentWorld(World w) {
        this.currentWorld = w;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }

    public List<World> getOverworlds() {
        return Bukkit.getWorlds().stream()
            .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
            .toList();
    }

    public boolean isWorldEnabled(World w) {
        if (w == null) return false;
        return !disabledWorlds.contains(w.getName().toLowerCase(Locale.ROOT));
    }

    public boolean isPlayerEnabled(Player p) {
        return p != null && isWorldEnabled(p.getWorld());
    }

    public List<Player> getEligiblePlayers() {
        return new java.util.ArrayList<>(Bukkit.getOnlinePlayers().stream()
            .filter(this::isPlayerEnabled)
            .toList());
    }
}
