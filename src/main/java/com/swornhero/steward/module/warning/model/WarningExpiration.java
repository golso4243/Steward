package com.swornhero.steward.module.warning.model;

import java.time.Duration;
import java.time.Instant;

public enum WarningExpiration {

    DEFAULT(
            "Default Duration",
            null,
            20
    ),

    SEVEN_DAYS(
            "7 Days",
            Duration.ofDays(7),
            21
    ),

    THIRTY_DAYS(
            "30 Days",
            Duration.ofDays(30),
            22
    ),

    NINETY_DAYS(
            "90 Days",
            Duration.ofDays(90),
            23
    ),

    NEVER(
            "No Expiration",
            null,
            24
    );

    private final String displayName;
    private final Duration duration;
    private final int slot;

    WarningExpiration(
            String displayName,
            Duration duration,
            int slot
    ) {
        this.displayName = displayName;
        this.duration = duration;
        this.slot = slot;
    }

    public String displayName() {
        return displayName;
    }

    public int slot() {
        return slot;
    }

    public static WarningExpiration fromSlot(
            int slot
    ) {
        for (WarningExpiration expiration : values()) {
            if (expiration.slot == slot) {
                return expiration;
            }
        }

        return null;
    }

    public Instant calculateExpiration(
            WarningLevel level,
            Instant issuedAt
    ) {
        if (level == null || issuedAt == null) {
            throw new IllegalArgumentException(
                    "Warning level and issue time cannot be null."
            );
        }

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

    public String resolvedDisplayName(
            WarningLevel level
    ) {
        if (this != DEFAULT) {
            return displayName;
        }

        return switch (level) {
            case VERBAL ->
                    "Default Duration (30 Days)";

            case FORMAL ->
                    "Default Duration (90 Days)";

            case FINAL ->
                    "Default Duration (No Expiration)";
        };
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