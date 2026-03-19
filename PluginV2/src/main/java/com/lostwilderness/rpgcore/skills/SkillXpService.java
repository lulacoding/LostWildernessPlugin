package com.lostwilderness.rpgcore.skills;

import java.util.Map;
import java.util.UUID;

/**
 * Shared skill XP grant service so modules can use one consistent entry point.
 */
public final class SkillXpService {

    private final AuraSkillsBridge auraBridge;

    public SkillXpService(AuraSkillsBridge auraBridge) {
        this.auraBridge = auraBridge;
    }

    public boolean isAvailable() {
        return auraBridge != null && auraBridge.isAvailable();
    }

    public boolean grantXp(UUID playerUuid, String skillKey, double amount) {
        return auraBridge != null && auraBridge.addSkillXp(playerUuid, skillKey, amount);
    }

    public boolean grantXpBatch(UUID playerUuid, Map<String, Double> grants) {
        return auraBridge != null && auraBridge.addSkillXpBatch(playerUuid, grants);
    }
}
