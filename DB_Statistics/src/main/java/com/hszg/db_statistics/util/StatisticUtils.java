package com.hszg.db_statistics.util;

import com.hszg.db_statistics.client.dto.trips.TripDto;
import com.hszg.db_statistics.client.dto.trips.TripScheduleDto;
import com.hszg.db_statistics.enums.TimeInterval;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.IsoFields;

@Slf4j
public class StatisticUtils {

    public static boolean isDelayed(TripDto trip) {
        TripScheduleDto s = trip.getSchedule();
        if (s == null) return false;
        
        long delay = DurationHelper.calculateDelayInMinutes(s.getArrivalPlan(), s.getArrivalReal());
        return delay >= 6;
    }

    public static String generateIntervalKey(Instant arrivalPlanDate, TimeInterval interval) {
        try {
            LocalDate date = LocalDate.ofInstant(arrivalPlanDate, ZoneId.of("UTC"));

            return switch (interval) {
                case WEEKLY -> {
                    int week = date.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
                    int year = date.get(IsoFields.WEEK_BASED_YEAR);
                    yield String.format("%d-KW%02d", year, week);
                }
                case MONTHLY -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                case YEARLY -> String.valueOf(date.getYear());
                case DAILY -> date.toString();
            };
        } catch (Exception e) {
            log.trace("Could not parse date for interval key: {}", arrivalPlanDate.toString());
            return "UNKNOWN";
        }
    }
}