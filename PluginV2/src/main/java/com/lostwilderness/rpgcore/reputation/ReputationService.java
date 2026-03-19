package com.lostwilderness.rpgcore.reputation;

import com.lostwilderness.rpgcore.classes.ClassService;
import com.lostwilderness.rpgcore.classes.PlayerClass;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ReputationService {

    private final ReputationRepository repository;
    private final ClassService classService;
    private final Map<UUID, Map<Faction, Integer>> cache = new ConcurrentHashMap<>();
    private static final long TIMEOUT_MS = 5000;

    public ReputationService(ReputationRepository repository, ClassService classService) {
        this.repository = repository;
        this.classService = classService;
    }

    public CompletableFuture<Integer> getPointsAsync(UUID playerUuid, Faction faction) {
        Map<Faction, Integer> map = cache.get(playerUuid);
        if (map != null && map.containsKey(faction)) {
            return CompletableFuture.completedFuture(map.get(faction));
        }
        return repository.getPoints(playerUuid, faction).thenApply(points -> {
            cache.computeIfAbsent(playerUuid, k -> new EnumMap<>(Faction.class)).put(faction, points);
            return points;
        });
    }

    public int getPoints(UUID playerUuid, Faction faction) {
        try {
            return getPointsAsync(playerUuid, faction).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0;
        }
    }

    public CompletableFuture<Void> addReputation(UUID playerUuid, Faction faction, int delta) {
        return getPointsAsync(playerUuid, faction).thenCompose(current -> {
            int adjusted = delta;
            if (delta > 0 && classService != null && faction != null) {
                try {
                    var clsFuture = classService.getClass(playerUuid);
                    PlayerClass playerClass = clsFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS).orElse(null);
                    if (playerClass != null && playerClass.getAlignedFaction() == faction) {
                        adjusted = (int) Math.round(delta * 1.2); // +20% aligned faction bonus
                    }
                } catch (Exception ignored) {
                }
            }

            int next = current + adjusted;
            cache.computeIfAbsent(playerUuid, k -> new EnumMap<>(Faction.class)).put(faction, next);
            
            // Notify if significant change or just a small message
            org.bukkit.entity.Player player = org.bukkit.Bukkit.getPlayer(playerUuid);
            if (player != null && faction.isGood() != null) {
                if (adjusted > 0) {
                    player.sendMessage(org.bukkit.ChatColor.GRAY + "Your standing with " + 
                        faction.getColor() + faction.getDisplayName() + org.bukkit.ChatColor.GRAY + " has improved.");
                } else if (adjusted < 0) {
                    player.sendMessage(org.bukkit.ChatColor.GRAY + "Your standing with " + 
                        faction.getColor() + faction.getDisplayName() + org.bukkit.ChatColor.GRAY + " has worsened.");
                }
            }
            
            return repository.setPoints(playerUuid, faction, next);
        });
    }

    /**
     * Calculates "Honor" based on good vs bad faction points.
     * Scale: -1000 to +1000
     */
    public CompletableFuture<Integer> getHonorScore(UUID playerUuid) {
        return repository.getAllPoints(playerUuid).thenApply(pointsMap -> {
            int goodSum = 0;
            int badSum = 0;
            for (Map.Entry<Faction, Integer> entry : pointsMap.entrySet()) {
                Boolean isGood = entry.getKey().isGood();
                if (isGood == null) continue;
                if (isGood) goodSum += entry.getValue();
                else badSum += entry.getValue();
            }
            int score = goodSum - badSum;
            return Math.max(-1000, Math.min(1000, score));
        });
    }

    public String getHonorTitle(int score) {
        if (score >= 800) return "Hero";
        if (score >= 400) return "Noble";
        if (score >= 100) return "Honorable";
        if (score > -100) return "Neutral";
        if (score > -400) return "Dishonorable";
        if (score > -800) return "Villain";
        return "Outlaw";
    }

    public void invalidateCache(UUID playerUuid) {
        cache.remove(playerUuid);
    }
}
