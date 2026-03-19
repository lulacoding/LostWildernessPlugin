package com.lostwilderness.rpgcore.party.listener;

import com.lostwilderness.rpgcore.party.Party;
import com.lostwilderness.rpgcore.party.PartyService;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

/**
 * Handles party visual display:
 * - Scoreboard teams with [Party] prefix
 * - Party color coding
 * - Glowing effect
 */
public final class PartyDisplayListener implements Listener {

    private final PartyService service;
    private final YamlConfiguration config;

    public PartyDisplayListener(PartyService service, YamlConfiguration config) {
        this.service = service;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Update display for joining player
        updatePlayerDisplay(event.getPlayer());

        // Update display for all party members (in case they need to see the new member)
        Party party = service.getParty(event.getPlayer().getUniqueId());
        if (party != null) {
            for (UUID memberId : party.getMembers()) {
                if (memberId.equals(event.getPlayer().getUniqueId())) continue;
                Player member = Bukkit.getPlayer(memberId);
                if (member != null && member.isOnline()) {
                    updatePlayerDisplay(member);
                }
            }
        }
    }

    /**
     * Update visual display for a single player based on their party status.
     * Called by PartyService.updatePlayerDisplay().
     */
    public void updatePlayerDisplay(Player player) {
        if (!config.getBoolean("party.display.show-party-prefix", true)) {
            return; // Display disabled
        }

        Party party = service.getParty(player.getUniqueId());
        Scoreboard scoreboard = player.getScoreboard();

        // If player has no scoreboard, use main scoreboard
        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            player.setScoreboard(scoreboard);
        }

        String playerName = player.getName();

        // Remove player from any existing party teams
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("party_") && team.hasEntry(playerName)) {
                team.removeEntry(playerName);
            }
        }

        // If not in party, disable glow and return
        if (party == null) {
            if (config.getBoolean("party.display.enable-glow", true)) {
                player.setGlowing(false);
            }
            return;
        }

        // Create or get party team
        String teamId = "party_" + party.getId().toString().substring(0, 8);
        if (teamId.length() > 16) {
            teamId = teamId.substring(0, 16);
        }

        Team team = scoreboard.getTeam(teamId);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamId);
        }

        // Set party color and prefix
        TextColor color = parseColor(party.getColor());
        if (color == null) {
            color = NamedTextColor.GREEN;
        }

        try {
            if (config.getBoolean("party.display.show-party-prefix", true)) {
                team.prefix(Component.text("[Party] ").color(color));
            }
            team.color(NamedTextColor.nearestTo(color));
        } catch (Exception e) {
            // Fallback to no prefix on error
        }

        // Add player to team
        team.addEntry(playerName);

        // Enable glowing
        if (config.getBoolean("party.display.enable-glow", true)) {
            player.setGlowing(true);
        }

        // Update display for all other party members so they see this player's team
        for (UUID memberId : party.getMembers()) {
            if (memberId.equals(player.getUniqueId())) continue;
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                updateMemberView(member, party);
            }
        }
    }

    /**
     * Update a player's view of their party members (for cross-player visibility).
     */
    private void updateMemberView(Player viewer, Party party) {
        Scoreboard scoreboard = viewer.getScoreboard();
        if (scoreboard == null || scoreboard.equals(Bukkit.getScoreboardManager().getMainScoreboard())) {
            scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
            viewer.setScoreboard(scoreboard);
        }

        String teamId = "party_" + party.getId().toString().substring(0, 8);
        if (teamId.length() > 16) {
            teamId = teamId.substring(0, 16);
        }

        Team team = scoreboard.getTeam(teamId);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamId);
        }

        // Set team properties
        TextColor color = parseColor(party.getColor());
        if (color == null) {
            color = NamedTextColor.GREEN;
        }

        try {
            if (config.getBoolean("party.display.show-party-prefix", true)) {
                team.prefix(Component.text("[Party] ").color(color));
            }
            team.color(NamedTextColor.nearestTo(color));
        } catch (Exception e) {
            // Ignore
        }

        // Add all party members to the team
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                String memberName = member.getName();
                if (!team.hasEntry(memberName)) {
                    team.addEntry(memberName);
                }
            }
        }
    }

    /**
     * Parse hex color string to TextColor.
     */
    private TextColor parseColor(String colorHex) {
        if (colorHex == null || !colorHex.matches("#[0-9A-Fa-f]{6}")) {
            return null;
        }
        try {
            int rgb = Integer.parseInt(colorHex.substring(1), 16);
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            return TextColor.color(r, g, b);
        } catch (Exception e) {
            return null;
        }
    }
}
