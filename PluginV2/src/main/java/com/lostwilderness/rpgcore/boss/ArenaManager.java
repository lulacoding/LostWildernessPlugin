package com.lostwilderness.rpgcore.boss;

import org.bukkit.Location;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry of boss arenas. Thread-safe.
 */
public class ArenaManager {

    private final ConcurrentHashMap<String, BossArena> arenasById = new ConcurrentHashMap<>();

    /**
     * Registers an arena. Replaces any existing arena with the same id.
     */
    public void register(BossArena arena) {
        if (arena == null) return;
        arenasById.put(arena.getId(), arena);
    }

    /**
     * Unregisters the arena with the given id.
     */
    public void unregister(String id) {
        if (id != null) arenasById.remove(id);
    }

    /**
     * Returns the arena with the given id.
     */
    public Optional<BossArena> getById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(arenasById.get(id));
    }

    /**
     * Returns the first arena that contains the given location.
     */
    public Optional<BossArena> getByLocation(Location loc) {
        if (loc == null) return Optional.empty();
        for (BossArena arena : arenasById.values()) {
            if (arena.getBoundary().contains(loc)) return Optional.of(arena);
        }
        return Optional.empty();
    }

    /**
     * Returns all registered arenas.
     */
    public Collection<BossArena> getAll() {
        return Collections.unmodifiableCollection(arenasById.values());
    }

    public void clear() {
        arenasById.clear();
    }
}
