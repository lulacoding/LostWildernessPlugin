package com.lostwilderness.lobbyv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PersonalityTrait;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Central manager for personality quiz sessions.
 * Handles quiz flow, player movement restriction, and answer processing.
 */
public class QuizSessionManager {

    private final QuizCompletionHandler completionHandler;
    private final Map<UUID, QuizSession> activeSessions;
    private static final List<QuizQuestion> QUIZ_QUESTIONS = createQuizQuestions();

    // Quiz lock duration (20 seconds per question + buffer)
    private static final int LOCK_TICKS_PER_QUESTION = 400; // 20 seconds

    public QuizSessionManager(Plugin plugin, QuizCompletionHandler completionHandler) {
        this.completionHandler = completionHandler;
        this.activeSessions = new WeakHashMap<>();
    }

    /**
     * Start a new quiz session for a player.
     */
    public void startQuiz(Player player) {
        UUID uuid = player.getUniqueId();

        if (activeSessions.containsKey(uuid)) {
            player.sendMessage("§cYou already have an active quiz session!");
            return;
        }

        QuizSession session = new QuizSession(uuid);
        activeSessions.put(uuid, session);

        freezePlayer(player);
        displayQuestion(player, session);
    }

    /**
     * Record an answer and advance to next question or complete quiz.
     */
    public void answerQuestion(Player player, String answer) {
        UUID uuid = player.getUniqueId();
        QuizSession session = activeSessions.get(uuid);

        if (session == null) {
            player.sendMessage("§cYou don't have an active quiz session!");
            return;
        }

        if (session.isCompleted()) {
            player.sendMessage("§cYou've already completed the quiz!");
            return;
        }

        QuizQuestion currentQuestion = QUIZ_QUESTIONS.get(session.getCurrentQuestionIndex());

        // Parse answer as number and convert to choice text
        String choiceText;
        try {
            int choiceIndex = Integer.parseInt(answer) - 1; // Convert to 0-indexed
            if (choiceIndex < 0 || choiceIndex >= currentQuestion.choices().size()) {
                player.sendMessage("§cInvalid choice! Please enter a number between 1 and " + currentQuestion.choices().size());
                return;
            }
            choiceText = currentQuestion.choices().get(choiceIndex);
        } catch (NumberFormatException e) {
            player.sendMessage("§cPlease enter a number (e.g., 1, 2, 3, 4)");
            return;
        }

        // Record answer and update scores
        session.recordAnswer(choiceText);

        for (PersonalityTrait trait : PersonalityTrait.values()) {
            int points = currentQuestion.getTraitPoints(choiceText, trait);
            if (points > 0) {
                session.addTraitPoints(trait, points);
            }
        }

        for (Element element : Element.values()) {
            int points = currentQuestion.getElementPoints(choiceText, element);
            if (points > 0) {
                session.addElementPoints(element, points);
            }
        }

        session.advanceQuestion();

        // Check if quiz is complete
        if (session.getCurrentQuestionIndex() >= QUIZ_QUESTIONS.size()) {
            completeQuiz(player, session);
        } else {
            displayQuestion(player, session);
        }
    }

