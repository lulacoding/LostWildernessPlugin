package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.repo.ClanRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Alliance service: blocks on repo with timeout. On accept, adds relation "alliance" both ways.
 */
public final class AllianceServiceImpl implements AllianceService {

    private static final long TIMEOUT_MS = 10_000;

    private final ClanRepository repo;

    public AllianceServiceImpl(ClanRepository repo) {
        this.repo = repo;
    }

    @Override
    public void inviteToAlliance(UUID fromClanId, UUID targetClanId, UUID inviterPlayerUuid) {
        try {
            repo.createAllianceInvite(fromClanId, targetClanId, inviterPlayerUuid).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send alliance invite", e);
        }
    }

    @Override
    public List<UUID> getPendingAllianceInvitesFor(UUID targetClanId) {
        try {
            return repo.getPendingAllianceInvitesTargeting(targetClanId).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return List.of();
        }
    }

    @Override
    public void acceptAllianceInvite(UUID acceptingClanId, UUID invitingClanId) {
        try {
            repo.removeAllianceInvite(invitingClanId, acceptingClanId).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            repo.addClanRelation(acceptingClanId, invitingClanId, "alliance").get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            repo.addClanRelation(invitingClanId, acceptingClanId, "alliance").get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to accept alliance", e);
        }
    }

    @Override
    public void leaveAlliance(UUID clanId, UUID formerAllyClanId) {
        try {
            repo.removeClanRelation(clanId, formerAllyClanId).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            repo.removeClanRelation(formerAllyClanId, clanId).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to leave alliance", e);
        }
    }

    @Override
    public List<UUID> getAllies(UUID clanId) {
        try {
            return repo.getAllies(clanId).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return List.of();
        }
    }
}
