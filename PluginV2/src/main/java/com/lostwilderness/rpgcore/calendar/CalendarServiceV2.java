package com.lostwilderness.rpgcore.calendar;

import java.time.LocalDate;
import java.util.UUID;

public interface CalendarServiceV2 {

    enum Season {
        SPRING, SUMMER, AUTUMN, WINTER
    }

    record CalendarSnapshot(LocalDate date, long dayCount, Season season, long mcDay) {}

    CalendarSnapshot getCurrentSnapshot();

    LocalDate getPlayerJoinDate(UUID playerUuid);

    void recordFirstJoinIfAbsent(UUID playerUuid);

    void advanceToNextDay();

    void resetCalendar();
}

