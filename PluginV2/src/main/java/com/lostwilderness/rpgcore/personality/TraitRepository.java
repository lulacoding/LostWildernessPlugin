package com.lostwilderness.rpgcore.personality;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * Async database operations for personality trait system.
 * Uses "player" datasource. Supports H2 and MySQL/MariaDB.
 */
public final class TraitRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final Executor executor;
    private final boolean mysql;

    public TraitRepository(SqlExecutor sql, Executor executor, boolean useMysql) {
        this.sql = sql;
        this.executor = executor;
        this.mysql = useMysql;
    }

    /**
     * Create all tables for personality system.
     */
    public void createTablesIfNotExists() {
        // Main traits table
        String traitsTable = """
            CREATE TABLE IF NOT EXISTS player_traits (
                player_uuid VARCHAR(36) PRIMARY KEY,
                primary_trait VARCHAR(50) NOT NULL,
                primary_tier VARCHAR(20) NOT NULL,
                element VARCHAR(20) NOT NULL,
                element_revealed BOOLEAN NOT NULL DEFAULT FALSE,
                element_activated BOOLEAN NOT NULL DEFAULT FALSE,
                is_postgame BOOLEAN NOT NULL DEFAULT FALSE,
                assigned_at BIGINT NOT NULL,
                last_tier_advance BIGINT
            )
            """;

        // Temple completions table
        String templesTable = """
            CREATE TABLE IF NOT EXISTS player_temples (
                player_uuid VARCHAR(36) NOT NULL,
                element VARCHAR(20) NOT NULL,
                completed_at BIGINT NOT NULL,
                PRIMARY KEY (player_uuid, element)
            )
            """;

        // Quiz answers (for analytics/debugging)
        String quizTable = """
            CREATE TABLE IF NOT EXISTS player_quiz_answers (
                player_uuid VARCHAR(36) NOT NULL,
                question_number INT NOT NULL,
                answer TEXT NOT NULL,
                answered_at BIGINT NOT NULL,
                PRIMARY KEY (player_uuid, question_number)
            )
            """;

        // Holy Enchants tracking
        String enchantsTable = """
            CREATE TABLE IF NOT EXISTS player_holy_enchants (
                player_uuid VARCHAR(36) NOT NULL,
                holy_enchant VARCHAR(50) NOT NULL,
                granted_at BIGINT NOT NULL,
                source VARCHAR(20) NOT NULL,
                PRIMARY KEY (player_uuid, holy_enchant)
            )
            """;

        // Christmas claims
        String christmasTable = """
            CREATE TABLE IF NOT EXISTS player_christmas_claims (
                player_uuid VARCHAR(36) PRIMARY KEY,
                last_claim_year INT NOT NULL,
                enchants_claimed INT NOT NULL
            )
            """;

        sql.update(DATASOURCE, traitsTable).join();
        sql.update(DATASOURCE, templesTable).join();
        sql.update(DATASOURCE, quizTable).join();
        sql.update(DATASOURCE, enchantsTable).join();
        sql.update(DATASOURCE, christmasTable).join();
    }

    /**
     * Load complete trait profile for player.
     */
    public CompletableFuture<Optional<PlayerTraitProfile>> loadProfile(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT * FROM player_traits WHERE player_uuid = ?",
            rs -> mapTraitProfile(uuid, rs),
            uuid.toString());
    }

    /**
     * Check if player has completed quiz (has a trait assigned).
     */
    public CompletableFuture<Boolean> hasCompletedQuiz(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT COUNT(*) as cnt FROM player_traits WHERE player_uuid = ?",
            rs -> {
                if (!rs.next()) return false;
                return rs.getInt("cnt") > 0;
            },
            uuid.toString());
    }

    /**
     * Assign initial trait and element (after quiz completion).
     */
    public CompletableFuture<Void> assignTrait(UUID uuid, PersonalityTrait trait, Element element) {
        String upsert = mysql
            ? "INSERT INTO player_traits (player_uuid, primary_trait, primary_tier, element, element_revealed, element_activated, is_postgame, assigned_at) VALUES (?, ?, ?, ?, FALSE, FALSE, FALSE, ?) ON DUPLICATE KEY UPDATE primary_trait = VALUES(primary_trait), element = VALUES(element)"
            : "MERGE INTO player_traits (player_uuid, primary_trait, primary_tier, element, element_revealed, element_activated, is_postgame, assigned_at) KEY(player_uuid) VALUES (?, ?, ?, ?, FALSE, FALSE, FALSE, ?)";

        return sql.update(DATASOURCE, upsert,
            uuid.toString(),
            trait.name(),
            TraitTier.APPRENTICE.name(),
            element.name(),
            Instant.now().toEpochMilli()
        ).thenRun(() -> {});
    }

    /**
     * Save complete profile (upsert).
     */
    public CompletableFuture<Void> saveProfile(PlayerTraitProfile profile) {
        String upsert = mysql
            ? "INSERT INTO player_traits (player_uuid, primary_trait, primary_tier, element, element_revealed, element_activated, is_postgame, assigned_at, last_tier_advance) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE primary_tier = VALUES(primary_tier), element_revealed = VALUES(element_revealed), element_activated = VALUES(element_activated), is_postgame = VALUES(is_postgame), last_tier_advance = VALUES(last_tier_advance)"
            : "MERGE INTO player_traits (player_uuid, primary_trait, primary_tier, element, element_revealed, element_activated, is_postgame, assigned_at, last_tier_advance) KEY(player_uuid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        return sql.update(DATASOURCE, upsert,
            profile.playerUuid().toString(),
            profile.primaryTrait().name(),
            profile.primaryTier().name(),
            profile.element().name(),
            profile.elementRevealed(),
            profile.elementActivated(),
            profile.isPostGame(),
            profile.assignedAt().toEpochMilli(),
            profile.lastTierAdvance() != null ? profile.lastTierAdvance().toEpochMilli() : null
        ).thenRun(() -> {});
    }

    /**
     * Mark element as revealed.
     */
    public CompletableFuture<Void> revealElement(UUID uuid) {
        return sql.update(DATASOURCE,
            "UPDATE player_traits SET element_revealed = TRUE WHERE player_uuid = ?",
            uuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Mark temple as complete.
     */
    public CompletableFuture<Void> completeTemple(UUID uuid, Element element) {
        String insert = mysql
            ? "INSERT INTO player_temples (player_uuid, element, completed_at) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE completed_at = VALUES(completed_at)"
            : "MERGE INTO player_temples (player_uuid, element, completed_at) KEY(player_uuid, element) VALUES (?, ?, ?)";

        return sql.update(DATASOURCE, insert,
            uuid.toString(),
            element.name(),
            Instant.now().toEpochMilli()
        ).thenRun(() -> {});
    }

    /**
     * Grant Holy Enchants (add to granted set).
     */
    public CompletableFuture<Void> grantHolyEnchants(UUID uuid, Set<HolyEnchant> enchants, String source) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (HolyEnchant enchant : enchants) {
            String insert = mysql
                ? "INSERT INTO player_holy_enchants (player_uuid, holy_enchant, granted_at, source) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE granted_at = VALUES(granted_at)"
                : "MERGE INTO player_holy_enchants (player_uuid, holy_enchant, granted_at, source) KEY(player_uuid, holy_enchant) VALUES (?, ?, ?, ?)";

            CompletableFuture<Void> future = sql.update(DATASOURCE, insert,
                uuid.toString(),
                enchant.name(),
                Instant.now().toEpochMilli(),
                source
            ).thenRun(() -> {});

            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    /**
     * Save quiz answer (for analytics).
     */
    public CompletableFuture<Void> saveQuizAnswer(UUID uuid, int questionNumber, String answer) {
        String insert = mysql
            ? "INSERT INTO player_quiz_answers (player_uuid, question_number, answer, answered_at) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE answer = VALUES(answer)"
            : "MERGE INTO player_quiz_answers (player_uuid, question_number, answer, answered_at) KEY(player_uuid, question_number) VALUES (?, ?, ?, ?)";

        return sql.update(DATASOURCE, insert,
            uuid.toString(),
            questionNumber,
            answer,
            Instant.now().toEpochMilli()
        ).thenRun(() -> {});
    }

    /**
     * Map ResultSet to PlayerTraitProfile.
     */
    private Optional<PlayerTraitProfile> mapTraitProfile(UUID uuid, ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();

        PersonalityTrait trait = PersonalityTrait.valueOf(rs.getString("primary_trait"));
        TraitTier tier = TraitTier.valueOf(rs.getString("primary_tier"));
        Element element = Element.valueOf(rs.getString("element"));
        boolean elementRevealed = rs.getBoolean("element_revealed");
        boolean elementActivated = rs.getBoolean("element_activated");
        boolean isPostGame = rs.getBoolean("is_postgame");
        Instant assignedAt = Instant.ofEpochMilli(rs.getLong("assigned_at"));
        long lastAdvance = rs.getLong("last_tier_advance");
        Instant lastTierAdvance = rs.wasNull() ? null : Instant.ofEpochMilli(lastAdvance);

        // Load temple completions
        Map<Element, Boolean> temples = loadTempleCompletions(uuid).join();

        // Load granted Holy Enchants
        Set<HolyEnchant> enchants = loadGrantedEnchants(uuid).join();

        // Load Christmas claim info
        Instant lastChristmas = loadLastChristmasClaim(uuid).join();

        return Optional.of(new PlayerTraitProfile(
            uuid,
            trait,
            tier,
            element,
            elementRevealed,
            elementActivated,
            temples,
            isPostGame,
            enchants,
            lastChristmas,
            assignedAt,
            lastTierAdvance
        ));
    }

    /**
     * Load temple completion map.
     */
    private CompletableFuture<Map<Element, Boolean>> loadTempleCompletions(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT element FROM player_temples WHERE player_uuid = ?",
            rs -> {
                Map<Element, Boolean> map = new HashMap<>();
                // Initialize all to false
                for (Element e : Element.values()) {
                    map.put(e, false);
                }
                // Mark completed ones as true
                while (rs.next()) {
                    Element completed = Element.valueOf(rs.getString("element"));
                    map.put(completed, true);
                }
                return map;
            },
            uuid.toString()
        );
    }

    /**
     * Load granted Holy Enchants set.
     */
    private CompletableFuture<Set<HolyEnchant>> loadGrantedEnchants(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT holy_enchant FROM player_holy_enchants WHERE player_uuid = ?",
            rs -> {
                Set<HolyEnchant> set = new HashSet<>();
                while (rs.next()) {
                    set.add(HolyEnchant.valueOf(rs.getString("holy_enchant")));
                }
                return set;
            },
            uuid.toString()
        );
    }

    /**
     * Load last Christmas claim timestamp.
     */
    private CompletableFuture<Instant> loadLastChristmasClaim(UUID uuid) {
        return sql.query(DATASOURCE,
            "SELECT last_claim_year FROM player_christmas_claims WHERE player_uuid = ?",
            rs -> {
                if (!rs.next()) return null;
                // Convert year to approximate Instant (Jan 1 of that year)
                int year = rs.getInt("last_claim_year");
                return Instant.parse(year + "-01-01T00:00:00Z");
            },
            uuid.toString()
        );
    }
}
