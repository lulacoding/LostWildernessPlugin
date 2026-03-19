package com.lostwilderness.rpgcore.boss.listener;

import com.lostwilderness.rpgcore.boss.ArenaManager;
import com.lostwilderness.rpgcore.boss.BossArena;
import com.lostwilderness.rpgcore.boss.Boundary;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class ArenaBoundaryListener implements Listener {

    private final ArenaManager arenaManager;

    public ArenaBoundaryListener(ArenaManager arenaManager) {
        this.arenaManager = arenaManager;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null || from.getWorld() == null || !from.getWorld().equals(to.getWorld())) return;
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) return;

        for (BossArena arena : arenaManager.getAll()) {
            if (!arena.isRestrictMovement()) continue;
            if (!arena.getBoundary().contains(from)) continue;
            if (arena.getBoundary().contains(to)) return; 

            event.setTo(from.clone());
            spawnImpactParticles(arena.getBoundary(), to);
            return;
        }
    }

    private void spawnImpactParticles(Boundary boundary, Location attemptedTo) {
        Location edge = boundary.clamp(attemptedTo);
        if (edge == null) return;
        double px = edge.getX();
        double py = edge.getY() + 0.5;
        double pz = edge.getZ();
        var world = boundary.getWorld();
        world.spawnParticle(Particle.ENCHANT, px, py, pz, 12, 0.4, 0.5, 0.4, 0.02);
        world.spawnParticle(Particle.END_ROD, px, py, pz, 4, 0.2, 0.3, 0.2, 0.01);
    }
}
