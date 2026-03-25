package com.lostwilderness.rpgcore.pets.command;

import com.lostwilderness.rpgcore.pets.PetProfile;
import com.lostwilderness.rpgcore.pets.PetService;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Main command for pet system.
 * Subcommands: list, graveyard, info, rename, find
 */
public final class PetCommand implements CommandExecutor, TabCompleter {

    private final PetService petService;

    public PetCommand(PetService petService) {
        this.petService = petService;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        if (args.length == 0) {
            showHelp(player);
            return true;
        }

        String subcommand = args[0].toLowerCase();

        switch (subcommand) {
            case "list" -> handleList(player);
            case "graveyard" -> handleGraveyard(player);
            case "info" -> handleInfo(player, args);
            case "rename" -> handleRename(player, args);
            case "find" -> handleFind(player, args);
            default -> showHelp(player);
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "=== Pet Commands ===");
        player.sendMessage(ChatColor.YELLOW + "/pets list" + ChatColor.GRAY + " - Show your living pets");
        player.sendMessage(ChatColor.YELLOW + "/pets graveyard" + ChatColor.GRAY + " - Show deceased pets");
        player.sendMessage(ChatColor.YELLOW + "/pets info <name>" + ChatColor.GRAY + " - View pet details");
        player.sendMessage(ChatColor.YELLOW + "/pets rename <old> <new>" + ChatColor.GRAY + " - Rename a pet");
        player.sendMessage(ChatColor.YELLOW + "/pets find <name>" + ChatColor.GRAY + " - Re-link lost pet");
    }

