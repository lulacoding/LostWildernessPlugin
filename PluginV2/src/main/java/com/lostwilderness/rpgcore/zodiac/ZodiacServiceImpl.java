package com.lostwilderness.rpgcore.zodiac;

import com.lostwilderness.rpgcore.calendar.CalendarServiceV2;
import com.lostwilderness.rpgcore.clans.ClanService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Implementation of ZodiacService.
 * Manages zodiac profile assignment, caching, and bonus calculations.
 */
final class ZodiacServiceImpl implements ZodiacService {

    private final ZodiacRepository repo;
    private final CalendarServiceV2 calendar;
    private final ClanService clans;
    private final Plugin plugin;

    private final Map<UUID, ZodiacProfile> cache = new ConcurrentHashMap<>();

    ZodiacServiceImpl(ZodiacRepository repo, CalendarServiceV2 calendar, ClanService clans, Plugin plugin) {
        this.repo = repo;
        this.calendar = calendar;
        this.clans = clans;
        this.plugin = plugin;
    }

    @Override
    public ZodiacProfile assignZodiac(UUID playerUuid, long epochDay) {
        LocalDate joinDate = calendar.getPlayerJoinDate(playerUuid);
        int month = joinDate.getMonthValue();
        int day = joinDate.getDayOfMonth();
        int year = joinDate.getYear();

        boolean isEpochian = (epochDay == 0);

        // Calculate month sign (with Ophiuchus handling)
        ZodiacSign monthSign = ZodiacSign.getSignForMCDate(month, day, isEpochian);

        // Calculate year sign
        ZodiacSign yearSign = ZodiacSign.getSignForYear(year);

        // Epochian second sign (hidden, random)
        ZodiacSign secondSign = null;
        if (isEpochian) {
            ZodiacSign[] allSigns = ZodiacSign.values();
            secondSign = allSigns[ThreadLocalRandom.current().nextInt(allSigns.length)];
        }

        // Random spirit animal (always hidden initially)
        SpiritAnimal[] allAnimals = SpiritAnimal.values();
        SpiritAnimal spiritAnimal = allAnimals[ThreadLocalRandom.current().nextInt(allAnimals.length)];

        ZodiacProfile profile = new ZodiacProfile(
            playerUuid,
            monthSign,
            yearSign,
            isEpochian,
            secondSign,
            false, // secondSignRevealed
            spiritAnimal,
            false, // spiritAnimalRevealed
            null   // personality
        );

        // Save to DB and cache
        repo.save(profile).join();
        cache.put(playerUuid, profile);

        plugin.getLogger().info("[Zodiac] Assigned zodiac for " + playerUuid +
            ": Month=" + monthSign.displayName() + ", Year=" + yearSign.displayName() +
            (isEpochian ? " (Epochian)" : ""));

        return profile;
    }

