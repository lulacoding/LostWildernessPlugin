package com.lostwilderness.rpgcore.personality;

import com.lostwilderness.rpgcore.progression.ProgressionService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of TraitService.
 * Manages trait progression, Ultimate items, and Holy Enchants.
 */
public class TraitServiceImpl implements TraitService {

    private final TraitRepository repository;
    private final ProgressionService progressionService;
    private final CompletionService completionService;
    private final HolyEnchantService holyEnchantService;
    private final Plugin plugin;

    // In-memory cache of loaded profiles
    private final Map<UUID, PlayerTraitProfile> profileCache;

    public TraitServiceImpl(
        TraitRepository repository,
        ProgressionService progressionService,
        CompletionService completionService,
        HolyEnchantService holyEnchantService,
        Plugin plugin
    ) {
        this.repository = repository;
        this.progressionService = progressionService;
        this.completionService = completionService;
        this.holyEnchantService = holyEnchantService;
        this.plugin = plugin;
        this.profileCache = new ConcurrentHashMap<>();
    }

    // ========== Profile Management ==========

    @Override
    public CompletableFuture<Optional<PlayerTraitProfile>> getProfile(UUID uuid) {
        // Check cache first
        if (profileCache.containsKey(uuid)) {
            return CompletableFuture.completedFuture(Optional.of(profileCache.get(uuid)));
        }

        // Load from database and cache
        return repository.loadProfile(uuid).thenApply(optProfile -> {
            optProfile.ifPresent(profile -> profileCache.put(uuid, profile));
            return optProfile;
        });
    }

    @Override
    public CompletableFuture<Void> assignInitialTrait(UUID uuid, PersonalityTrait trait, Element element) {
        return repository.assignTrait(uuid, trait, element).thenRun(() -> {
            // Create new profile and cache it
            PlayerTraitProfile profile = PlayerTraitProfile.createNew(uuid, trait, element);
            profileCache.put(uuid, profile);
        });
    }

    @Override
    public CompletableFuture<Void> reloadProfile(UUID uuid) {
        profileCache.remove(uuid);
        return getProfile(uuid).thenApply(opt -> null);
    }

    // ========== Progression ==========

    @Override
    public void checkAndAdvanceTier(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) return;

        int completion = completionService.getCompletionPercent(uuid);
        TraitTier currentTier = profile.primaryTier();
        TraitTier targetTier = TraitTier.getTierForCompletion(completion);

