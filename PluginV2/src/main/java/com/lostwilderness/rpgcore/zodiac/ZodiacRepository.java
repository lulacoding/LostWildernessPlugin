package com.lostwilderness.rpgcore.zodiac;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Repository for zodiac profile persistence.
 * Supports MySQL and H2 databases.
 */
public final class ZodiacRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public ZodiacRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    public void createTableIfNotExists() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS player_zodiac (
                player_uuid VARCHAR(36) PRIMARY KEY,
                month_sign VARCHAR(32) NOT NULL,
                year_sign VARCHAR(32) NOT NULL,
                is_epochian BOOLEAN NOT NULL DEFAULT FALSE,
                second_sign VARCHAR(32),
                second_sign_revealed BOOLEAN NOT NULL DEFAULT FALSE,
                spirit_animal VARCHAR(32) NOT NULL,
                spirit_animal_revealed BOOLEAN NOT NULL DEFAULT FALSE,
                personality VARCHAR(255),
                created_at BIGINT NOT NULL
            )
            """;
        sql.update(DATASOURCE, ddl).join();
    }

    public CompletableFuture<Optional<ZodiacProfile>> findByUuid(UUID playerUuid) {
        return sql.query(DATASOURCE,
            "SELECT * FROM player_zodiac WHERE player_uuid = ?",
            this::mapRow,
            playerUuid.toString());
    }

    public CompletableFuture<Void> save(ZodiacProfile profile) {
        String upsert = mysql
            ? """
                INSERT INTO player_zodiac (player_uuid, month_sign, year_sign, is_epochian, second_sign,
                    second_sign_revealed, spirit_animal, spirit_animal_revealed, personality, created_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE
                    month_sign = VALUES(month_sign),
                    year_sign = VALUES(year_sign),
                    is_epochian = VALUES(is_epochian),
                    second_sign = VALUES(second_sign),
                    second_sign_revealed = VALUES(second_sign_revealed),
                    spirit_animal = VALUES(spirit_animal),
                    spirit_animal_revealed = VALUES(spirit_animal_revealed),
                    personality = VALUES(personality)
                """
            : """
                MERGE INTO player_zodiac (player_uuid, month_sign, year_sign, is_epochian, second_sign,
                    second_sign_revealed, spirit_animal, spirit_animal_revealed, personality, created_at)
                KEY(player_uuid)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        return sql.update(DATASOURCE, upsert,
            profile.playerUuid().toString(),
            profile.monthSign().name(),
            profile.yearSign().name(),
            profile.isEpochian(),
            profile.secondSign() != null ? profile.secondSign().name() : null,
            profile.secondSignRevealed(),
            profile.spiritAnimal().name(),
            profile.spiritAnimalRevealed(),
            profile.personality(),
            System.currentTimeMillis()
        ).thenRun(() -> {});
    }

    public CompletableFuture<Void> updateSecondSignRevealed(UUID playerUuid, boolean revealed) {
        return sql.update(DATASOURCE,
            "UPDATE player_zodiac SET second_sign_revealed = ? WHERE player_uuid = ?",
            revealed,
            playerUuid.toString()
        ).thenRun(() -> {});
    }

    public CompletableFuture<Void> updateSpiritAnimalRevealed(UUID playerUuid, boolean revealed) {
        return sql.update(DATASOURCE,
            "UPDATE player_zodiac SET spirit_animal_revealed = ? WHERE player_uuid = ?",
            revealed,
            playerUuid.toString()
        ).thenRun(() -> {});
    }

    private Optional<ZodiacProfile> mapRow(ResultSet rs) throws SQLException {
        if (!rs.next()) {
            return Optional.empty();
        }

        UUID uuid = UUID.fromString(rs.getString("player_uuid"));
        ZodiacSign monthSign = ZodiacSign.valueOf(rs.getString("month_sign"));
        ZodiacSign yearSign = ZodiacSign.valueOf(rs.getString("year_sign"));
        boolean isEpochian = rs.getBoolean("is_epochian");

        String secondSignStr = rs.getString("second_sign");
        ZodiacSign secondSign = secondSignStr != null ? ZodiacSign.valueOf(secondSignStr) : null;

        boolean secondSignRevealed = rs.getBoolean("second_sign_revealed");
        SpiritAnimal spiritAnimal = SpiritAnimal.valueOf(rs.getString("spirit_animal"));
        boolean spiritAnimalRevealed = rs.getBoolean("spirit_animal_revealed");
        String personality = rs.getString("personality");

        return Optional.of(new ZodiacProfile(
            uuid, monthSign, yearSign, isEpochian,
            secondSign, secondSignRevealed,
            spiritAnimal, spiritAnimalRevealed,
            personality
        ));
    }
}
