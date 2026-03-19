package com.lostwilderness.rpgcore.party;

import org.bukkit.entity.Player;

import java.util.Set;
import java.util.UUID;

/**
 * Party service - manages cross-server party state and operations.
 */
public interface PartyService {

    // --- Core operations ---

    /**
     * Create a new party with the given leader.
     * Auto-creates party with default name if none provided.
     */
    Party createParty(UUID leaderUuid, String name);

    /**
     * Disband a party (leader only).
     */
    void disbandParty(UUID leaderUuid);

    /**
     * Invite a player to the party.
     * If leader has no party, creates one automatically.
     */
    void invitePlayer(UUID leaderUuid, UUID targetUuid);

    /**
     * Accept an invite to join a party.
     */
    void acceptInvite(UUID playerUuid, UUID partyId);

    /**
     * Leave the current party.
     * If leader leaves, party is disbanded.
     */
    void leaveParty(UUID playerUuid);

    /**
     * Kick a member from the party (leader only).
     */
    void kickMember(UUID leaderUuid, UUID targetUuid);

    // --- Queries ---

    /**
     * Get the party a player belongs to.
     * @return Party or null if not in a party
     */
    Party getParty(UUID playerUuid);

    /**
     * Get a party by its ID.
     */
    Party getPartyById(UUID partyId);

    /**
     * Get all member UUIDs in a player's party.
     */
    Set<UUID> getPartyMembers(UUID playerUuid);

    /**
     * Check if player is in a party.
     */
    boolean isInParty(UUID playerUuid);

    /**
     * Check if two players are in the same party.
     */
    boolean areInSameParty(UUID player1, UUID player2);

    /**
     * Get the party color for display.
     */
    String getPartyColor(UUID partyId);

    /**
     * Get all pending invites for a player.
     */
    Set<UUID> getPlayerInvites(UUID playerUuid);

    // --- Display ---

    /**
     * Update visual display for all party members (scoreboard teams, glow).
     */
    void updatePartyDisplay(UUID partyId);

    /**
     * Update display for a single player.
     */
    void updatePlayerDisplay(Player player);

    /**
     * Cache a party in memory (used after loading from DB).
     */
    void cacheParty(Party party);
}