        // Check if tier should advance
        if (targetTier.ordinal() > currentTier.ordinal()) {
            advanceTier(uuid, targetTier).thenRun(() -> {
                // Send advancement message
                Player player = Bukkit.getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
                    player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Trait Advancement!");
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "Your " + profile.primaryTrait().getColoredName()
                        + ChatColor.GRAY + " trait has advanced to:");
                    player.sendMessage(targetTier.getColoredName());
                    player.sendMessage("");
                    player.sendMessage(ChatColor.GRAY + "New Ability: "
                        + profile.primaryTrait().getPassiveDescription(targetTier, true)); // TODO: Honor alignment
                    player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

                    player.sendTitle(
                        ChatColor.GOLD + "Trait Advanced!",
                        targetTier.getColoredName(),
                        10, 60, 20
                    );
                }

                // Grant Ultimate item if reaching Ultimate tier
                if (targetTier == TraitTier.ULTIMATE) {
                    grantUltimateItem(uuid);
                }
            });
        }
    }

    @Override
    public CompletableFuture<Void> advanceTier(UUID uuid, TraitTier newTier) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(null);
        }

        PlayerTraitProfile updated = profile.withTier(newTier);
        profileCache.put(uuid, updated);

        return repository.saveProfile(updated);
    }

    @Override
    public int getCompletionPercent(UUID uuid) {
        return completionService.getCompletionPercent(uuid);
    }

    // ========== Elements ==========

    @Override
    public CompletableFuture<Void> revealElement(UUID playerUuid, UUID lordUuid) {
        PlayerTraitProfile profile = profileCache.get(playerUuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(null);
        }

        PlayerTraitProfile updated = profile.withElementRevealed();
        profileCache.put(playerUuid, updated);

        // Send reveal message
        Player player = Bukkit.getPlayer(playerUuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Element Revealed!");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "Your elemental affinity is: " + profile.element().getFullDisplay());
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + "Complete temple quests to unlock elemental passives.");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

            player.sendTitle(
                profile.element().getColoredName(),
                ChatColor.GRAY + "Your Element",
                10, 60, 20
            );
        }

        return repository.revealElement(playerUuid);
    }

    @Override
    public CompletableFuture<Void> completeTemple(UUID uuid, Element element) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(null);
        }

        PlayerTraitProfile updated = profile.withTempleComplete(element);
        profileCache.put(uuid, updated);

        // Send completion message
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Temple Complete!");
            player.sendMessage("");
            player.sendMessage(element.getFullDisplay() + ChatColor.GRAY + " temple conquered!");
            player.sendMessage("");

            // First temple activates player's element
            if (updated.getCompletedTempleCount() == 1) {
                player.sendMessage(ChatColor.GREEN + "Your element is now ACTIVE!");
                player.sendMessage(ChatColor.GRAY + "Passive: " + profile.element().getPassiveDescription());
            }

            // All temples complete = post-game
            if (updated.isPostGame()) {
                player.sendMessage("");
                player.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "⭐ POST-GAME UNLOCKED! ⭐");
                player.sendMessage(ChatColor.GRAY + "All 5 elements active + 300% completion!");
                player.sendMessage(ChatColor.GOLD + "+50% event rewards for life!");
            }

            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        return repository.completeTemple(uuid, element);
    }

    @Override
    public boolean isPostGame(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) return false;

        return profile.isPostGame() && getCompletionPercent(uuid) >= 300;
    }

    // ========== Ultimate Items ==========

    @Override
    public ItemStack createUltimateItem(PersonalityTrait trait, TraitTier tier) {
        if (tier != TraitTier.ULTIMATE) {
            return null; // Only create for Ultimate tier
        }

        ItemStack item = new ItemStack(trait.getUltimateItem());
        ItemMeta meta = item.getItemMeta();

        // Set display name
        meta.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + trait.getUltimateItemName());

        // Add max vanilla enchantments (unsafe to go beyond normal max)
        switch (trait) {
            case MAGE -> { // Crossbow
                meta.addEnchant(Enchantment.PIERCING, 10, true);
                meta.addEnchant(Enchantment.QUICK_CHARGE, 5, true);
                meta.addEnchant(Enchantment.MULTISHOT, 1, true);
            }
            case WARRIOR -> { // Diamond Sword
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.FIRE_ASPECT, 3, true);
                meta.addEnchant(Enchantment.LOOTING, 5, true);
                meta.addEnchant(Enchantment.SWEEPING_EDGE, 5, true);
            }
            case ARCHER -> { // Bow
                meta.addEnchant(Enchantment.POWER, 10, true);
                meta.addEnchant(Enchantment.PUNCH, 3, true);
                meta.addEnchant(Enchantment.FLAME, 1, true);
                meta.addEnchant(Enchantment.INFINITY, 1, true);
            }
            case BERSERKER -> { // Diamond Axe
                meta.addEnchant(Enchantment.SHARPNESS, 10, true);
                meta.addEnchant(Enchantment.EFFICIENCY, 7, true);
                meta.addEnchant(Enchantment.LOOTING, 5, true);
            }
            case RUNEKEEPER -> { // Diamond Sword
                meta.addEnchant(Enchantment.SHARPNESS, 8, true);
                meta.addEnchant(Enchantment.LOOTING, 5, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
            }
            case SCOUT -> { // Diamond Boots
                meta.addEnchant(Enchantment.PROTECTION, 6, true);
                meta.addEnchant(Enchantment.FEATHER_FALLING, 8, true);
                meta.addEnchant(Enchantment.DEPTH_STRIDER, 4, true);
                meta.addEnchant(Enchantment.SOUL_SPEED, 5, true);
            }
            default -> {
                // Generic enchantments for other Ultimate items
                meta.addEnchant(Enchantment.UNBREAKING, 5, true);
                meta.addEnchant(Enchantment.MENDING, 1, true);
            }
        }

        // Add lore
        List<String> lore = new ArrayList<>();
        lore.add(ChatColor.DARK_PURPLE + "" + ChatColor.ITALIC + "Ultimate Item");
        lore.add(ChatColor.GRAY + trait.getPassiveDescription(TraitTier.ULTIMATE, true));
        meta.setLore(lore);

        item.setItemMeta(meta);

        // Apply trait enchant via HolyEnchantService
        holyEnchantService.applyTraitEnchant(item, trait);

        return item;
    }

    @Override
    public CompletableFuture<Void> grantUltimateItem(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) {
            return CompletableFuture.completedFuture(null);
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) {
            return CompletableFuture.completedFuture(null);
        }

        ItemStack ultimateItem = createUltimateItem(profile.primaryTrait(), TraitTier.ULTIMATE);
        if (ultimateItem == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Add to player inventory
        player.getInventory().addItem(ultimateItem);

        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Ultimate Item Granted!");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + "You have received: " + ChatColor.GOLD + profile.primaryTrait().getUltimateItemName());
        player.sendMessage(ChatColor.DARK_GRAY + "This item can be blessed by a Lord for even greater power.");
        player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        return CompletableFuture.completedFuture(null);
    }

    // ========== Holy Enchants ==========

    @Override
    public CompletableFuture<Void> blessItem(UUID targetUuid, ItemStack item, UUID lordUuid) {
        if (item == null || !holyEnchantService.hasTraitEnchant(item)) {
            return CompletableFuture.completedFuture(null);
        }

        // Get random applicable Holy Enchant
        HolyEnchant randomEnchant = holyEnchantService.getRandomApplicableEnchant(item);
        if (randomEnchant == null) {
            return CompletableFuture.completedFuture(null);
        }

        // Bless the item
        holyEnchantService.blessItem(item, randomEnchant);

        // Save to database
        PlayerTraitProfile profile = profileCache.get(targetUuid);
        if (profile != null) {
            Set<HolyEnchant> newEnchants = Set.of(randomEnchant);
            PlayerTraitProfile updated = profile.withHolyEnchants(newEnchants);
            profileCache.put(targetUuid, updated);

            return repository.grantHolyEnchants(targetUuid, newEnchants, "LORD_BLESSING");
        }

        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletableFuture<Void> grantChristmasEnchants(UUID uuid, int count) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null || !isPostGame(uuid)) {
            return CompletableFuture.completedFuture(null);
        }

        // Generate random Holy Enchants (no duplicates)
        List<HolyEnchant> allEnchants = new ArrayList<>(Arrays.asList(HolyEnchant.values()));
        Collections.shuffle(allEnchants);

        Set<HolyEnchant> granted = new HashSet<>();
        for (int i = 0; i < Math.min(count, allEnchants.size()); i++) {
            granted.add(allEnchants.get(i));
        }

        PlayerTraitProfile updated = profile.withHolyEnchants(granted);
        profileCache.put(uuid, updated);

        // Send message to player
        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.sendMessage("");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            player.sendMessage(ChatColor.RED + "" + ChatColor.BOLD + "🎄 Christmas Miracle! 🎄");
            player.sendMessage("");
            player.sendMessage(ChatColor.GRAY + "As a post-game player, you've been granted:");
            player.sendMessage(ChatColor.GOLD + "" + granted.size() + " Holy Enchants!");
            player.sendMessage("");
            for (HolyEnchant enchant : granted) {
                player.sendMessage(ChatColor.GOLD + "  ⚡ " + enchant.getDisplayName());
            }
            player.sendMessage("");
            player.sendMessage(ChatColor.DARK_GRAY + "Use these to bless your Ultimate items!");
            player.sendMessage(ChatColor.GOLD + "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        }

        return repository.grantHolyEnchants(uuid, granted, "CHRISTMAS");
    }

    // ========== Utility ==========

    @Override
    public boolean hasActiveTrait(UUID uuid, PersonalityTrait trait, TraitTier minTier) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        if (profile == null) return false;

        return profile.primaryTrait() == trait && profile.primaryTier().ordinal() >= minTier.ordinal();
    }

    @Override
    public PersonalityTrait getCurrentTrait(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        return profile != null ? profile.primaryTrait() : null;
    }

    @Override
    public TraitTier getCurrentTier(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        return profile != null ? profile.primaryTier() : TraitTier.APPRENTICE;
    }

    @Override
    public boolean isElementRevealed(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        return profile != null && profile.elementRevealed();
    }

    @Override
    public boolean isElementActivated(UUID uuid) {
        PlayerTraitProfile profile = profileCache.get(uuid);
        return profile != null && profile.elementActivated();
    }
}
