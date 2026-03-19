package com.lostwilderness.lobbyv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PersonalityTrait;
import com.lostwilderness.rpgcore.personality.TraitRepository;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Handles quiz completion: calculates results, assigns traits, sends messages.
 */
public class QuizCompletionHandler {

    private final Plugin plugin;
    private final TraitRepository traitRepository;

    public QuizCompletionHandler(Plugin plugin, TraitRepository traitRepository) {
        this.plugin = plugin;
        this.traitRepository = traitRepository;
    }

    /**
     * Process quiz completion and assign trait + element.
     */
    public void onQuizComplete(Player player, QuizSession session) {
        UUID uuid = player.getUniqueId();

        // Calculate winning trait and element
        PersonalityTrait winningTrait = calculateWinnerTrait(session.getTraitScores());
        Element winningElement = calculateWinnerElement(session.getElementScores());

        // Save quiz answers for analytics (async)
        saveQuizAnswersAsync(uuid, session.getAnswers());

        // Assign trait and element (async)
        assignTraitAndElement(player, uuid, winningTrait, winningElement);
    }

    /**
     * Calculate winning trait (highest score, ties broken randomly).
     */
    private PersonalityTrait calculateWinnerTrait(Map<PersonalityTrait, Integer> scores) {
        if (scores.isEmpty()) {
            return PersonalityTrait.WARRIOR; // Default fallback
        }

        int maxScore = scores.values().stream().max(Integer::compare).orElse(0);

        // Collect all traits with max score (for tie-breaking)
        List<PersonalityTrait> winners = new ArrayList<>();
        for (Map.Entry<PersonalityTrait, Integer> entry : scores.entrySet()) {
            if (entry.getValue() == maxScore) {
                winners.add(entry.getKey());
            }
        }

        // Random tie-breaker
        if (winners.isEmpty()) {
            return PersonalityTrait.WARRIOR;
        } else if (winners.size() == 1) {
            return winners.get(0);
        } else {
            return winners.get(ThreadLocalRandom.current().nextInt(winners.size()));
        }
    }

    /**
     * Calculate winning element (highest score, ties broken randomly).
     */
    private Element calculateWinnerElement(Map<Element, Integer> scores) {
        if (scores.isEmpty()) {
            return Element.FIRE; // Default fallback
        }

        int maxScore = scores.values().stream().max(Integer::compare).orElse(0);

        // Collect all elements with max score (for tie-breaking)
        List<Element> winners = new ArrayList<>();
        for (Map.Entry<Element, Integer> entry : scores.entrySet()) {
            if (entry.getValue() == maxScore) {
                winners.add(entry.getKey());
            }
        }

        // Random tie-breaker
        if (winners.isEmpty()) {
            return Element.FIRE;
        } else if (winners.size() == 1) {
            return winners.get(0);
        } else {
            return winners.get(ThreadLocalRandom.current().nextInt(winners.size()));
        }
    }

    /**
     * Assign trait and element to player (async).
     */
    private void assignTraitAndElement(Player player, UUID uuid, PersonalityTrait trait, Element element) {
        traitRepository.assignTrait(uuid, trait, element)
            .thenRun(() -> {
                // Send completion messages on main thread
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (!player.isOnline()) return;

                    sendCompletionMessage(player, trait, element);
                    allowSurvivalEntry(player);
                });
            })
            .exceptionally(ex -> {
                plugin.getLogger().severe("Failed to assign trait for " + uuid + ": " + ex.getMessage());
                ex.printStackTrace();
                return null;
            });
    }

    /**
     * Save quiz answers for analytics (async, fire-and-forget).
     */
    private void saveQuizAnswersAsync(UUID uuid, List<String> answers) {
        for (int i = 0; i < answers.size(); i++) {
            int questionNumber = i + 1;
            String answer = answers.get(i);
            traitRepository.saveQuizAnswer(uuid, questionNumber, answer)
                .exceptionally(ex -> {
                    plugin.getLogger().warning("Failed to save quiz answer for " + uuid + " Q" + questionNumber);
                    return null;
                });
        }
    }

    /**
     * Send completion message with trait and element info.
     */
    private void sendCompletionMessage(Player player, PersonalityTrait trait, Element element) {
        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§lYour Personality Profile");
        player.sendMessage("");
        player.sendMessage("§7Primary Trait: " + trait.getFullDisplay());
        player.sendMessage("§8" + trait.getShortDescription());
        player.sendMessage("");
        player.sendMessage("§7Elemental Affinity: §8[Hidden]");
        player.sendMessage("§8Your element will be revealed by a Lord when you're ready.");
        player.sendMessage("");
        player.sendMessage("§aYou may now enter the Survival world!");
        player.sendMessage("§7Step into the Gateway to begin your journey.");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");

        // Title message
        player.sendTitle(
            trait.getColoredName(),
            "§7" + trait.getShortDescription(),
            10, 60, 20
        );
    }

    /**
     * Allow player to enter survival portal.
     * (This is handled by LobbyTeleportListener checking quiz completion)
     */
    private void allowSurvivalEntry(Player player) {
        player.sendMessage("§aThe Gateway to Survival is now accessible!");
    }
}
