package com.lostwilderness.rpgcore.calendar;

import com.lostwilderness.rpgcore.infra.db.SqlExecutor;

import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

final class CalendarRepository {

    private static final String DATASOURCE = "player";
    private final SqlExecutor sql;
    private final boolean mysql;

    CalendarRepository(SqlExecutor sql, boolean mysql) {
        this.sql = sql;
        this.mysql = mysql;
    }

    void createTablesIfNotExists() {
        // Avoid reserved words YEAR, MONTH, DAY in H2/MySQL by using calendar_* column names
        String ddlState = """
            CREATE TABLE IF NOT EXISTS calendar_state (
                id INT PRIMARY KEY,
                epoch_day BIGINT NOT NULL,
                mc_day BIGINT NOT NULL,
                calendar_year INT NOT NULL,
                calendar_month INT NOT NULL,
                calendar_day INT NOT NULL,
                season VARCHAR(16) NOT NULL
            )
            """;
        String ddlJoin = """
            CREATE TABLE IF NOT EXISTS calendar_player_join (
                player_uuid VARCHAR(36) PRIMARY KEY,
                join_epoch_day BIGINT NOT NULL
            )
            """;
        sql.update(DATASOURCE, ddlState).join();
        sql.update(DATASOURCE, ddlJoin).join();

        // Migration for old V1 column names if they exist
        if (mysql) {
            try {
                sql.update(DATASOURCE, "ALTER TABLE calendar_state CHANGE COLUMN `year` calendar_year INT NOT NULL").join();
                sql.update(DATASOURCE, "ALTER TABLE calendar_state CHANGE COLUMN `month` calendar_month INT NOT NULL").join();
                sql.update(DATASOURCE, "ALTER TABLE calendar_state CHANGE COLUMN `day` calendar_day INT NOT NULL").join();
            } catch (Exception ignored) {}
        }
    }

    CompletableFuture<CalendarServiceV2.CalendarSnapshot> loadStateOrDefault() {
        return sql.query(DATASOURCE,
            "SELECT epoch_day, mc_day, calendar_year, calendar_month, calendar_day, season FROM calendar_state WHERE id = 1",
            rs -> {
                if (rs.next()) {
                    long epochDay = rs.getLong("epoch_day");
                    long mcDay = rs.getLong("mc_day");
                    int year = rs.getInt("calendar_year");
                    int month = rs.getInt("calendar_month");
                    int day = rs.getInt("calendar_day");
                    String seasonStr = rs.getString("season");
                    CalendarServiceV2.Season season = CalendarServiceV2.Season.valueOf(seasonStr);
                    LocalDate date = LocalDate.of(year, month, day);
                    return new CalendarServiceV2.CalendarSnapshot(date, epochDay, season, mcDay);
                }
                // Default: day 0, 1st Spring, arbitrary starting date 0001-01-01
                LocalDate date = LocalDate.of(1, 1, 1);
                return new CalendarServiceV2.CalendarSnapshot(date, 0L, CalendarServiceV2.Season.SPRING, 0L);
            });
    }

    CompletableFuture<Void> saveState(CalendarServiceV2.CalendarSnapshot snapshot) {
        LocalDate date = snapshot.date();
        long epochDay = snapshot.dayCount();
        long mcDay = snapshot.mcDay();
        int year = date.getYear();
        int month = date.getMonthValue();
        int day = date.getDayOfMonth();
        String season = snapshot.season().name();
        String upsert = mysql
            ? "INSERT INTO calendar_state (id, epoch_day, mc_day, calendar_year, calendar_month, calendar_day, season) VALUES (1,?,?,?,?,?,?) ON DUPLICATE KEY UPDATE epoch_day=VALUES(epoch_day), mc_day=VALUES(mc_day), calendar_year=VALUES(calendar_year), calendar_month=VALUES(calendar_month), calendar_day=VALUES(calendar_day), season=VALUES(season)"
            : "MERGE INTO calendar_state (id, epoch_day, mc_day, calendar_year, calendar_month, calendar_day, season) KEY(id) VALUES (1,?,?,?,?,?,?)";
        return sql.update(DATASOURCE, upsert, epochDay, mcDay, year, month, day, season).thenRun(() -> {});
    }

    CompletableFuture<LocalDate> getPlayerJoinDate(UUID uuid, LocalDate defaultDate) {
        return sql.query(DATASOURCE,
            "SELECT join_epoch_day FROM calendar_player_join WHERE player_uuid = ?",
            (ResultSet rs) -> {
                if (rs.next()) {
                    long epochDay = rs.getLong("join_epoch_day");
                    return LocalDate.ofEpochDay(epochDay);
                }
                return defaultDate;
            },
            uuid.toString());
    }

    CompletableFuture<Void> recordFirstJoinIfAbsent(UUID uuid, LocalDate currentDate) {
        long epochDay = currentDate.toEpochDay();
        String sqlStr = mysql
            ? "INSERT IGNORE INTO calendar_player_join (player_uuid, join_epoch_day) VALUES (?, ?)"
            : "MERGE INTO calendar_player_join (player_uuid, join_epoch_day) KEY(player_uuid) VALUES (?, ?)";
        return sql.update(DATASOURCE, sqlStr, uuid.toString(), epochDay).thenRun(() -> {});
    }
}

