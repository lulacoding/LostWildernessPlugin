package com.lostwilderness.rpgcore.pets;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;
import com.lostwilderness.rpgcore.zodiac.ZodiacSign;
import org.bukkit.entity.EntityType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Async database operations for pet registry.
 * Uses "player" datasource.
 */
public final class PetRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    public PetRepository(SqlExecutor sql, boolean useMysql) {
        this.sql = sql;
        this.mysql = useMysql;
    }

    /**
     * Create pet_registry table if not exists.
     */
    public void createTableIfNotExists() {
        String ddl = """
            CREATE TABLE IF NOT EXISTS pet_registry (
                pet_uuid VARCHAR(36) PRIMARY KEY,
                owner_uuid VARCHAR(36) NOT NULL,
                entity_type VARCHAR(64) NOT NULL,
                custom_name VARCHAR(64),
                tame_day BIGINT NOT NULL,
                tame_mc_date VARCHAR(32) NOT NULL,
                death_day BIGINT,
                death_mc_date VARCHAR(32),
                zodiac_sign VARCHAR(32) NOT NULL,
                personality VARCHAR(64) NOT NULL,
                personality_revealed BOOLEAN DEFAULT FALSE,
                collar_priority VARCHAR(16) DEFAULT 'DEFAULT',
                is_lost BOOLEAN DEFAULT FALSE,
                last_seen_location VARCHAR(128),
                created_at BIGINT NOT NULL,
                updated_at BIGINT NOT NULL
            )
            """;

        sql.update(DATASOURCE, ddl).join();

        // Create indexes for common queries
        String indexOwner = "CREATE INDEX IF NOT EXISTS idx_pet_owner ON pet_registry(owner_uuid)";
        String indexOwnerAlive = "CREATE INDEX IF NOT EXISTS idx_pet_owner_alive ON pet_registry(owner_uuid, death_day)";

        sql.update(DATASOURCE, indexOwner).join();
        sql.update(DATASOURCE, indexOwnerAlive).join();
    }

    /**
     * Register a new pet (insert only, never update).
     */
    public CompletableFuture<Void> registerPet(PetProfile profile) {
        String insert = """
            INSERT INTO pet_registry (
                pet_uuid, owner_uuid, entity_type, custom_name,
                tame_day, tame_mc_date, death_day, death_mc_date,
                zodiac_sign, personality, personality_revealed,
                collar_priority, is_lost, last_seen_location,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        return sql.update(DATASOURCE, insert,
            profile.petUuid().toString(),
            profile.ownerUuid().toString(),
            profile.entityType().name(),
            profile.customName(),
            profile.tameDay(),
            profile.tameMcDate(),
            profile.deathDay(),
            profile.deathMcDate(),
            profile.zodiacSign().name(),
            profile.personality().name(),
            profile.personalityRevealed(),
            profile.collarPriority().name(),
            profile.isLost(),
            profile.lastSeenLocation(),
            profile.createdAt(),
            profile.updatedAt()
        ).thenRun(() -> {});
    }

    /**
     * Record death date for pet.
     */
    public CompletableFuture<Void> recordDeath(UUID petUuid, long deathDay, String deathMcDate) {
        String update = """
            UPDATE pet_registry
            SET death_day = ?, death_mc_date = ?, updated_at = ?
            WHERE pet_uuid = ?
            """;

        return sql.update(DATASOURCE, update,
            deathDay,
            deathMcDate,
            System.currentTimeMillis(),
            petUuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Get all active (living) pets for an owner.
     */
    public CompletableFuture<List<PetProfile>> getActivePets(UUID ownerUuid) {
        String query = """
            SELECT * FROM pet_registry
            WHERE owner_uuid = ? AND death_day IS NULL
            ORDER BY tame_day DESC
            """;

        return sql.query(DATASOURCE, query, this::mapRows, ownerUuid.toString());
    }

    /**
     * Get all dead pets for an owner (graveyard).
     */
    public CompletableFuture<List<PetProfile>> getDeadPets(UUID ownerUuid) {
        String query = """
            SELECT * FROM pet_registry
            WHERE owner_uuid = ? AND death_day IS NOT NULL
            ORDER BY death_day DESC
            """;

        return sql.query(DATASOURCE, query, this::mapRows, ownerUuid.toString());
    }

    /**
     * Get pet profile by UUID.
     */
    public CompletableFuture<Optional<PetProfile>> getPetByUuid(UUID petUuid) {
        String query = "SELECT * FROM pet_registry WHERE pet_uuid = ?";
        return sql.query(DATASOURCE, query, this::mapRow, petUuid.toString());
    }

    /**
     * Count active pets for owner (for 3 pet limit check).
     */
    public CompletableFuture<Integer> countActivePets(UUID ownerUuid) {
        String query = """
            SELECT COUNT(*) as cnt FROM pet_registry
            WHERE owner_uuid = ? AND death_day IS NULL
            """;

        return sql.query(DATASOURCE, query, rs -> {
            if (rs.next()) {
                return rs.getInt("cnt");
            }
            return 0;
        }, ownerUuid.toString());
    }

    /**
     * Find pet by owner and custom name.
     * Phase 2: Command support.
     */
    public CompletableFuture<Optional<PetProfile>> findPetByName(UUID ownerUuid, String customName) {
        String query = """
            SELECT * FROM pet_registry
            WHERE owner_uuid = ? AND custom_name = ? AND death_day IS NULL
            LIMIT 1
            """;

        return sql.query(DATASOURCE, query, this::mapRow, ownerUuid.toString(), customName);
    }

    /**
     * Update pet's custom name.
     * Phase 2: Rename command support.
     */
    public CompletableFuture<Void> updatePetName(UUID petUuid, String newName) {
        String update = """
            UPDATE pet_registry
            SET custom_name = ?, updated_at = ?
            WHERE pet_uuid = ?
            """;

        return sql.update(DATASOURCE, update,
            newName,
            System.currentTimeMillis(),
            petUuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Mark pet as lost (not found during reconciliation).
     * Phase 2: Reconciliation support.
     */
    public CompletableFuture<Void> markAsLost(UUID petUuid, boolean isLost) {
        String update = """
            UPDATE pet_registry
            SET is_lost = ?, updated_at = ?
            WHERE pet_uuid = ?
            """;

        return sql.update(DATASOURCE, update,
            isLost,
            System.currentTimeMillis(),
            petUuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Update pet's last seen location.
     * Phase 2: Reconciliation support.
     */
    public CompletableFuture<Void> updateLastSeenLocation(UUID petUuid, String location) {
        String update = """
            UPDATE pet_registry
            SET last_seen_location = ?, updated_at = ?
            WHERE pet_uuid = ?
            """;

        return sql.update(DATASOURCE, update,
            location,
            System.currentTimeMillis(),
            petUuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Transfer pet ownership to a new player.
     * Phase 6: Golden Bone retaming.
     */
    public CompletableFuture<Void> transferOwnership(UUID petUuid, UUID newOwnerUuid) {
        String update = """
            UPDATE pet_registry
            SET owner_uuid = ?, updated_at = ?
            WHERE pet_uuid = ?
            """;

        return sql.update(DATASOURCE, update,
            newOwnerUuid.toString(),
            System.currentTimeMillis(),
            petUuid.toString()
        ).thenRun(() -> {});
    }

    /**
     * Map single row to PetProfile.
     */
    private Optional<PetProfile> mapRow(ResultSet rs) throws SQLException {
        if (!rs.next()) return Optional.empty();
        return Optional.of(buildProfile(rs));
    }

    /**
     * Map multiple rows to List<PetProfile>.
     */
    private List<PetProfile> mapRows(ResultSet rs) throws SQLException {
        List<PetProfile> profiles = new ArrayList<>();
        while (rs.next()) {
            profiles.add(buildProfile(rs));
        }
        return profiles;
    }

    /**
     * Build PetProfile from current ResultSet row.
     */
    private PetProfile buildProfile(ResultSet rs) throws SQLException {
        UUID petUuid = UUID.fromString(rs.getString("pet_uuid"));
        UUID ownerUuid = UUID.fromString(rs.getString("owner_uuid"));
        EntityType entityType = EntityType.valueOf(rs.getString("entity_type"));
        String customName = rs.getString("custom_name");
        long tameDay = rs.getLong("tame_day");
        String tameMcDate = rs.getString("tame_mc_date");

        long deathDayLong = rs.getLong("death_day");
        Long deathDay = rs.wasNull() ? null : deathDayLong;
        String deathMcDate = rs.getString("death_mc_date");

        ZodiacSign zodiacSign = ZodiacSign.valueOf(rs.getString("zodiac_sign"));
        PetPersonality personality = PetPersonality.valueOf(rs.getString("personality"));
        boolean personalityRevealed = rs.getBoolean("personality_revealed");
        CollarPriority collarPriority = CollarPriority.valueOf(rs.getString("collar_priority"));
        boolean isLost = rs.getBoolean("is_lost");
        String lastSeenLocation = rs.getString("last_seen_location");
        long createdAt = rs.getLong("created_at");
        long updatedAt = rs.getLong("updated_at");

        return new PetProfile(
            petUuid, ownerUuid, entityType, customName,
            tameDay, tameMcDate, deathDay, deathMcDate,
            zodiacSign, personality, personalityRevealed,
            collarPriority, isLost, lastSeenLocation,
            createdAt, updatedAt
        );
    }
}
