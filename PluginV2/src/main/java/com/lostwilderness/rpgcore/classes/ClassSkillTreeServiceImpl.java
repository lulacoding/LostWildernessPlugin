package com.lostwilderness.rpgcore.classes;

import com.lostwilderness.rpgcore.skills.AuraSkillsBridge;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class ClassSkillTreeServiceImpl implements ClassSkillTreeService {

    private static final long SANCTIFIED_GROUND_COOLDOWN_MS = 600_000L; // 10 minutes

    private final Plugin plugin;
    private final AuraSkillsBridge auraBridge;
    private final PlayerClassRepository repository;
    private final Map<UUID, Long> sanctifiedGroundCooldowns = new ConcurrentHashMap<>();

    // Unlock tree definitions (all 5 classes, 7 unlocks each)
    private static final Map<PlayerClass, List<ClassUnlock>> UNLOCK_TREES = Map.of(
        PlayerClass.CELESTIAL_TEMPLAR, List.of(
            new ClassUnlock(1, "Smite", "Lightning AoE", true),
            new ClassUnlock(5, "Holy Aura", "Regen after undead kill", false),
            new ClassUnlock(10, "Blessed Armor", "Resistance I in full armor", false),
            new ClassUnlock(15, "Divine Strike", "Smite hits 2 targets", true),
            new ClassUnlock(20, "Radiant Shield", "Reflect 10% damage", false),
            new ClassUnlock(30, "Sanctified Ground", "Survive death once", false),
            new ClassUnlock(50, "Ascension", "Chain lightning Smite", true)
        ),
        PlayerClass.WILDLAND_RANGER, List.of(
            new ClassUnlock(1, "Swift Feet", "Speed I in forest biomes", false),
            new ClassUnlock(5, "Eagle Eye", "Arrow damage +15%", false),
            new ClassUnlock(10, "Hunter's Mark", "Mark target for bonus damage", true),
            new ClassUnlock(15, "Camouflage", "Invisibility when still", false),
            new ClassUnlock(20, "Pack Leader", "Tamed pets +30% HP +15% damage", false),
            new ClassUnlock(30, "Phantom Arrow", "Arrows pass through blocks 20%", false),
            new ClassUnlock(50, "Ranger's Quiver", "Class Mastery Item: Infinity bow", false)
        ),
        PlayerClass.REDEEMED_ARTIFICER, List.of(
            new ClassUnlock(1, "Slam", "Knockback + Slowness AoE", true),
            new ClassUnlock(5, "Efficient Repairs", "Anvil costs -2 levels", false),
            new ClassUnlock(10, "Reinforced Tools", "Tools 20% less durability loss", false),
            new ClassUnlock(15, "Power Slam", "Slam radius +50%, Slowness III", true),
            new ClassUnlock(20, "Master Crafter", "15% extra item when crafting", false),
            new ClassUnlock(30, "Arcane Forge", "Enchanting +1 option, no lapis", false),
            new ClassUnlock(50, "Eternal Hammer", "Class Mastery Item: Repair tool", false)
        ),
        PlayerClass.CORRUPTED_CULTIST, List.of(
            new ClassUnlock(1, "Dash", "Teleport forward 8 blocks", true),
            new ClassUnlock(5, "Lifesteal", "5% chance heal 1 heart", false),
            new ClassUnlock(10, "Shadow Step", "Dash range 12 blocks", true),
            new ClassUnlock(15, "Venom Strike", "15% poison on hit", false),
            new ClassUnlock(20, "Dark Pact", "Low HP triggers Strength + Speed", false),
            new ClassUnlock(30, "Soul Drain", "Lifesteal 15% chance 1.5 hearts", false),
            new ClassUnlock(50, "Mirror Shard", "Class Mastery Item: Swap positions", false)
        ),
        PlayerClass.DESTROYER_BERSERKER, List.of(
            new ClassUnlock(1, "War Cry", "Allies Resistance II + mob knockback", true),
            new ClassUnlock(5, "Berserker's Rage", "Strength I at low HP", false),
            new ClassUnlock(10, "Iron Skin", "Resistance I with axe", false),
            new ClassUnlock(15, "Brutal War Cry", "War Cry grants Strength I", true),
            new ClassUnlock(20, "Unstoppable", "Immune to mob knockback", false),
            new ClassUnlock(30, "Bloodlust", "Kills refresh Speed I", false),
            new ClassUnlock(50, "Ragnarok Axe", "Class Mastery Item: Bloodlust axe", false)
        )
    );

    // Mastery skill keys per class
    private static final Map<PlayerClass, String> MASTERY_SKILLS = Map.of(
        PlayerClass.CELESTIAL_TEMPLAR, "templar_mastery",
        PlayerClass.WILDLAND_RANGER, "ranger_mastery",
        PlayerClass.REDEEMED_ARTIFICER, "artificer_mastery",
        PlayerClass.CORRUPTED_CULTIST, "cultist_mastery",
        PlayerClass.DESTROYER_BERSERKER, "berserker_mastery"
    );

    public ClassSkillTreeServiceImpl(Plugin plugin, AuraSkillsBridge auraBridge, PlayerClassRepository repository) {
        this.plugin = plugin;
        this.auraBridge = auraBridge;
        this.repository = repository;
        UltimateItemBuilder.initialize(plugin);
    }

    @Override
    public String getMasterySkillKey(PlayerClass playerClass) {
        return MASTERY_SKILLS.getOrDefault(playerClass, "unknown");
    }

    @Override
    public int getMasteryLevel(UUID playerUuid, PlayerClass playerClass) {
        if (auraBridge == null || !auraBridge.isAvailable()) {
            return 0;
        }
        String skillKey = getMasterySkillKey(playerClass);
        return auraBridge.getSkillLevel(playerUuid, skillKey);
    }

    @Override
    public boolean isUnlocked(UUID playerUuid, PlayerClass playerClass, int requiredLevel) {
        return getMasteryLevel(playerUuid, playerClass) >= requiredLevel;
    }

    @Override
    public Optional<ClassUnlock> getNextUnlock(UUID playerUuid, PlayerClass playerClass) {
        int currentLevel = getMasteryLevel(playerUuid, playerClass);
        List<ClassUnlock> unlocks = UNLOCK_TREES.getOrDefault(playerClass, List.of());

        return unlocks.stream()
            .filter(unlock -> unlock.requiredLevel() > currentLevel)
            .min(Comparator.comparingInt(ClassUnlock::requiredLevel));
    }

    @Override
    public List<ClassUnlockStatus> getAllUnlocks(UUID playerUuid, PlayerClass playerClass) {
        int currentLevel = getMasteryLevel(playerUuid, playerClass);
        List<ClassUnlock> unlocks = UNLOCK_TREES.getOrDefault(playerClass, List.of());

        return unlocks.stream()
            .map(unlock -> new ClassUnlockStatus(unlock, currentLevel >= unlock.requiredLevel(), currentLevel))
            .toList();
    }

    @Override
    public CompletableFuture<Boolean> hasUltimateItem(UUID playerUuid, PlayerClass playerClass) {
        return repository.hasUltimateItem(playerUuid, playerClass);
    }

    @Override
    public CompletableFuture<Boolean> grantUltimateItem(UUID playerUuid, PlayerClass playerClass) {
        // Check eligibility: level 50+ and not already granted
        int level = getMasteryLevel(playerUuid, playerClass);
        if (level < 50) {
            return CompletableFuture.completedFuture(false);
        }

        return hasUltimateItem(playerUuid, playerClass).thenCompose(alreadyHas -> {
            if (alreadyHas) {
                return CompletableFuture.completedFuture(false);
            }

            // Build and give the item
            Player player = Bukkit.getPlayer(playerUuid);
            if (player == null || !player.isOnline()) {
                return CompletableFuture.completedFuture(false);
            }

            UltimateItemBuilder.giveUltimateItem(player, playerClass);

            // Mark as granted in DB
            return repository.markUltimateItemGiven(playerUuid, playerClass).thenApply(unused -> {
                // Send title + sound
                player.sendTitle("§6§lUltimate Unlock!", "§e" + getUltimateItemName(playerClass), 10, 70, 20);
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                plugin.getLogger().info("[classes] Granted Ultimate item (" + playerClass + ") to " + player.getName());
                return true;
            });
        });
    }

    @Override
    public boolean isSanctifiedGroundAvailable(UUID playerUuid) {
        Long lastUse = sanctifiedGroundCooldowns.get(playerUuid);
        if (lastUse == null) {
            return true;
        }
        long elapsed = System.currentTimeMillis() - lastUse;
        return elapsed >= SANCTIFIED_GROUND_COOLDOWN_MS;
    }

    @Override
    public void triggerSanctifiedGround(UUID playerUuid) {
        sanctifiedGroundCooldowns.put(playerUuid, System.currentTimeMillis());
    }

    private String getUltimateItemName(PlayerClass playerClass) {
        return switch (playerClass) {
            case CELESTIAL_TEMPLAR -> "Holy Avenger";
            case WILDLAND_RANGER -> "Ranger's Quiver";
            case REDEEMED_ARTIFICER -> "Eternal Hammer";
            case CORRUPTED_CULTIST -> "Mirror Shard";
            case DESTROYER_BERSERKER -> "Ragnarok Axe";
        };
    }
}
