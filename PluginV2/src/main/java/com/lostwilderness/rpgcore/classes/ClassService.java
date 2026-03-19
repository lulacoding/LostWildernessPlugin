package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.skills.SkillXpService;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Optional;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class ClassService {

    private final Plugin plugin;
    private final PlayerClassRepository repository;
    private final SkillXpService skillXpService;
    private final ConfigurationSection classConfig;

    public ClassService(Plugin plugin,
            PlayerClassRepository repository,
            SkillXpService skillXpService,
            ConfigurationSection classConfig) {
        this.plugin = plugin;
        this.repository = repository;
        this.skillXpService = skillXpService;
        this.classConfig = classConfig;
    }

    public CompletableFuture<Optional<PlayerClass>> getClass(UUID playerUuid) {
        return repository.find(playerUuid);
    }

    public CompletableFuture<Boolean> hasClass(UUID playerUuid) {
        return repository.exists(playerUuid);
    }

    public CompletableFuture<Void> setClass(UUID playerUuid, PlayerClass playerClass) {
        long now = System.currentTimeMillis();
        return repository.save(playerUuid, playerClass, now)
                .thenCompose(v -> hasStarterXpConfigured(playerClass)
                        ? repository.enqueueStarterXpGrant(playerUuid, playerClass, now)
                        : CompletableFuture.completedFuture(null))
                .thenCompose(v -> tryApplyPendingStarterXp(playerUuid))
                .thenRun(() -> {
                    Runnable syncWork = () -> {
                        Player p = Bukkit.getPlayer(playerUuid);
                        if (p != null && p.isOnline()) {
                            p.sendMessage("§aYou have chosen the class: §f" + playerClass.name());
                        }
                    };
                    if (Bukkit.isPrimaryThread()) {
                        syncWork.run();
                    } else {
                        Bukkit.getScheduler().runTask(plugin, syncWork);
                    }
                });
    }

    public CompletableFuture<Boolean> tryApplyPendingStarterXp(UUID playerUuid) {
        return repository.findPendingStarterXpGrant(playerUuid)
                .thenCompose(pending -> {
                    if (pending.isEmpty()) {
                        return CompletableFuture.completedFuture(false);
                    }

                    CompletableFuture<Boolean> grantedFuture = new CompletableFuture<>();
                    Runnable syncWork = () -> grantedFuture.complete(applyStarterXpNow(playerUuid, pending.get()));
                    if (Bukkit.isPrimaryThread()) {
                        syncWork.run();
                    } else {
                        Bukkit.getScheduler().runTask(plugin, syncWork);
                    }

                    return grantedFuture.thenCompose(granted -> {
                        if (!granted) {
                            return CompletableFuture.completedFuture(false);
                        }
                        return repository.markStarterXpGrantClaimed(playerUuid, System.currentTimeMillis());
                    });
                });
    }

    private boolean applyStarterXpNow(UUID playerUuid, PlayerClass playerClass) {
        if (skillXpService == null || !skillXpService.isAvailable() || classConfig == null)
            return false;
        ConfigurationSection section = classConfig.getConfigurationSection(playerClass.name());
        if (section == null)
            return false;
        ConfigurationSection starter = section.getConfigurationSection("starter-xp");
        if (starter == null)
            return false;

        Map<String, Double> grants = new LinkedHashMap<>();
        for (String skillKey : starter.getKeys(false)) {
            double amount = starter.getDouble(skillKey, 0.0);
            if (amount > 0) {
                grants.put(skillKey, amount);
            }
        }
        if (grants.isEmpty()) {
            return false;
        }
        return skillXpService.grantXpBatch(playerUuid, grants);
    }

    private boolean hasStarterXpConfigured(PlayerClass playerClass) {
        if (classConfig == null)
            return false;
        ConfigurationSection section = classConfig.getConfigurationSection(playerClass.name());
        if (section == null)
            return false;
        ConfigurationSection starter = section.getConfigurationSection("starter-xp");
        if (starter == null)
            return false;
        for (String skillKey : starter.getKeys(false)) {
            if (starter.getDouble(skillKey, 0.0) > 0) {
                return true;
            }
        }
        return false;
    }
}
