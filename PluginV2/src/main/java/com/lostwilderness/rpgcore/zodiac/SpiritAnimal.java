package com.lostwilderness.rpgcore.zodiac;

import org.bukkit.entity.EntityType;

public enum SpiritAnimal {
    WOLF("Wolf", EntityType.WOLF, "Gain +5% melee damage when near wolves or in packs"),
    FOX("Fox", EntityType.FOX, "Gain +10% speed at night"),
    BAT("Bat", EntityType.BAT, "Regeneration I when in darkness (light level ≤ 4)"),
    DOLPHIN("Dolphin", EntityType.DOLPHIN, "Enhanced Dolphin's Grace near water"),
    BEE("Bee", EntityType.BEE, "Crops grow faster in a 5-block radius around you"),
    CAT("Cat", EntityType.CAT, "Phantoms will never target you"),
    RABBIT("Rabbit", EntityType.RABBIT, "Jump Boost I when health is below 30%"),
    PARROT("Parrot", EntityType.PARROT, "Detect nearby hostile mobs (louder footsteps)"),
    AXOLOTL("Axolotl", EntityType.AXOLOTL, "Regeneration after combat ends"),
    PANDA("Panda", EntityType.PANDA, "Immunity to slowness effects"),
    TURTLE("Turtle", EntityType.TURTLE, "Resistance I when stationary for 3+ seconds"),
    OCELOT("Ocelot", EntityType.OCELOT, "Creepers remain neutral to you"),
    HORSE("Horse", EntityType.HORSE, "Speed boost when in open biomes (Plains, Savanna)"),
    IRON_GOLEM("Iron Golem", EntityType.IRON_GOLEM, "Increased knockback resistance");

    private final String displayName;
    private final EntityType entityType;
    private final String description;

    SpiritAnimal(String displayName, EntityType entityType, String description) {
        this.displayName = displayName;
        this.entityType = entityType;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public EntityType entityType() {
        return entityType;
    }

    public String description() {
        return description;
    }
}
