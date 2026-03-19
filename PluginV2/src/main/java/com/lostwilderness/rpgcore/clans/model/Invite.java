package com.lostwilderness.rpgcore.clans.model;

import java.util.UUID;

/**
 * Pending clan invite: clan, invited player, inviter, timestamp.
 */
public final class Invite {

    private final UUID clanId;
    private final UUID invitedUuid;
    private final UUID inviterUuid;
    private final long inviteTime;

    public Invite(UUID clanId, UUID invitedUuid, UUID inviterUuid, long inviteTime) {
        this.clanId = clanId;
        this.invitedUuid = invitedUuid;
        this.inviterUuid = inviterUuid;
        this.inviteTime = inviteTime;
    }

    public UUID getClanId() {
        return clanId;
    }

    public UUID getInvitedUuid() {
        return invitedUuid;
    }

    public UUID getInviterUuid() {
        return inviterUuid;
    }

    public long getInviteTime() {
        return inviteTime;
    }
}
