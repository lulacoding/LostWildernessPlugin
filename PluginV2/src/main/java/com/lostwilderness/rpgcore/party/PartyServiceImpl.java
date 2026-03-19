package com.lostwilderness.rpgcore.party;

import com.lostwilderness.rpgcore.infra.messaging.ClusterMessagingService;
import com.lostwilderness.rpgcore.infra.messaging.StubClusterMessagingService;
import com.lostwilderness.rpgcore.infra.scheduler.SchedulerService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Party service implementation with in-memory cache and DB persistence.
 * Blocks on async repository operations for sync command API.
 */
public final class PartyServiceImpl implements PartyService {

    private final PartyRepository repo;
    private final ClusterMessagingService messaging;
    private final SchedulerService scheduler;
    private final org.bukkit.plugin.Plugin plugin;
    private final int maxSize;
    private final String defaultColor;

    // In-memory cache: partyId -> Party
    private final Map<UUID, Party> cache = new ConcurrentHashMap<>();
    // Reverse index: playerUuid -> partyId
    private final Map<UUID, UUID> playerPartyIndex = new ConcurrentHashMap<>();

    // Display listener (set by PartyModule after creation)
    private Object displayListener;

    public PartyServiceImpl(PartyRepository repo, ClusterMessagingService messaging,
                            SchedulerService scheduler, org.bukkit.plugin.Plugin plugin,
                            int maxSize, String defaultColor) {
        this.repo = repo;
        this.messaging = messaging;
        this.scheduler = scheduler;
        this.plugin = plugin;
        this.maxSize = maxSize;
        this.defaultColor = defaultColor;
    }

    /**
     * Set the display listener (called by PartyModule).
     * We use Object to avoid circular dependency.
     */
    public void setDisplayListener(Object listener) {
        this.displayListener = listener;
    }

    private static <T> T join(CompletableFuture<T> future) {
        try {
            return future.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Party DB operation failed", e);
        }
    }

    // --- Core operations ---

    @Override
    public Party createParty(UUID leaderUuid, String name) {
        // Check if already in a party
        if (isInParty(leaderUuid)) {
            throw new IllegalStateException("Already in a party");
        }

        Party party = new Party(UUID.randomUUID(), leaderUuid,
            name != null ? name : ("Party_" + leaderUuid.toString().substring(0, 8)),
            maxSize, System.currentTimeMillis(), defaultColor);

        join(repo.createParty(party));
        cache.put(party.getId(), party);
        playerPartyIndex.put(leaderUuid, party.getId());

        publishPartySync(party);
        plugin.getLogger().info("[party] Created: " + party);
        return party;
    }

    @Override
    public void disbandParty(UUID leaderUuid) {
        Party party = getPartyByLeader(leaderUuid);
        if (party == null) {
            throw new IllegalStateException("Not in a party");
        }

        join(repo.deleteParty(party.getId()));
        cache.remove(party.getId());
        for (UUID memberId : party.getMembers()) {
            playerPartyIndex.remove(memberId);
        }

        publishPartyDisband(party.getId());
        plugin.getLogger().info("[party] Disbanded: " + party.getId());
    }

    @Override
    public void invitePlayer(UUID leaderUuid, UUID targetUuid) {
        Party party = getPartyByLeader(leaderUuid);

        // Auto-create party if leader doesn't have one
        if (party == null) {
            party = createParty(leaderUuid, null);
        }

        if (party.isFull()) {
            throw new IllegalStateException("Party is full");
        }

        if (party.isMember(targetUuid)) {
            throw new IllegalStateException("Player is already in the party");
        }

        if (party.isInvited(targetUuid)) {
            throw new IllegalStateException("Player already has a pending invite");
        }

        party.addInvite(targetUuid);
        join(repo.addInvite(party.getId(), targetUuid));
        publishPartySync(party);

        // Send message to target player if online
        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null && target.isOnline()) {
            Player leader = Bukkit.getPlayer(leaderUuid);
            String leaderName = leader != null ? leader.getName() : leaderUuid.toString();
            target.sendMessage("§a" + leaderName + " §7invited you to their party! §e/party accept " + leaderName);
        }

