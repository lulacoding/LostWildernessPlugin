package com.lostwilderness.rpgcore.clans;

import com.lostwilderness.rpgcore.clans.model.Clan;
import com.lostwilderness.rpgcore.clans.model.Invite;
import com.lostwilderness.rpgcore.clans.repo.ClanRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Clan service implementation. Blocks on repository futures for sync API used
 * by commands.
 */
public final class ClanServiceImpl implements ClanService {

    private final ClanRepository repo;
    private final org.bukkit.plugin.Plugin plugin;

    public ClanServiceImpl(ClanRepository repo, org.bukkit.plugin.Plugin plugin) {
        this.repo = repo;
        this.plugin = plugin;
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Clan DB operation failed", e);
        }
    }

    @Override
    public Clan createClan(String name, String colorHex, UUID leaderUuid) {
        Clan clan = new Clan(UUID.randomUUID(), name, colorHex != null ? colorHex : "#FFFFFF");
        join(repo.saveClan(clan));
        join(repo.addMember(clan.getId(), leaderUuid, "Leader"));
        return clan;
    }

    @Override
    public Clan getClanById(UUID clanId) {
        return join(repo.getClanById(clanId));
    }

    @Override
    public Clan getClanByName(String name) {
        return join(repo.getClanByName(name));
    }

    @Override
    public UUID getClanOfPlayer(UUID playerUuid) {
        return join(repo.getClanOfPlayer(playerUuid));
    }

    @Override
    public Invite invitePlayerToClan(UUID clanId, UUID playerUuid, UUID inviterUuid) {
        Invite inv = new Invite(clanId, playerUuid, inviterUuid, System.currentTimeMillis());
        join(repo.createInvite(inv));
        return inv;
    }

    @Override
    public List<Invite> getPendingInvites(UUID playerUuid) {
        return join(repo.getPendingInvites(playerUuid));
    }

    @Override
    public void addMemberToClan(UUID clanId, UUID playerUuid, String rank) {
        join(repo.addMember(clanId, playerUuid, rank));
    }

    @Override
    public void removeMemberFromClan(UUID clanId, UUID playerUuid) {
        join(repo.removeMemberFromClan(clanId, playerUuid));
    }

    @Override
    public void updateClanColor(UUID clanId, String colorHex) {
        join(repo.updateClanColor(clanId, colorHex));
    }

    @Override
    public int getClanMemberCount(UUID clanId) {
        Integer n = join(repo.getClanMemberCount(clanId));
        return n != null ? n : 0;
    }

    @Override
    public String getMemberRank(UUID clanId, UUID playerUuid) {
        return join(repo.getMemberRank(clanId, playerUuid));
    }

    @Override
    public void addClanRelation(UUID clanId, UUID targetClanId, String relation) {
        join(repo.addClanRelation(clanId, targetClanId, relation));
    }

    @Override
    public void removeClanAndData(UUID clanId) {
        join(repo.removeClanAndData(clanId));
    }

    @Override
    public List<Clan> getAllClans() {
        return join(repo.getAllClans());
    }

    @Override
    public List<Map.Entry<UUID, String>> getClanMembersWithRanks(UUID clanId) {
        return join(repo.getClanMembersWithRanks(clanId));
    }

    @Override
    public void removeInvitesForPlayer(UUID playerUuid) {
        join(repo.removeInvitesForPlayer(playerUuid));
    }

    @Override
    public void updatePlayerDisplay(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager() != null
                ? Bukkit.getScoreboardManager().getMainScoreboard()
                : null;
        if (scoreboard == null)
            return;
        String name = player.getName();
        for (Team t : scoreboard.getTeams()) {
            if (t.hasEntry(name))
                t.removeEntry(name);
        }
        UUID clanId = getClanOfPlayer(player.getUniqueId());
        if (clanId == null)
            return;
        Clan clan = getClanById(clanId);
        if (clan == null)
            return;
        String teamId = "clan_" + clanId.toString().substring(0, 8).replace("-", "");
        if (teamId.length() > 16)
            teamId = teamId.substring(0, 16);
        Team team = scoreboard.getTeam(teamId);
        if (team == null)
            team = scoreboard.registerNewTeam(teamId);
        TextColor color = parseColor(clan.getColorHex());
        if (color == null)
            color = NamedTextColor.WHITE;
        try {
            team.prefix(Component.text("[" + clan.getName() + "] ").color(color));
            team.color(NamedTextColor.nearestTo(color));
        } catch (Exception e) {
            plugin.getLogger().warning("Clan display color: " + e.getMessage());
        }
        team.addEntry(name);
    }

    @Override
    public void refreshClanDisplay(UUID clanId) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (clanId.equals(getClanOfPlayer(p.getUniqueId()))) {
                updatePlayerDisplay(p);
            }
        }
    }

    @Override
    public int getWitherKills(UUID clanId) {
        Integer n = join(repo.getWitherKills(clanId));
        return n != null ? n : 0;
    }

    @Override
    public int incrementWitherKills(UUID clanId) {
        return join(repo.incrementWitherKills(clanId));
    }

    private static TextColor parseColor(String colorHex) {
        if (colorHex == null || !colorHex.matches("#[0-9A-Fa-f]{6}"))
            return null;
        try {
            int rgb = Integer.parseInt(colorHex.substring(1), 16);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            return TextColor.color(r, g, b);
        } catch (Exception e) {
            return NamedTextColor.WHITE;
        }
    }
}
