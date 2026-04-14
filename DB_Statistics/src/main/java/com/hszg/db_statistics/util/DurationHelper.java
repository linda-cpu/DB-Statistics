package com.hszg.db_statistics.util;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;

public class DurationHelper {
    public static long calculateDelayInMinutes(Instant planned, Instant real) {
        if (planned == null || real == null) {
            return -9999;
        }

        try {
            return Duration.between(planned, real).toMinutes();

        } catch (DateTimeParseException e) {
            return -9999;
        }
    }
}
