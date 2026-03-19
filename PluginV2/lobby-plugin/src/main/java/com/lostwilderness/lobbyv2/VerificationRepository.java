package com.lostwilderness.lobbyv2;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VerificationRepository {
    private final DataSource dataSource;

    public VerificationRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        ensureTables();
    }

    private void ensureTables() {
        try (Connection conn = dataSource.getConnection();
                Statement st = conn.createStatement()) {
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS pending_verification (
                        mc_username VARCHAR(64) PRIMARY KEY,
                        created_at BIGINT NOT NULL
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS linked_accounts (
                        discord_id VARCHAR(64) NOT NULL,
                        mc_username VARCHAR(64) PRIMARY KEY
                    )
                    """);
            st.executeUpdate("""
                    CREATE TABLE IF NOT EXISTS onboarding_flags (
                        player_uuid VARCHAR(36) PRIMARY KEY,
                        greeter_intro_seen BOOLEAN NOT NULL,
                        updated_at BIGINT NOT NULL
                    )
                    """);
        } catch (SQLException e) {
            throw new RuntimeException("Could not create verification tables", e);
        }
    }

    public boolean hasSeenGreeterIntro(String playerUuid) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT greeter_intro_seen FROM onboarding_flags WHERE player_uuid = ? LIMIT 1")) {
            ps.setString(1, playerUuid);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean("greeter_intro_seen");
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean markGreeterIntroSeen(String playerUuid) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO onboarding_flags (player_uuid, greeter_intro_seen, updated_at) VALUES (?, ?, ?) " +
                                "ON DUPLICATE KEY UPDATE greeter_intro_seen = VALUES(greeter_intro_seen), updated_at = VALUES(updated_at)")) {
            long now = System.currentTimeMillis();
            ps.setString(1, playerUuid);
            ps.setBoolean(2, true);
            ps.setLong(3, now);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean addPendingVerification(String mcUsername) {
        String normalized = mcUsername.trim();
        if (normalized.isEmpty())
            return false;
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO pending_verification (mc_username, created_at) VALUES (?, ?) ON DUPLICATE KEY UPDATE created_at = ?")) {
            long now = System.currentTimeMillis();
            ps.setString(1, normalized);
            ps.setLong(2, now);
            ps.setLong(3, now);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean hasPendingVerification(String mcUsername) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT 1 FROM pending_verification WHERE LOWER(mc_username) = LOWER(?) LIMIT 1")) {
            ps.setString(1, mcUsername.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean removePendingVerification(String mcUsername) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "DELETE FROM pending_verification WHERE LOWER(mc_username) = LOWER(?)")) {
            ps.setString(1, mcUsername.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean isVerified(String mcUsername) {
        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "SELECT 1 FROM linked_accounts WHERE LOWER(mc_username) = LOWER(?) LIMIT 1")) {
            ps.setString(1, mcUsername.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            return false;
        }
    }

    public void setLinkedAccount(String discordId, String mcUsername) {
        String user = mcUsername.trim();
        if (user.isEmpty() || discordId == null || discordId.isEmpty())
            return;

        removePendingVerification(user);

        try (Connection conn = dataSource.getConnection();
                PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO linked_accounts (discord_id, mc_username) VALUES (?, ?) ON DUPLICATE KEY UPDATE discord_id = ?")) {
            ps.setString(1, discordId);
            ps.setString(2, user);
            ps.setString(3, discordId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