    /**
     * Display the current question to the player.
     */
    private void displayQuestion(Player player, QuizSession session) {
        QuizQuestion question = QUIZ_QUESTIONS.get(session.getCurrentQuestionIndex());

        player.sendMessage("");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
        player.sendMessage("§e§lPersonality Quiz §7(Question " + (session.getCurrentQuestionIndex() + 1) + "/10)");
        player.sendMessage("");
        player.sendMessage("§f" + question.questionText());
        player.sendMessage("");

        for (int i = 0; i < question.choices().size(); i++) {
            player.sendMessage("§a[" + (i + 1) + "]§f " + question.choices().get(i));
        }

        player.sendMessage("");
        player.sendMessage("§7Type your answer number in chat (1-" + question.choices().size() + ")");
        player.sendMessage("§6§l━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
    }

    /**
     * Complete the quiz and process results.
     */
    private void completeQuiz(Player player, QuizSession session) {
        session.markCompleted();
        unfreezePlayer(player);

        player.sendMessage("");
        player.sendMessage("§a§lQuiz Complete!");
        player.sendMessage("§7Calculating your personality profile...");

        // Hand off to completion handler
        completionHandler.onQuizComplete(player, session);

        // Remove session from active sessions
        activeSessions.remove(player.getUniqueId());
    }

    /**
     * Freeze player in place (SLOWNESS + JUMP_BOOST)
     */
    private void freezePlayer(Player player) {
        int totalLockTicks = LOCK_TICKS_PER_QUESTION * QUIZ_QUESTIONS.size();
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, totalLockTicks, 10, false, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, totalLockTicks, 128, false, false, false));
    }

    /**
     * Unfreeze player (remove effects)
     */
    private void unfreezePlayer(Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.JUMP_BOOST);
    }

    /**
     * Check if player has an active quiz session.
     */
    public boolean hasActiveSession(UUID uuid) {
        return activeSessions.containsKey(uuid);
    }

    /**
     * Cancel a quiz session (on disconnect, etc.)
     */
    public void cancelSession(UUID uuid) {
        activeSessions.remove(uuid);
    }

    /**
     * Create the static list of 10 quiz questions.
     */
    private static List<QuizQuestion> createQuizQuestions() {
        List<QuizQuestion> questions = new ArrayList<>();

        // Question 1: Combat Style
        questions.add(new QuizQuestion(
            1,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "You encounter a powerful enemy. What's your approach?",
            List.of(
                "Charge in with melee weapons blazing",
                "Stay back and attack from range",
                "Use magic and potions strategically",
                "Summon allies to fight alongside you"
            ),
            Map.of(
                "Charge in with melee weapons blazing", Map.of(
                    PersonalityTrait.WARRIOR, 3,
                    PersonalityTrait.BERSERKER, 2
                ),
                "Stay back and attack from range", Map.of(
                    PersonalityTrait.ARCHER, 3,
                    PersonalityTrait.SCOUT, 1
                ),
                "Use magic and potions strategically", Map.of(
                    PersonalityTrait.MAGE, 3,
                    PersonalityTrait.ALCHEMIST, 2
                ),
                "Summon allies to fight alongside you", Map.of(
                    PersonalityTrait.TAMER, 3,
                    PersonalityTrait.HEALER, 1
                )
            ),
            Map.of(
                "Charge in with melee weapons blazing", Map.of(Element.FIRE, 2),
                "Stay back and attack from range", Map.of(Element.WIND, 2),
                "Use magic and potions strategically", Map.of(Element.AETHER, 2),
                "Summon allies to fight alongside you", Map.of(Element.EARTH, 2)
            )
        ));

        // Question 2: Exploration Preference
        questions.add(new QuizQuestion(
            2,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "What environment do you feel most at home in?",
            List.of(
                "Dense forests and jungles",
                "High mountains and cliffs",
                "Deep caves and mines",
                "Open oceans and rivers"
            ),
            Map.of(
                "Dense forests and jungles", Map.of(
                    PersonalityTrait.RANGER, 3,
                    PersonalityTrait.TAMER, 2
                ),
                "High mountains and cliffs", Map.of(
                    PersonalityTrait.SCOUT, 3,
                    PersonalityTrait.ARCHER, 2
                ),
                "Deep caves and mines", Map.of(
                    PersonalityTrait.SMITH, 3,
                    PersonalityTrait.BERSERKER, 1
                ),
                "Open oceans and rivers", Map.of(
                    PersonalityTrait.HEALER, 2,
                    PersonalityTrait.ILLUSIONIST, 2
                )
            ),
            Map.of(
                "Dense forests and jungles", Map.of(Element.EARTH, 3),
                "High mountains and cliffs", Map.of(Element.WIND, 3),
                "Deep caves and mines", Map.of(Element.FIRE, 2),
                "Open oceans and rivers", Map.of(Element.WATER, 3)
            )
        ));

        // Question 3: Resource Management
        questions.add(new QuizQuestion(
            3,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "You find rare resources. What do you do?",
            List.of(
                "Craft powerful gear immediately",
                "Save them for later upgrades",
                "Brew potions with them",
                "Share with allies for better group equipment"
            ),
            Map.of(
                "Craft powerful gear immediately", Map.of(
                    PersonalityTrait.SMITH, 3,
                    PersonalityTrait.WARRIOR, 2
                ),
                "Save them for later upgrades", Map.of(
                    PersonalityTrait.SAGE, 3,
                    PersonalityTrait.RUNEKEEPER, 2
                ),
                "Brew potions with them", Map.of(
                    PersonalityTrait.ALCHEMIST, 3,
                    PersonalityTrait.MAGE, 1
                ),
                "Share with allies for better group equipment", Map.of(
                    PersonalityTrait.HEALER, 3,
                    PersonalityTrait.RANGER, 1
                )
            ),
            Map.of(
                "Craft powerful gear immediately", Map.of(Element.FIRE, 2),
                "Save them for later upgrades", Map.of(Element.AETHER, 2),
                "Brew potions with them", Map.of(Element.WATER, 2),
                "Share with allies for better group equipment", Map.of(Element.EARTH, 2)
            )
        ));

        // Question 4: Problem Solving
        questions.add(new QuizQuestion(
            4,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "You face a complex puzzle or challenge. How do you solve it?",
            List.of(
                "Study and research the solution carefully",
                "Try different approaches until something works",
                "Use unconventional or creative methods",
                "Brute force through obstacles"
            ),
            Map.of(
                "Study and research the solution carefully", Map.of(
                    PersonalityTrait.SAGE, 3,
                    PersonalityTrait.RUNEKEEPER, 2
                ),
                "Try different approaches until something works", Map.of(
                    PersonalityTrait.SCOUT, 3,
                    PersonalityTrait.ALCHEMIST, 2
                ),
                "Use unconventional or creative methods", Map.of(
                    PersonalityTrait.ILLUSIONIST, 3,
                    PersonalityTrait.MAGE, 2
                ),
                "Brute force through obstacles", Map.of(
                    PersonalityTrait.BERSERKER, 3,
                    PersonalityTrait.WARRIOR, 2
                )
            ),
            Map.of(
                "Study and research the solution carefully", Map.of(Element.AETHER, 3),
                "Try different approaches until something works", Map.of(Element.WIND, 2),
                "Use unconventional or creative methods", Map.of(Element.WATER, 2),
                "Brute force through obstacles", Map.of(Element.FIRE, 3)
            )
        ));

        // Question 5: Social Role
        questions.add(new QuizQuestion(
            5,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "In a group, what role do you naturally take?",
            List.of(
                "Leader and strategist",
                "Supporter and healer",
                "Damage dealer and frontline",
                "Scout and information gatherer"
            ),
            Map.of(
                "Leader and strategist", Map.of(
                    PersonalityTrait.SAGE, 2,
                    PersonalityTrait.WARRIOR, 2
                ),
                "Supporter and healer", Map.of(
                    PersonalityTrait.HEALER, 3,
                    PersonalityTrait.ALCHEMIST, 2
                ),
                "Damage dealer and frontline", Map.of(
                    PersonalityTrait.BERSERKER, 3,
                    PersonalityTrait.ARCHER, 2
                ),
                "Scout and information gatherer", Map.of(
                    PersonalityTrait.SCOUT, 3,
                    PersonalityTrait.RANGER, 2
                )
            ),
            Map.of(
                "Leader and strategist", Map.of(Element.AETHER, 2),
                "Supporter and healer", Map.of(Element.WATER, 3),
                "Damage dealer and frontline", Map.of(Element.FIRE, 3),
                "Scout and information gatherer", Map.of(Element.WIND, 3)
            )
        ));

        // Question 6: Crafting Preference
        questions.add(new QuizQuestion(
            6,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "What type of items do you most enjoy creating?",
            List.of(
                "Weapons and armor",
                "Potions and consumables",
                "Enchanted tools and books",
                "Decorative builds and structures"
            ),
            Map.of(
                "Weapons and armor", Map.of(
                    PersonalityTrait.SMITH, 3,
                    PersonalityTrait.WARRIOR, 1
                ),
                "Potions and consumables", Map.of(
                    PersonalityTrait.ALCHEMIST, 3,
                    PersonalityTrait.MAGE, 2
                ),
                "Enchanted tools and books", Map.of(
                    PersonalityTrait.RUNEKEEPER, 3,
                    PersonalityTrait.SAGE, 2
                ),
                "Decorative builds and structures", Map.of(
                    PersonalityTrait.ILLUSIONIST, 2,
                    PersonalityTrait.RANGER, 2
                )
            ),
            Map.of(
                "Weapons and armor", Map.of(Element.EARTH, 3),
                "Potions and consumables", Map.of(Element.WATER, 3),
                "Enchanted tools and books", Map.of(Element.AETHER, 3),
                "Decorative builds and structures", Map.of(Element.WIND, 2)
            )
        ));

        // Question 7: Risk Taking
        questions.add(new QuizQuestion(
            7,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "How do you feel about taking risks?",
            List.of(
                "I thrive on danger and high-risk situations",
                "I take calculated risks when necessary",
                "I prefer safe, reliable strategies",
                "I avoid risk but adapt when forced"
            ),
            Map.of(
                "I thrive on danger and high-risk situations", Map.of(
                    PersonalityTrait.BERSERKER, 3,
                    PersonalityTrait.SCOUT, 2
                ),
                "I take calculated risks when necessary", Map.of(
                    PersonalityTrait.WARRIOR, 2,
                    PersonalityTrait.ARCHER, 2
                ),
                "I prefer safe, reliable strategies", Map.of(
                    PersonalityTrait.SAGE, 3,
                    PersonalityTrait.SMITH, 2
                ),
                "I avoid risk but adapt when forced", Map.of(
                    PersonalityTrait.HEALER, 2,
                    PersonalityTrait.RANGER, 2
                )
            ),
            Map.of(
                "I thrive on danger and high-risk situations", Map.of(Element.FIRE, 3),
                "I take calculated risks when necessary", Map.of(Element.WIND, 2),
                "I prefer safe, reliable strategies", Map.of(Element.EARTH, 3),
                "I avoid risk but adapt when forced", Map.of(Element.WATER, 2)
            )
        ));

        // Question 8: Magic vs Physical
        questions.add(new QuizQuestion(
            8,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "Do you prefer magic or physical prowess?",
            List.of(
                "Pure magic and arcane power",
                "A balance of both magic and melee",
                "Pure physical strength and skill",
                "Tactics and strategy over raw power"
            ),
            Map.of(
                "Pure magic and arcane power", Map.of(
                    PersonalityTrait.MAGE, 3,
                    PersonalityTrait.RUNEKEEPER, 2
                ),
                "A balance of both magic and melee", Map.of(
                    PersonalityTrait.ALCHEMIST, 2,
                    PersonalityTrait.ILLUSIONIST, 2
                ),
                "Pure physical strength and skill", Map.of(
                    PersonalityTrait.WARRIOR, 3,
                    PersonalityTrait.BERSERKER, 3
                ),
                "Tactics and strategy over raw power", Map.of(
                    PersonalityTrait.SCOUT, 3,
                    PersonalityTrait.SAGE, 2
                )
            ),
            Map.of(
                "Pure magic and arcane power", Map.of(Element.AETHER, 3),
                "A balance of both magic and melee", Map.of(Element.WATER, 2),
                "Pure physical strength and skill", Map.of(Element.FIRE, 3),
                "Tactics and strategy over raw power", Map.of(Element.WIND, 2)
            )
        ));

        // Question 9: Animal Companionship
        questions.add(new QuizQuestion(
            9,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "What's your relationship with animals?",
            List.of(
                "I tame and befriend every creature I meet",
                "Animals are useful resources and tools",
                "I respect nature from a distance",
                "I prefer mechanical or magical constructs"
            ),
            Map.of(
                "I tame and befriend every creature I meet", Map.of(
                    PersonalityTrait.TAMER, 3,
                    PersonalityTrait.RANGER, 2
                ),
                "Animals are useful resources and tools", Map.of(
                    PersonalityTrait.SMITH, 2,
                    PersonalityTrait.ALCHEMIST, 2
                ),
                "I respect nature from a distance", Map.of(
                    PersonalityTrait.SAGE, 2,
                    PersonalityTrait.ARCHER, 2
                ),
                "I prefer mechanical or magical constructs", Map.of(
                    PersonalityTrait.RUNEKEEPER, 3,
                    PersonalityTrait.MAGE, 2
                )
            ),
            Map.of(
                "I tame and befriend every creature I meet", Map.of(Element.EARTH, 3),
                "Animals are useful resources and tools", Map.of(Element.FIRE, 2),
                "I respect nature from a distance", Map.of(Element.WIND, 2),
                "I prefer mechanical or magical constructs", Map.of(Element.AETHER, 3)
            )
        ));

        // Question 10: Long-term Goals
        questions.add(new QuizQuestion(
            10,
            QuizQuestion.QuizType.MULTIPLE_CHOICE,
            "What's your ultimate goal in this world?",
            List.of(
                "Become the strongest warrior",
                "Master all knowledge and skills",
                "Build a thriving community",
                "Explore every corner of the world"
            ),
            Map.of(
                "Become the strongest warrior", Map.of(
                    PersonalityTrait.WARRIOR, 3,
                    PersonalityTrait.BERSERKER, 2
                ),
                "Master all knowledge and skills", Map.of(
                    PersonalityTrait.SAGE, 3,
                    PersonalityTrait.RUNEKEEPER, 2
                ),
                "Build a thriving community", Map.of(
                    PersonalityTrait.HEALER, 3,
                    PersonalityTrait.SMITH, 2
                ),
                "Explore every corner of the world", Map.of(
                    PersonalityTrait.SCOUT, 3,
                    PersonalityTrait.RANGER, 2
                )
            ),
            Map.of(
                "Become the strongest warrior", Map.of(Element.FIRE, 3),
                "Master all knowledge and skills", Map.of(Element.AETHER, 3),
                "Build a thriving community", Map.of(Element.EARTH, 3),
                "Explore every corner of the world", Map.of(Element.WIND, 3)
            )
        ));

        return questions;
    }
}