        plugin.getLogger().info("[party] Invited " + targetUuid + " to " + party.getId());
    }

    @Override
    public void acceptInvite(UUID playerUuid, UUID partyId) {
        Party party = getPartyById(partyId);
        if (party == null) {
            throw new IllegalStateException("Party no longer exists");
        }

        if (!party.isInvited(playerUuid)) {
            throw new IllegalStateException("No invite from this party");
        }

        if (party.isFull()) {
            throw new IllegalStateException("Party is full");
        }

        // Leave current party if in one
        if (isInParty(playerUuid)) {
            leaveParty(playerUuid);
        }

        party.addMember(playerUuid);
        party.removeInvite(playerUuid);
        join(repo.addMember(partyId, playerUuid));
        join(repo.removeInvite(partyId, playerUuid));

        playerPartyIndex.put(playerUuid, partyId);
        publishPartySync(party);

        plugin.getLogger().info("[party] Player " + playerUuid + " joined " + partyId);
    }

    @Override
    public void leaveParty(UUID playerUuid) {
        UUID partyId = playerPartyIndex.get(playerUuid);
        if (partyId == null) return;

        Party party = cache.get(partyId);
        if (party == null) return;

        if (party.isLeader(playerUuid)) {
            // Leader leaving = disband
            disbandParty(playerUuid);
        } else {
            party.removeMember(playerUuid);
            join(repo.removeMember(partyId, playerUuid));
            playerPartyIndex.remove(playerUuid);
            publishPartySync(party);
            plugin.getLogger().info("[party] Player " + playerUuid + " left " + partyId);
        }
    }

    @Override
    public void kickMember(UUID leaderUuid, UUID targetUuid) {
        Party party = getPartyByLeader(leaderUuid);
        if (party == null) {
            throw new IllegalStateException("Not in a party");
        }

        if (!party.isMember(targetUuid)) {
            throw new IllegalStateException("Player is not in the party");
        }

        if (party.isLeader(targetUuid)) {
            throw new IllegalStateException("Cannot kick the party leader");
        }

        party.removeMember(targetUuid);
        join(repo.removeMember(party.getId(), targetUuid));
        playerPartyIndex.remove(targetUuid);
        publishPartySync(party);

        plugin.getLogger().info("[party] Player " + targetUuid + " kicked from " + party.getId());
    }

    // --- Queries ---

    @Override
    public Party getParty(UUID playerUuid) {
        UUID partyId = playerPartyIndex.get(playerUuid);
        return partyId != null ? cache.get(partyId) : null;
    }

    @Override
    public Party getPartyById(UUID partyId) {
        return cache.get(partyId);
    }

    @Override
    public Set<UUID> getPartyMembers(UUID playerUuid) {
        Party party = getParty(playerUuid);
        return party != null ? party.getMembers() : Collections.emptySet();
    }

    @Override
    public boolean isInParty(UUID playerUuid) {
        return playerPartyIndex.containsKey(playerUuid);
    }

    @Override
    public boolean areInSameParty(UUID player1, UUID player2) {
        UUID party1 = playerPartyIndex.get(player1);
        UUID party2 = playerPartyIndex.get(player2);
        return party1 != null && party1.equals(party2);
    }

    @Override
    public String getPartyColor(UUID partyId) {
        Party party = cache.get(partyId);
        return party != null ? party.getColor() : defaultColor;
    }

    @Override
    public Set<UUID> getPlayerInvites(UUID playerUuid) {
        return join(repo.getPlayerInvites(playerUuid));
    }

    // --- Display ---

    @Override
    public void updatePartyDisplay(UUID partyId) {
        if (displayListener == null) return;

        Party party = cache.get(partyId);
        if (party == null) return;

        // Update display for all party members
        for (UUID memberId : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberId);
            if (member != null && member.isOnline()) {
                updatePlayerDisplay(member);
            }
        }
    }

    @Override
    public void updatePlayerDisplay(Player player) {
        if (displayListener == null) return;

        try {
            // Call PartyDisplayListener.updatePlayerDisplay via reflection
            java.lang.reflect.Method method = displayListener.getClass()
                .getMethod("updatePlayerDisplay", Player.class);
            method.invoke(displayListener, player);
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to update player display: " + e.getMessage());
        }
    }

    @Override
    public void cacheParty(Party party) {
        cache.put(party.getId(), party);
        for (UUID memberId : party.getMembers()) {
            playerPartyIndex.put(memberId, party.getId());
        }
    }

    // --- Cluster messaging ---

    private void publishPartySync(Party party) {
        try {
            String json = serializeParty(party);
            messaging.publish("party/sync", json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to publish party sync: " + e.getMessage());
        }
    }

    private void publishPartyDisband(UUID partyId) {
        try {
            messaging.publish("party/disband", partyId.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to publish party disband: " + e.getMessage());
        }
    }

    public void handlePartySync(byte[] data) {
        try {
            String json = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            Party party = deserializeParty(json);
            if (party != null) {
                cacheParty(party);
                plugin.getLogger().fine("[party] Synced party from cluster: " + party.getId());
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to handle party sync: " + e.getMessage());
        }
    }

    public void handlePartyDisband(byte[] data) {
        try {
            String partyIdStr = new String(data, java.nio.charset.StandardCharsets.UTF_8);
            UUID partyId = UUID.fromString(partyIdStr);
            Party party = cache.remove(partyId);
            if (party != null) {
                for (UUID memberId : party.getMembers()) {
                    playerPartyIndex.remove(memberId);
                }
                plugin.getLogger().fine("[party] Disbanded party from cluster: " + partyId);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to handle party disband: " + e.getMessage());
        }
    }

    // --- JSON Serialization ---

    private String serializeParty(Party party) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"id\":\"").append(party.getId()).append("\",");
        json.append("\"leader\":\"").append(party.getLeader()).append("\",");
        json.append("\"name\":\"").append(escapeJson(party.getName())).append("\",");
        json.append("\"color\":\"").append(party.getColor()).append("\",");
        json.append("\"maxSize\":").append(party.getMaxSize()).append(",");
        json.append("\"createdAt\":").append(party.getCreatedAt()).append(",");

        // Members
        json.append("\"members\":[");
        boolean first = true;
        for (UUID memberId : party.getMembers()) {
            if (!first) json.append(",");
            json.append("\"").append(memberId).append("\"");
            first = false;
        }
        json.append("],");

        // Invites
        json.append("\"invites\":[");
        first = true;
        for (UUID inviteId : party.getInvites()) {
            if (!first) json.append(",");
            json.append("\"").append(inviteId).append("\"");
            first = false;
        }
        json.append("]");

        json.append("}");
        return json.toString();
    }

    private Party deserializeParty(String json) {
        try {
            // Simple JSON parsing without external libraries
            String id = extractJsonString(json, "id");
            String leader = extractJsonString(json, "leader");
            String name = extractJsonString(json, "name");
            String color = extractJsonString(json, "color");
            int maxSize = extractJsonInt(json, "maxSize");
            long createdAt = extractJsonLong(json, "createdAt");

            Party party = new Party(UUID.fromString(id), UUID.fromString(leader),
                                   name, maxSize, createdAt, color);

            // Parse members
            String membersJson = extractJsonArray(json, "members");
            if (membersJson != null) {
                for (String memberIdStr : parseJsonArray(membersJson)) {
                    party.addMember(UUID.fromString(memberIdStr));
                }
            }

            // Parse invites
            String invitesJson = extractJsonArray(json, "invites");
            if (invitesJson != null) {
                for (String inviteIdStr : parseJsonArray(invitesJson)) {
                    party.addInvite(UUID.fromString(inviteIdStr));
                }
            }

            return party;
        } catch (Exception e) {
            plugin.getLogger().warning("[party] Failed to deserialize party: " + e.getMessage());
            return null;
        }
    }

    private String escapeJson(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }

    private String extractJsonString(String json, String key) {
        String pattern = "\"" + key + "\":\"";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length();
        int end = json.indexOf("\"", start);
        return json.substring(start, end);
    }

    private int extractJsonInt(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return 0;
        start += pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Integer.parseInt(json.substring(start, end).trim());
    }

    private long extractJsonLong(String json, String key) {
        String pattern = "\"" + key + "\":";
        int start = json.indexOf(pattern);
        if (start == -1) return 0;
        start += pattern.length();
        int end = json.indexOf(",", start);
        if (end == -1) end = json.indexOf("}", start);
        return Long.parseLong(json.substring(start, end).trim());
    }

    private String extractJsonArray(String json, String key) {
        String pattern = "\"" + key + "\":[";
        int start = json.indexOf(pattern);
        if (start == -1) return null;
        start += pattern.length() - 1; // Include the [
        int bracketCount = 0;
        int end = start;
        for (int i = start; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') bracketCount++;
            else if (c == ']') {
                bracketCount--;
                if (bracketCount == 0) {
                    end = i + 1;
                    break;
                }
            }
        }
        return json.substring(start, end);
    }

    private java.util.List<String> parseJsonArray(String arrayJson) {
        java.util.List<String> result = new java.util.ArrayList<>();
        if (arrayJson == null || arrayJson.equals("[]")) return result;

        String content = arrayJson.substring(1, arrayJson.length() - 1); // Remove [ ]
        String[] items = content.split(",");
        for (String item : items) {
            String trimmed = item.trim();
            if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
                result.add(trimmed.substring(1, trimmed.length() - 1));
            }
        }
        return result;
    }

    // --- Database Polling Fallback ---

    /**
     * Start database polling fallback for when ClusterMessagingService is stubbed.
     * Polls every 5 seconds to sync party state from DB.
     */
    public void startDatabasePolling() {
        if (!(messaging instanceof StubClusterMessagingService)) {
            // Real cluster messaging available, no need to poll
            return;
        }

        plugin.getLogger().info("[party] Starting database polling fallback (5s interval)");

        // Poll every 5 seconds (100 ticks)
        scheduler.runAsyncRepeating(() -> {
            try {
                // Get all online players
                Set<UUID> onlinePlayers = new HashSet<>();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    onlinePlayers.add(player.getUniqueId());
                }

                // For each online player, check if their party has changed
                for (UUID playerUuid : onlinePlayers) {
                    repo.getPlayerParty(playerUuid).thenCompose(partyId -> {
                        if (partyId == null) {
                            // Player not in a party - remove from cache if present
                            UUID cachedPartyId = playerPartyIndex.get(playerUuid);
                            if (cachedPartyId != null) {
                                playerPartyIndex.remove(playerUuid);
                                Party cachedParty = cache.get(cachedPartyId);
                                if (cachedParty != null) {
                                    cachedParty.removeMember(playerUuid);
                                }
                            }
                            return CompletableFuture.completedFuture(null);
                        }

                        // Check if party needs to be loaded/refreshed
                        return repo.getPartyById(partyId);
                    }).thenAccept(party -> {
                        if (party != null) {
                            Party cached = cache.get(party.getId());
                            if (cached == null || !partiesEqual(cached, party)) {
                                // Party changed - update cache
                                scheduler.runSync(() -> {
                                    cacheParty(party);
                                    Player player = Bukkit.getPlayer(playerUuid);
                                    if (player != null && player.isOnline()) {
                                        updatePlayerDisplay(player);
                                    }
                                });
                            }
                        }
                    }).exceptionally(ex -> {
                        plugin.getLogger().warning("[party] Polling error for " + playerUuid + ": " + ex.getMessage());
                        return null;
                    });
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[party] Polling error: " + e.getMessage());
            }
        }, 100L, 100L); // Start after 5s, repeat every 5s
    }

    /**
     * Check if two parties are equal (for change detection).
     */
    private boolean partiesEqual(Party p1, Party p2) {
        if (!p1.getId().equals(p2.getId())) return false;
        if (!p1.getLeader().equals(p2.getLeader())) return false;
        if (!Objects.equals(p1.getName(), p2.getName())) return false;
        if (!Objects.equals(p1.getColor(), p2.getColor())) return false;
        if (!p1.getMembers().equals(p2.getMembers())) return false;
        if (!p1.getInvites().equals(p2.getInvites())) return false;
        return true;
    }

    // --- Helper methods ---

    private Party getPartyByLeader(UUID leaderUuid) {
        Party party = getParty(leaderUuid);
        if (party != null && party.isLeader(leaderUuid)) {
            return party;
        }
        return null;
    }
}
