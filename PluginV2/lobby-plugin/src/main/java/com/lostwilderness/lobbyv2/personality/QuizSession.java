package com.lostwilderness.lobbyv2.personality;

import com.lostwilderness.rpgcore.personality.Element;
import com.lostwilderness.rpgcore.personality.PersonalityTrait;

import java.time.Instant;
import java.util.*;

/**
 * In-memory state for an active personality quiz session.
 * Stored in WeakHashMap in QuizSessionManager, cleared on completion.
 */
public class QuizSession {
    private final UUID playerUuid;
    private final Instant startTime;
    private int currentQuestionIndex;
    private final List<String> answers;
    private final Map<PersonalityTrait, Integer> traitScores;
    private final Map<Element, Integer> elementScores;
    private boolean completed;

    public QuizSession(UUID playerUuid) {
        this.playerUuid = playerUuid;
        this.startTime = Instant.now();
        this.currentQuestionIndex = 0;
        this.answers = new ArrayList<>();
        this.traitScores = new EnumMap<>(PersonalityTrait.class);
        this.elementScores = new EnumMap<>(Element.class);
        this.completed = false;

        // Initialize all scores to 0
        for (PersonalityTrait trait : PersonalityTrait.values()) {
            traitScores.put(trait, 0);
        }
        for (Element element : Element.values()) {
            elementScores.put(element, 0);
        }
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public int getCurrentQuestionIndex() {
        return currentQuestionIndex;
    }

    public void advanceQuestion() {
        this.currentQuestionIndex++;
    }

    public void recordAnswer(String answer) {
        this.answers.add(answer);
    }

    public List<String> getAnswers() {
        return Collections.unmodifiableList(answers);
    }

    public void addTraitPoints(PersonalityTrait trait, int points) {
        traitScores.merge(trait, points, Integer::sum);
    }

    public void addElementPoints(Element element, int points) {
        elementScores.merge(element, points, Integer::sum);
    }

    public Map<PersonalityTrait, Integer> getTraitScores() {
        return Collections.unmodifiableMap(traitScores);
    }

    public Map<Element, Integer> getElementScores() {
        return Collections.unmodifiableMap(elementScores);
    }

    public boolean isCompleted() {
        return completed;
    }

    public void markCompleted() {
        this.completed = true;
    }

    /**
     * Get the winning trait (highest score, ties broken randomly)
     */
    public PersonalityTrait getWinningTrait() {
        return traitScores.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(PersonalityTrait.WARRIOR); // Default fallback
    }

    /**
     * Get the winning element (highest score, ties broken randomly)
     */
    public Element getWinningElement() {
        return elementScores.entrySet().stream()
            .max(Comparator.comparingInt(Map.Entry::getValue))
            .map(Map.Entry::getKey)
            .orElse(Element.FIRE); // Default fallback
    }
}
