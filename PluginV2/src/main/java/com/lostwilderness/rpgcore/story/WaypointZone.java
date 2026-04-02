package com.lostwilderness.rpgcore.story;

/**
 * One arrival cylinder: entering it (with the Waypoints permission or optional Amos compass)
 * fires a BetonQuest event once.
 */
public final class WaypointZone {

    private final String id;
    private final String worldName;
    private final int blockX;
    private final int blockY;
    private final int blockZ;
    private final double radius;
    private final String waypointPermission;
    private final String betonquestEvent;
    private final String skipIfPlayerHasTag;
    private final boolean removeAmosCompass;

    public WaypointZone(
            String id,
            String worldName,
            int blockX,
            int blockY,
            int blockZ,
            double radius,
            String waypointPermission,
            String betonquestEvent,
            String skipIfPlayerHasTag,
            boolean removeAmosCompass
    ) {
        this.id = id;
        this.worldName = worldName;
        this.blockX = blockX;
        this.blockY = blockY;
        this.blockZ = blockZ;
        this.radius = radius;
        this.waypointPermission = waypointPermission;
        this.betonquestEvent = betonquestEvent;
        this.skipIfPlayerHasTag = skipIfPlayerHasTag;
        this.removeAmosCompass = removeAmosCompass;
    }

    public String getId() {
        return id;
    }

    public String getWorldName() {
        return worldName;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockY() {
        return blockY;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public double getRadius() {
        return radius;
    }

    public String getWaypointPermission() {
        return waypointPermission;
    }

    public String getBetonquestEvent() {
        return betonquestEvent;
    }

    public String getSkipIfPlayerHasTag() {
        return skipIfPlayerHasTag;
    }

    public boolean isRemoveAmosCompass() {
        return removeAmosCompass;
    }
}
