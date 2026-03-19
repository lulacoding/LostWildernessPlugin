package com.lostwilderness.rpgcore.clans;

import java.util.List;
import java.util.UUID;

/**
 * Alliance between clans: invite, accept, leave, list allies.
 */
public interface AllianceService {

    void inviteToAlliance(UUID fromClanId, UUID targetClanId, UUID inviterPlayerUuid);

    List<UUID> getPendingAllianceInvitesFor(UUID targetClanId);

    void acceptAllianceInvite(UUID acceptingClanId, UUID invitingClanId);

    void leaveAlliance(UUID clanId, UUID formerAllyClanId);

    List<UUID> getAllies(UUID clanId);
}
