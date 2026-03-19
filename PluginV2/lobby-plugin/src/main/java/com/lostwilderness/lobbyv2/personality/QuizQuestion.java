package com.lostwilderness.lobbyv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PersonalityTrait;

import java.util.List;
import java.util.Map;

/**
 * Immutable record representing a single personality quiz question.
 * Supports multiple choice format with trait/element scoring matrices.
 */
public record QuizQuestion(
    int questionNumber,
    QuizType type,
    String questionText,
    List<String> choices,
    Map<String, Map<PersonalityTrait, Integer>> traitScoring,
    Map<String, Map<Element, Integer>> elementScoring
) {
    /**
     * Quiz question type (future expansion for crafting/creative challenges)
     */
    public enum QuizType {
        MULTIPLE_CHOICE
    }

    /**
     * Get trait points awarded for a specific answer.
     */
    public int getTraitPoints(String answer, PersonalityTrait trait) {
        if (!traitScoring.containsKey(answer)) return 0;
        return traitScoring.get(answer).getOrDefault(trait, 0);
    }

    /**
     * Get element points awarded for a specific answer.
     */
    public int getElementPoints(String answer, Element element) {
        if (!elementScoring.containsKey(answer)) return 0;
        return elementScoring.get(answer).getOrDefault(element, 0);
    }

    /**
     * Check if an answer is valid for this question.
     */
    public boolean isValidAnswer(String answer) {
        return choices.contains(answer);
    }
}
