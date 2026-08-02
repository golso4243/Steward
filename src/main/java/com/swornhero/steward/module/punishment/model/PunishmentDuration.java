package com.swornhero.steward.module.punishment.model;

import java.time.Duration;
import java.time.Instant;

public enum PunishmentDuration {

    ONE_HOUR(
            "1 Hour",
            Duration.ofHours(1)
    ),

    SIX_HOURS(
            "6 Hours",
            Duration.ofHours(6)
    ),

    ONE_DAY(
            "1 Day",
            Duration.ofDays(1)
    ),

    THREE_DAYS(
            "3 Days",
            Duration.ofDays(3)
    ),

    SEVEN_DAYS(
            "7 Days",
            Duration.ofDays(7)
    ),

    FOURTEEN_DAYS(
            "14 Days",
            Duration.ofDays(14)
    ),

    THIRTY_DAYS(
            "30 Days",
            Duration.ofDays(30)
    ),

    PERMANENT(
            "Permanent",
            null
    );

    private final String displayName;
    private final Duration duration;

    PunishmentDuration(
            String displayName,
            Duration duration
    ) {
        this.displayName = displayName;
        this.duration = duration;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isPermanent() {
        return duration == null;
    }

    public Instant calculateExpiration(
            Instant issuedAt
    ) {
        if (issuedAt == null || isPermanent()) {
            return null;
        }

        return issuedAt.plus(duration);
    }
}