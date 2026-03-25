package com.lostwilderness.rpgcore.pets;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 32 pet personalities with passive effects.
 * Phase 1: Enum only, no effects yet.
 * Phase 4: Effects implemented.
 */
public enum PetPersonality {
    // Event-driven (6)
    LOYAL_SENTINEL("Loyal Sentinel", "Barks to warn of nearby hostile mobs"),
    LUCKY_CHARM("Lucky Charm", "Increases drop rates from mob kills"),
    ARROW_SHIELD("Arrow Shield", "Blocks one projectile per minute"),
    CHAOS_SPARK("Chaos Spark", "Prevents one creeper detonation nearby"),
    ETERNAL_YOUTH("Eternal Youth", "Prevents pet death once per day"),
    GUARDIAN_ANGEL("Guardian Angel", "Saves owner from fatal blow once per day"),

    // Proximity passive (6)
    HEALING_TOUCH("Healing Touch", "Slowly heals nearby tamed pets"),
    FISHER_KING("Fisher King", "Increases fishing luck when nearby"),
    DEEP_DIVER("Deep Diver", "Grants water breathing to owner"),
    CALM_WHISPERER("Calm Whisperer", "Pacifies hostile mobs within range"),
    LIGHT_BEACON("Light Beacon", "Emits soft light particles in darkness"),
    STAR_GAZER("Star Gazer", "Grants stat buffs under clear night sky"),

    // Scheduled effects (6)
    TREASURE_SNIFFER("Treasure Sniffer", "Randomly digs up buried loot"),
    CROP_WHISPERER("Crop Whisperer", "Speeds up growth of nearby crops"),
    ORE_SEEKER("Ore Seeker", "Points toward closest ore vein"),
    FOOD_FORAGER("Food Forager", "Brings random food items to owner"),
    BLOCK_BUDDY("Block Buddy", "Picks up and returns loose blocks"),
    WEATHER_PROPHET("Weather Prophet", "Predicts upcoming weather changes"),

    // Cosmetic/Special (14)
    SHADOW_STALKER("Shadow Stalker", "Turns invisible in low light"),
    SPEED_DEMON("Speed Demon", "Sudden 5-second sprint bursts"),
    MELODY_MAKER("Melody Maker", "Plays random note-block tunes"),
    FIRE_DANCER("Fire Dancer", "Gains fire resistance near flames"),
    WALL_WALKER("Wall Walker", "Climbs vertical blocks like a spider"),
    BLINK("Blink", "Short teleport back to owner when too far"),
    DANCE_MASTER("Dance Master", "Performs particle dances on command"),
    ECHO_VOICE("Echo Voice", "Repeats player emotes and sounds"),
    MINI_EXPLORER("Mini Explorer", "Fills small map areas automatically"),
    DAWN_HERALD("Dawn Herald", "Notifies owner at sunrise"),
    DREAM_WEAVER("Dream Weaver", "Grants random positive effect on sleep"),
    STORM_RIDER("Storm Rider", "Gains speed and joy in rain"),
    MIMIC_MASTER("Mimic Master", "Copies owner movements cosmetically"),
    PRANKSTER("Prankster", "Random harmless pranks and antics");

    private final String displayName;
    private final String description;

    PetPersonality(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Randomly select a personality for a newly tamed pet.
     */
    public static PetPersonality randomPersonality() {
        PetPersonality[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }
}
