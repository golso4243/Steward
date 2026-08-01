package com.swornhero.steward.module.warning.model;

import java.time.Duration;
import java.time.Instant;

public enum WarningExpiration {

    DEFAULT(
            "Default Duration",
            null
    ),

    SEVEN_DAYS(
            "7 Days",
            Duration.ofDays(7)
    ),

    THIRTY_DAYS(
            "30 Days",
            Duration.ofDays(30)
    ),

    NINETY_DAYS(
            "90 Days",
            Duration.ofDays(90)
    ),

    NEVER(
            "No Expiration",
            null
    );

    private final String displayName;
    private final Duration duration;

    WarningExpiration(
            String displayName,
            Duration duration
    ) {
        this.displayName = displayName;
        this.duration = duration;
    }

    public String displayName() {
        return displayName;
    }

    public Instant calculateExpiration(
            WarningLevel level,
            Instant issuedAt
    ) {
        if (this == DEFAULT) {
            return calculateDefaultExpiration(
                    level,
                    issuedAt
            );
        }

        if (this == NEVER) {
            return null;
        }

        return issuedAt.plus(duration);
    }

    private static Instant calculateDefaultExpiration(
            WarningLevel level,
            Instant issuedAt
    ) {
        return switch (level) {
            case VERBAL ->
                    issuedAt.plus(
                            Duration.ofDays(30)
                    );

            case FORMAL ->
                    issuedAt.plus(
                            Duration.ofDays(90)
                    );

            case FINAL ->
                    null;
        };
    }
}