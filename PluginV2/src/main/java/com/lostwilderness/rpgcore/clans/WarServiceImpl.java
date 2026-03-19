package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.repo.ClanRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * War service: blocks on repo with timeout.
 */
public final class WarServiceImpl implements WarService {

    private static final long TIMEOUT_MS = 10_000;

    private final ClanRepository repo;

    public WarServiceImpl(ClanRepository repo) {
        this.repo = repo;
    }

    @Override
    public boolean areAtWar(UUID clanId1, UUID clanId2) {
        try {
            return Boolean.TRUE.equals(repo.areAtWar(clanId1, clanId2).get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void declareWar(UUID clanId1, UUID clanId2) {
        try {
            repo.addWar(clanId1, clanId2).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to declare war", e);
        }
    }

    @Override
    public void ceasefire(UUID clanId1, UUID clanId2) {
        try {
            repo.removeWar(clanId1, clanId2).get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to ceasefire", e);
        }
    }

    @Override
    public List<UUID[]> getActiveWars() {
        try {
            return repo.getActiveWars().get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return List.of();
        }
    }
}
