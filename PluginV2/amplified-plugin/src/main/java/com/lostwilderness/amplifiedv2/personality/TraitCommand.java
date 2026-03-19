package com.lostwilderness.amplifiedv2.personality;

import com.lostwilderness.rpgcore.personality.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Command executor for /trait command with 6 subcommands:
 * - /trait info [player] - Display trait, tier, element status, completion %
 * - /trait revealelement <player> - Lord only: Reveal element to player
 * - /trait bless <player> - Lord only: Bless held Ultimate item
 * - /trait temple <element> - Admin: Mark temple complete
 * - /trait set <trait> - Admin: Force trait assignment
 * - /trait quiz - Admin: Restart quiz (clears completion flag)
 */
public class TraitCommand implements CommandExecutor {

    private final Plugin plugin;
    private final TraitService traitService;
    private final CompletionService completionService;
    private final HolyEnchantService holyEnchantService;
    private final Map<UUID, Long> decoyCooldowns = new HashMap<>();

    public TraitCommand(Plugin plugin, TraitService traitService, CompletionService completionService, HolyEnchantService holyEnchantService) {
        this.plugin = plugin;
        this.traitService = traitService;
        this.completionService = completionService;
        this.holyEnchantService = holyEnchantService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "info":
                return handleInfo(sender, args);
            case "revealelement":
                return handleRevealElement(sender, args);
            case "bless":
                return handleBless(sender, args);
            case "temple":
                return handleTemple(sender, args);
            case "set":
                return handleSet(sender, args);
            case "quiz":
                return handleQuiz(sender, args);
            case "decoy":
                return handleDecoy(sender, args);
            default:
                sender.sendMessage("§cUnknown subcommand: " + subcommand);
                sendHelp(sender);
                return true;
        }
    }

    /**
     * /trait info [player] - Display trait, tier, element status, completion %
     */
    private boolean handleInfo(CommandSender sender, String[] args) {
        Player target;

        if (args.length >= 2) {
            // Check other player (requires permission)
            if (!sender.hasPermission("lw.trait.info.others")) {
                sender.sendMessage("§cYou don't have permission to view other players' traits.");
                return true;
            }
            target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found: " + args[1]);
                return true;
            }
        } else {
            // Check own traits
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cConsole must specify a player: /trait info <player>");
                return true;
            }
            target = (Player) sender;
        }

        UUID uuid = target.getUniqueId();

        // Fetch profile asynchronously
        traitService.getProfile(uuid).thenAccept(profileOpt -> {
            if (profileOpt.isEmpty()) {
                sender.sendMessage("§e" + target.getName() + " §chas not completed the personality quiz yet.");
                return;
            }

            PlayerTraitProfile profile = profileOpt.get();
            int completion = completionService.getCompletionPercent(uuid);

            // Format output
            sender.sendMessage("§8§m--------------------§r §6Trait Info §8§m--------------------");
            sender.sendMessage("§ePlayer: §f" + target.getName());
            sender.sendMessage("§ePrimary Trait: " + formatTrait(profile.primaryTrait(), profile.primaryTier()));
            sender.sendMessage("§eElement: " + formatElement(profile));
            sender.sendMessage("§eCompletion: " + formatCompletion(completion));
            sender.sendMessage("§eTemple Progress: " + formatTemples(profile));
            if (profile.isPostGame()) {
                sender.sendMessage("§6⭐ Post-Game God §7(All 5 elements active)");
            }
            sender.sendMessage("§8§m------------------------------------------------");
        }).exceptionally(ex -> {
            sender.sendMessage("§cError loading trait info: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    /**
     * /trait revealelement <player> - Lord only: Reveal element to player
     */
    private boolean handleRevealElement(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lw.rank.lord")) {
            sender.sendMessage("§cOnly Lords can reveal elements to players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /trait revealelement <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        UUID lordUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        if (lordUuid == null) {
            sender.sendMessage("§cConsole cannot reveal elements (must be performed by a Lord).");
            return true;
        }

        traitService.revealElement(target.getUniqueId(), lordUuid).thenRun(() -> {
            sender.sendMessage("§aRevealed element to §e" + target.getName() + "§a!");
            target.sendMessage("§6§l⚡ A Lord has revealed your elemental affinity!");
            target.sendMessage("§7Check §e/trait info §7to see your element.");
        }).exceptionally(ex -> {
            sender.sendMessage("§cError revealing element: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    /**
     * /trait bless <player> - Lord only: Bless held Ultimate item
     */
    private boolean handleBless(CommandSender sender, String[] args) {
        if (!sender.hasPermission("lw.rank.lord")) {
            sender.sendMessage("§cOnly Lords can bless Ultimate items.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /trait bless <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found: " + args[1]);
            return true;
        }

        ItemStack heldItem = target.getInventory().getItemInMainHand();
        if (heldItem == null || heldItem.getType().isAir()) {
            sender.sendMessage("§c" + target.getName() + " is not holding an item.");
            return true;
        }

        // Check if item has a Trait Enchant (is an Ultimate item)
        String traitEnchant = holyEnchantService.getTraitEnchant(heldItem);
        if (traitEnchant == null) {
            sender.sendMessage("§c" + target.getName() + " is not holding an Ultimate item.");
            return true;
        }

        UUID lordUuid = sender instanceof Player ? ((Player) sender).getUniqueId() : null;
        if (lordUuid == null) {
            sender.sendMessage("§cConsole cannot bless items (must be performed by a Lord).");
            return true;
        }

        traitService.blessItem(target.getUniqueId(), heldItem, lordUuid).thenRun(() -> {
            sender.sendMessage("§aBlessed §e" + target.getName() + "§a's Ultimate item!");
            target.sendMessage("§6§l⚡ A Lord has blessed your Ultimate item!");
            target.sendMessage("§7Enchantments doubled and Holy Enchant added!");
        }).exceptionally(ex -> {
            sender.sendMessage("§cError blessing item: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    /**
     * /trait temple <element> - Admin: Mark temple complete
     */
    private boolean handleTemple(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cOnly operators can mark temple completions.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cConsole cannot complete temples (must be performed by a player).");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /trait temple <element>");
            sender.sendMessage("§7Elements: FIRE, EARTH, WIND, WATER, AETHER");
            return true;
        }

        Player player = (Player) sender;
        Element element;
        try {
            element = Element.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid element: " + args[1]);
            sender.sendMessage("§7Valid elements: FIRE, EARTH, WIND, WATER, AETHER");
            return true;
        }

        traitService.completeTemple(player.getUniqueId(), element).thenRun(() -> {
            sender.sendMessage("§aMarked §e" + element.name() + " §atemple as complete!");
            if (traitService.isPostGame(player.getUniqueId())) {
                sender.sendMessage("§6⭐ You have achieved Post-Game God status! All 5 elements active.");
            }
        }).exceptionally(ex -> {
            sender.sendMessage("§cError marking temple complete: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    /**
     * /trait set <trait> - Admin: Force trait assignment
     */
    private boolean handleSet(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cOnly operators can force trait assignments.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cConsole cannot set traits (must be performed by a player).");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /trait set <trait>");
            sender.sendMessage("§7Traits: MAGE, WARRIOR, ARCHER, HEALER, RANGER, ALCHEMIST, SMITH, SCOUT, BERSERKER, SAGE, TAMER, RUNEKEEPER, ILLUSIONIST");
            return true;
        }

        Player player = (Player) sender;
        PersonalityTrait trait;
        try {
            trait = PersonalityTrait.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cInvalid trait: " + args[1]);
            sender.sendMessage("§7Valid traits: MAGE, WARRIOR, ARCHER, HEALER, RANGER, ALCHEMIST, SMITH, SCOUT, BERSERKER, SAGE, TAMER, RUNEKEEPER, ILLUSIONIST");
            return true;
        }

        // Assign random element for now
        Element randomElement = Element.values()[(int) (Math.random() * Element.values().length)];

        traitService.assignInitialTrait(player.getUniqueId(), trait, randomElement).thenRun(() -> {
            sender.sendMessage("§aForce-assigned trait: " + formatTrait(trait, TraitTier.APPRENTICE));
            sender.sendMessage("§7Element: " + formatElement(randomElement, false, false));
        }).exceptionally(ex -> {
            sender.sendMessage("§cError setting trait: " + ex.getMessage());
            ex.printStackTrace();
            return null;
        });

        return true;
    }

    /**
     * /trait quiz - Admin: Restart quiz (clears completion flag)
     */
    private boolean handleQuiz(CommandSender sender, String[] args) {
        if (!sender.isOp()) {
            sender.sendMessage("§cOnly operators can reset quiz completion.");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cConsole cannot reset quiz (must be performed by a player).");
            return true;
        }

        Player player = (Player) sender;
        sender.sendMessage("§cQuiz reset not yet implemented (requires lobby plugin integration).");
        sender.sendMessage("§7Use §e/trait set <trait> §7to force-assign a trait instead.");

        return true;
    }

    /**
     * Send help message
     */
    /**
     * /trait decoy - Spawn harmless decoy (Illusionist Trait only)
     */
    private boolean handleDecoy(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use decoys.");
            return true;
        }

        Player player = (Player) sender;

        // Check trait
        try {
            Optional<PlayerTraitProfile> profile = traitService.getProfile(player.getUniqueId()).join();
            if (profile.isEmpty() || profile.get().primaryTrait() != PersonalityTrait.ILLUSIONIST) {
                player.sendMessage("§cYou must be an ILLUSIONIST to use decoys.");
                return true;
            }

            if (profile.get().primaryTier().ordinal() < TraitTier.TRAIT.ordinal()) {
                player.sendMessage("§cYou must reach ILLUSIONIST Trait tier to use decoys.");
                return true;
            }
        } catch (Exception e) {
            player.sendMessage("§cError checking trait status.");
            return true;
        }

        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();

        // Check cooldown (2 minutes = 120,000ms)
        if (decoyCooldowns.containsKey(uuid)) {
            long lastUse = decoyCooldowns.get(uuid);
            long remaining = 120000 - (now - lastUse);
            if (remaining > 0) {
                player.sendMessage("§cDecoy on cooldown! " + (remaining / 1000) + " seconds remaining.");
                return true;
            }
        }

        // Spawn decoy
        spawnDecoy(player);
        decoyCooldowns.put(uuid, now);
        player.sendMessage("§d✓ Decoy spawned!");

        return true;
    }

    private void spawnDecoy(Player player) {
        Location loc = player.getLocation().clone();
        ArmorStand decoy = (ArmorStand) loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);

        // Copy player appearance
        decoy.setHelmet(player.getInventory().getHelmet());
        decoy.setChestplate(player.getInventory().getChestplate());
        decoy.setLeggings(player.getInventory().getLeggings());
        decoy.setBoots(player.getInventory().getBoots());
        decoy.setItemInHand(player.getInventory().getItemInMainHand());

        // Set properties
        decoy.setGravity(false);
        decoy.setVisible(true);
        decoy.setBasePlate(false);
        decoy.setArms(true);
        decoy.setMarker(false); // Can be targeted by mobs
        decoy.setCustomName(player.getName() + "'s Decoy");
        decoy.setCustomNameVisible(false);

        // Spawn particles
        player.getWorld().spawnParticle(
            Particle.PORTAL,
            loc,
            30,
            0.5, 1.0, 0.5,
            0.1
        );

        // Remove after 5 seconds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!decoy.isDead()) {
                player.getWorld().spawnParticle(
                    Particle.SMOKE,
                    decoy.getLocation(),
                    20,
                    0.3, 0.5, 0.3,
                    0.05
                );
                decoy.remove();
            }
        }, 100L);
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§8§m--------------------§r §6Trait Commands §8§m--------------------");
        sender.sendMessage("§e/ptrait info [player] §7- Display trait info");
        sender.sendMessage("§e/ptrait decoy §7- Spawn harmless decoy (Illusionist Trait)");
        if (sender.hasPermission("lw.rank.lord")) {
            sender.sendMessage("§e/ptrait revealelement <player> §7- Reveal element (Lord)");
            sender.sendMessage("§e/ptrait bless <player> §7- Bless Ultimate item (Lord)");
        }
        if (sender.isOp()) {
            sender.sendMessage("§e/ptrait temple <element> §7- Mark temple complete (Admin)");
            sender.sendMessage("§e/ptrait set <trait> §7- Force trait assignment (Admin)");
            sender.sendMessage("§e/ptrait quiz §7- Reset quiz (Admin)");
        }
        sender.sendMessage("§8§m------------------------------------------------");
    }

    // ========== FORMATTING HELPERS ==========

    private String formatTrait(PersonalityTrait trait, TraitTier tier) {
        return tier.getColor().toString() + tier.getDisplayName() + " " + trait.getDisplayName() + " " + trait.getSymbol();
    }

    private String formatElement(PlayerTraitProfile profile) {
        Element element = profile.element();
        boolean revealed = profile.elementRevealed();
        boolean activated = profile.elementActivated();
        return formatElement(element, revealed, activated);
    }

    private String formatElement(Element element, boolean revealed, boolean activated) {
        if (!revealed) {
            return "§8??? §7(Not yet revealed)";
        }
        String activationStatus = activated ? "§a✓ Activated" : "§7✗ Not activated";
        return element.getColor().toString() + element.getDisplayName() + " " + element.getSymbol() + " §7(" + activationStatus + "§7)";
    }

    private String formatCompletion(int percent) {
        if (percent >= 300) {
            return "§6300% §7(Post-Game)";
        } else if (percent >= 200) {
            return "§a" + percent + "% §7(All Temples)";
        } else if (percent >= 100) {
            return "§e" + percent + "% §7(Ultimate Tier)";
        } else if (percent >= 50) {
            return "§b" + percent + "% §7(Master Tier)";
        } else if (percent >= 25) {
            return "§f" + percent + "% §7(Trait Tier)";
        } else {
            return "§7" + percent + "% §7(Apprentice)";
        }
    }

    private String formatTemples(PlayerTraitProfile profile) {
        int completed = profile.templeCompletions().values().stream()
            .filter(b -> b)
            .mapToInt(b -> 1)
            .sum();
        return "§e" + completed + "§7/§e5 §7temples complete";
    }
}