    private void handleList(Player player) {
        petService.getActivePets(player.getUniqueId()).thenAccept(pets -> {
            if (pets.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "You have no pets.");
                player.sendMessage(ChatColor.GRAY + "Tame an animal to register your first pet!");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "=== Your Pets (" + pets.size() + "/3) ===");
            for (PetProfile pet : pets) {
                String name = pet.customName() != null ? pet.customName() : pet.entityType().name();
                player.sendMessage(ChatColor.GREEN + "• " + name + ChatColor.GRAY +
                    " (" + pet.entityType() + ") - " + pet.zodiacSign().displayName() +
                    " - Tamed: " + pet.tameMcDate());
            }
            player.sendMessage(ChatColor.GRAY + "Use /pets info <name> for details.");
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Error loading pets: " + ex.getMessage());
            return null;
        });
    }

    private void handleGraveyard(Player player) {
        petService.getGraveyard(player.getUniqueId()).thenAccept(pets -> {
            if (pets.isEmpty()) {
                player.sendMessage(ChatColor.YELLOW + "Your graveyard is empty.");
                player.sendMessage(ChatColor.GRAY + "No pets have passed away yet.");
                return;
            }

            player.sendMessage(ChatColor.GOLD + "=== Pet Graveyard (" + pets.size() + " souls) ===");
            for (PetProfile pet : pets) {
                String name = pet.customName() != null ? pet.customName() : pet.entityType().name();
                player.sendMessage(ChatColor.DARK_GRAY + "☠ " + name + ChatColor.GRAY +
                    " (" + pet.entityType() + ")");
                player.sendMessage(ChatColor.GRAY + "  Born: " + pet.tameMcDate() +
                    " | Died: " + pet.deathMcDate());
            }
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Error loading graveyard: " + ex.getMessage());
            return null;
        });
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pets info <name>");
            return;
        }

        String petName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        petService.findPetByName(player.getUniqueId(), petName).thenAccept(opt -> {
            if (opt.isEmpty()) {
                player.sendMessage(ChatColor.RED + "No pet found with name: " + petName);
                player.sendMessage(ChatColor.GRAY + "Use /pets list to see your pets.");
                return;
            }

            PetProfile pet = opt.get();
            String displayName = pet.customName() != null ? pet.customName() : pet.entityType().name();

            player.sendMessage(ChatColor.GOLD + "=== " + displayName + " ===");
            player.sendMessage(ChatColor.YELLOW + "Type: " + ChatColor.WHITE + pet.entityType());
            player.sendMessage(ChatColor.YELLOW + "Zodiac: " + ChatColor.WHITE + pet.zodiacSign().displayName());

            if (pet.personalityRevealed()) {
                player.sendMessage(ChatColor.YELLOW + "Personality: " + ChatColor.WHITE + pet.personality().getDisplayName());
                player.sendMessage(ChatColor.GRAY + "  " + pet.personality().getDescription());
            } else {
                player.sendMessage(ChatColor.YELLOW + "Personality: " + ChatColor.DARK_GRAY + "??? " + ChatColor.GRAY + "(Visit the Lord)");
            }

            player.sendMessage(ChatColor.YELLOW + "Tamed: " + ChatColor.WHITE + pet.tameMcDate());

            if (!pet.isAlive()) {
                player.sendMessage(ChatColor.RED + "Status: Deceased");
                player.sendMessage(ChatColor.YELLOW + "Died: " + ChatColor.WHITE + pet.deathMcDate());
            } else {
                player.sendMessage(ChatColor.GREEN + "Status: Alive");
            }

            if (pet.isLost()) {
                player.sendMessage(ChatColor.DARK_RED + "⚠ Lost - last seen: " + pet.lastSeenLocation());
            }
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Error loading pet info: " + ex.getMessage());
            return null;
        });
    }

    private void handleRename(Player player, String[] args) {
        if (args.length < 3) {
            player.sendMessage(ChatColor.RED + "Usage: /pets rename <old name> <new name>");
            return;
        }

        // Find where "new name" starts (everything after first name)
        int newNameStart = -1;
        for (int i = 1; i < args.length - 1; i++) {
            // Try to find the pet with name up to this point
            String testName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, i + 1));
            // We'll just assume the last arg is the new name for simplicity
        }

        // Simple split: assume single-word old name for now
        String oldName = args[1];
        String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));

        petService.renamePet(player.getUniqueId(), oldName, newName).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Pet renamed: " + oldName + " → " + newName);
            } else {
                player.sendMessage(ChatColor.RED + "No pet found with name: " + oldName);
            }
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Error renaming pet: " + ex.getMessage());
            return null;
        });
    }

    private void handleFind(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.RED + "Usage: /pets find <name>");
            return;
        }

        String petName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));

        player.sendMessage(ChatColor.YELLOW + "Searching for " + petName + " within 32 blocks...");

        petService.manualReconcile(player.getUniqueId(), petName, player.getLocation()).thenAccept(success -> {
            if (success) {
                player.sendMessage(ChatColor.GREEN + "Found and re-linked " + petName + "!");
            } else {
                player.sendMessage(ChatColor.RED + "Could not find " + petName + " nearby.");
                player.sendMessage(ChatColor.GRAY + "Make sure the pet is within 32 blocks.");
            }
        }).exceptionally(ex -> {
            player.sendMessage(ChatColor.RED + "Error finding pet: " + ex.getMessage());
            return null;
        });
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.add("list");
            completions.add("graveyard");
            completions.add("info");
            completions.add("rename");
            completions.add("find");
        } else if (args.length == 2 && (args[0].equalsIgnoreCase("info") ||
                                         args[0].equalsIgnoreCase("rename") ||
                                         args[0].equalsIgnoreCase("find"))) {
            // Tab-complete pet names (if player is online)
            if (sender instanceof Player player) {
                try {
                    List<PetProfile> pets = petService.getActivePets(player.getUniqueId()).join();
                    for (PetProfile pet : pets) {
                        if (pet.customName() != null) {
                            completions.add(pet.customName());
                        }
                    }
                } catch (Exception ignored) {
                    // Fail silently for tab completion
                }
            }
        }

        return completions;
    }
}
