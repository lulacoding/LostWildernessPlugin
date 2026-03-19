package com.lostwilderness.rpgcore.party;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Party model - session-based grouping for cross-server gameplay.
 * Parties persist through portal transfers but dissolve on full disconnect.
 */
public final class Party {

    private final UUID id;
    private final UUID leader;
    private String name;
    private final Set<UUID> members;
    private final Set<UUID> invites;
    private final int maxSize;
    private final long createdAt;
    private String color;

    public Party(UUID id, UUID leader, String name, int maxSize) {
        this(id, leader, name, maxSize, System.currentTimeMillis(), "#00FF00");
    }

    public Party(UUID id, UUID leader, String name, int maxSize, long createdAt, String color) {
        this.id = id;
        this.leader = leader;
        this.name = name;
        this.members = new HashSet<>();
        this.members.add(leader);  // Leader is always a member
        this.invites = new HashSet<>();
        this.maxSize = maxSize;
        this.createdAt = createdAt;
        this.color = color;
    }

    // --- Query methods ---

    public boolean isMember(UUID playerUuid) {
        return members.contains(playerUuid);
    }

    public boolean isInvited(UUID playerUuid) {
        return invites.contains(playerUuid);
    }

    public boolean isLeader(UUID playerUuid) {
        return leader.equals(playerUuid);
    }

    public boolean isFull() {
        return members.size() >= maxSize;
    }

    public int size() {
        return members.size();
    }

    // --- Mutation methods ---

    public void addMember(UUID playerUuid) {
        if (!isFull()) {
            members.add(playerUuid);
        }
    }

    public void removeMember(UUID playerUuid) {
        members.remove(playerUuid);
    }

    public void addInvite(UUID playerUuid) {
        invites.add(playerUuid);
    }

    public void removeInvite(UUID playerUuid) {
        invites.remove(playerUuid);
    }

    // --- Getters ---

    public UUID getId() {
        return id;
    }

    public UUID getLeader() {
        return leader;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<UUID> getMembers() {
        return new HashSet<>(members);
    }

    public Set<UUID> getInvites() {
        return new HashSet<>(invites);
    }

    public int getMaxSize() {
        return maxSize;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Party)) return false;
        Party party = (Party) o;
        return id.equals(party.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Party{" +
                "id=" + id +
                ", leader=" + leader +
                ", name='" + name + '\'' +
                ", members=" + members.size() +
                ", invites=" + invites.size() +
                ", maxSize=" + maxSize +
                '}';
    }
}