    @Override
    public Optional<ZodiacProfile> getZodiacProfile(UUID playerUuid) {
        // Check cache first
        ZodiacProfile cached = cache.get(playerUuid);
        if (cached != null) {
            return Optional.of(cached);
        }

        // Load from DB
        try {
            Optional<ZodiacProfile> profile = repo.findByUuid(playerUuid).join();
            profile.ifPresent(p -> cache.put(playerUuid, p));
            return profile;
        } catch (Exception e) {
            plugin.getLogger().warning("[Zodiac] Failed to load profile for " + playerUuid + ": " + e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void revealSecondSign(UUID targetUuid, UUID lordUuid) {
        Player lord = Bukkit.getPlayer(lordUuid);
        if (lord == null || !lord.hasPermission("lw.rank.lord")) {
            if (lord != null) {
                lord.sendMessage(ChatColor.RED + "You must be a Lord to reveal hidden zodiac signs.");
            }
            return;
        }

        Optional<ZodiacProfile> profileOpt = getZodiacProfile(targetUuid);
        if (profileOpt.isEmpty()) {
            lord.sendMessage(ChatColor.RED + "Player has no zodiac profile.");
            return;
        }

        ZodiacProfile profile = profileOpt.get();
        if (!profile.isEpochian()) {
            lord.sendMessage(ChatColor.RED + "This player is not an Epochian and has no hidden second sign.");
            return;
        }

        if (profile.secondSignRevealed()) {
            lord.sendMessage(ChatColor.YELLOW + "This player's second sign has already been revealed.");
            return;
        }

        // Update DB
        repo.updateSecondSignRevealed(targetUuid, true).join();

        // Update cache
        ZodiacProfile updated = new ZodiacProfile(
            profile.playerUuid(),
            profile.monthSign(),
            profile.yearSign(),
            profile.isEpochian(),
            profile.secondSign(),
            true, // secondSignRevealed
            profile.spiritAnimal(),
            profile.spiritAnimalRevealed(),
            profile.personality()
        );
        cache.put(targetUuid, updated);

        // Notify both players
        lord.sendMessage(ChatColor.GOLD + "You have revealed the hidden second sign for " +
            Bukkit.getOfflinePlayer(targetUuid).getName() + "!");

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage(ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.GOLD +
                "A Lord has revealed your hidden second sign: " + ChatColor.YELLOW +
                profile.secondSign().displayName() + " " + profile.secondSign().symbol() + ChatColor.LIGHT_PURPLE + " ✦");
        }
    }

    @Override
    public void revealSpiritAnimal(UUID targetUuid, UUID lordUuid) {
        Player lord = Bukkit.getPlayer(lordUuid);
        if (lord == null || !lord.hasPermission("lw.rank.lord")) {
            if (lord != null) {
                lord.sendMessage(ChatColor.RED + "You must be a Lord to reveal spirit animals.");
            }
            return;
        }

        Optional<ZodiacProfile> profileOpt = getZodiacProfile(targetUuid);
        if (profileOpt.isEmpty()) {
            lord.sendMessage(ChatColor.RED + "Player has no zodiac profile.");
            return;
        }

        ZodiacProfile profile = profileOpt.get();
        if (profile.spiritAnimalRevealed()) {
            lord.sendMessage(ChatColor.YELLOW + "This player's spirit animal has already been revealed.");
            return;
        }

        // Update DB
        repo.updateSpiritAnimalRevealed(targetUuid, true).join();

        // Update cache
        ZodiacProfile updated = new ZodiacProfile(
            profile.playerUuid(),
            profile.monthSign(),
            profile.yearSign(),
            profile.isEpochian(),
            profile.secondSign(),
            profile.secondSignRevealed(),
            profile.spiritAnimal(),
            true, // spiritAnimalRevealed
            profile.personality()
        );
        cache.put(targetUuid, updated);

        // Notify both players
        lord.sendMessage(ChatColor.GOLD + "You have revealed the spirit animal for " +
            Bukkit.getOfflinePlayer(targetUuid).getName() + ": " + ChatColor.YELLOW +
            profile.spiritAnimal().displayName());

        Player target = Bukkit.getPlayer(targetUuid);
        if (target != null) {
            target.sendMessage(ChatColor.LIGHT_PURPLE + "✦ " + ChatColor.GOLD +
                "A Lord has revealed your spirit animal: " + ChatColor.YELLOW +
                profile.spiritAnimal().displayName() + ChatColor.LIGHT_PURPLE + " ✦");
            target.sendMessage(ChatColor.GRAY + "  " + profile.spiritAnimal().description());
        }
    }

    @Override
    public ZodiacSign getCurrentMonthSign() {
        LocalDate date = calendar.getCurrentSnapshot().date();
        return ZodiacSign.getSignForMCDate(date.getMonthValue(), date.getDayOfMonth());
    }

    @Override
    public ZodiacSign getCurrentYearSign() {
        int year = calendar.getCurrentSnapshot().date().getYear();
        return ZodiacSign.getSignForYear(year);
    }

    @Override
    public boolean isSignMonthActive(UUID playerUuid) {
        Optional<ZodiacProfile> profile = getZodiacProfile(playerUuid);
        if (profile.isEmpty()) return false;

        ZodiacSign currentMonth = getCurrentMonthSign();
        return profile.get().monthSign() == currentMonth;
    }

    @Override
    public boolean isSignYearActive(UUID playerUuid) {
        Optional<ZodiacProfile> profile = getZodiacProfile(playerUuid);
        if (profile.isEmpty()) return false;

        ZodiacSign currentYear = getCurrentYearSign();
        return profile.get().yearSign() == currentYear;
    }

    @Override
    public boolean isSyncActive(UUID playerUuid) {
        return isSignMonthActive(playerUuid) && isSignYearActive(playerUuid);
    }

    @Override
    public boolean isClanLeaderYearBonusActive(UUID clanLeaderUuid) {
        if (clans == null) return false;

        UUID clanId = clans.getClanOfPlayer(clanLeaderUuid);
        if (clanId == null) return false;

        // Find clan leader
        List<Map.Entry<UUID, String>> members = clans.getClanMembersWithRanks(clanId);
        UUID leaderUuid = members.stream()
            .filter(e -> "Leader".equals(e.getValue()))
            .map(Map.Entry::getKey)
            .findFirst()
            .orElse(null);

        if (leaderUuid == null || !leaderUuid.equals(clanLeaderUuid)) {
            return false;
        }

        // Check if leader's year sign matches current year
        Optional<ZodiacProfile> profile = getZodiacProfile(leaderUuid);
        if (profile.isEmpty()) return false;

        ZodiacSign currentYear = getCurrentYearSign();
        return profile.get().yearSign() == currentYear;
    }

    @Override
    public void invalidateCache(UUID playerUuid) {
        cache.remove(playerUuid);
    }
}
