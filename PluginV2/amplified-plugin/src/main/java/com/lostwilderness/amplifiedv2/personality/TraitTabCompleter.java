package com.lostwilderness.amplifiedv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PersonalityTrait;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Tab completion for /trait command
 */
public class TraitTabCompleter implements TabCompleter {

    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "info", "revealelement", "bless", "temple", "set", "quiz"
    );

    private static final List<String> TRAIT_NAMES = Arrays.stream(PersonalityTrait.values())
        .map(Enum::name)
        .map(String::toLowerCase)
        .collect(Collectors.toList());

    private static final List<String> ELEMENT_NAMES = Arrays.stream(Element.values())
        .map(Enum::name)
        .map(String::toLowerCase)
        .collect(Collectors.toList());

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        // First argument: subcommand
        if (args.length == 1) {
            completions.addAll(filterMatches(SUBCOMMANDS, args[0]));
            return completions;
        }

        // Second argument: depends on subcommand
        if (args.length == 2) {
            String subcommand = args[0].toLowerCase();
            switch (subcommand) {
                case "info":
                case "revealelement":
                case "bless":
                    // Player names
                    completions.addAll(getOnlinePlayerNames(args[1]));
                    break;
                case "temple":
                    // Element names
                    completions.addAll(filterMatches(ELEMENT_NAMES, args[1]));
                    break;
                case "set":
                    // Trait names
                    completions.addAll(filterMatches(TRAIT_NAMES, args[1]));
                    break;
                case "quiz":
                    // No arguments
                    break;
            }
        }

        return completions;
    }

    /**
     * Get online player names that match the partial input
     */
    private List<String> getOnlinePlayerNames(String partial) {
        return Bukkit.getOnlinePlayers().stream()
            .map(Player::getName)
            .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }

    /**
     * Filter a list of strings by partial match
     */
    private List<String> filterMatches(List<String> options, String partial) {
        return options.stream()
            .filter(option -> option.toLowerCase().startsWith(partial.toLowerCase()))
            .sorted()
            .collect(Collectors.toList());
    }
}
