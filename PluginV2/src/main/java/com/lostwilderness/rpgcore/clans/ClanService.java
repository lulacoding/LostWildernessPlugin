package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.model.Clan;
import com.lostwilderness.rpgcore.clans.model.Invite;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service interface for clan operations. Implementations may block on async DB.
 */
public interface ClanService {

    Clan createClan(String name, String colorHex, UUID leaderUuid);

    Clan getClanById(UUID clanId);

    Clan getClanByName(String name);

    UUID getClanOfPlayer(UUID playerUuid);

    Invite invitePlayerToClan(UUID clanId, UUID playerUuid, UUID inviterUuid);

    List<Invite> getPendingInvites(UUID playerUuid);

    void addMemberToClan(UUID clanId, UUID playerUuid, String rank);

    void removeMemberFromClan(UUID clanId, UUID playerUuid);

    void updateClanColor(UUID clanId, String colorHex);

    int getClanMemberCount(UUID clanId);

    String getMemberRank(UUID clanId, UUID playerUuid);

    void addClanRelation(UUID clanId, UUID targetClanId, String relation);

    void removeClanAndData(UUID clanId);

    List<Clan> getAllClans();

    List<Map.Entry<UUID, String>> getClanMembersWithRanks(UUID clanId);

    void removeInvitesForPlayer(UUID playerUuid);

    /**
     * Refresh scoreboard/team prefix for a player (e.g. after join or clan change).
     */
    void updatePlayerDisplay(Player player);

    /**
     * Refresh display for all members of a clan (e.g. after color change).
     */
    void refreshClanDisplay(UUID clanId);

    /** Returns the clan's current Wither kill count toward the Withering Council. */
    int getWitherKills(UUID clanId);

    /** Increments and returns the clan's Wither kill count. */
    int incrementWitherKills(UUID clanId);
}
