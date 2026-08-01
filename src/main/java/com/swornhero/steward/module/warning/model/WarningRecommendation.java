package com.swornhero.steward.module.warning.model;

public enum WarningRecommendation {

    NONE(
            "No Escalation Recommended",
            "No active warning escalation is currently suggested."
    ),

    MONITOR(
            "Monitor Behavior",
            "Monitor the player for repeated or related behavior."
    ),

    STAFF_REVIEW(
            "Staff Review",
            "Review the player's recent warning history before further action."
    ),

    CONSIDER_FINAL_WARNING(
            "Consider Final Warning",
            "Consider a final warning or another appropriate moderation response."
    ),

    CONSIDER_PUNISHMENT(
            "Consider Additional Action",
            "Consider a mute, kick, temporary ban, or other appropriate action."
    );

    private final String displayName;
    private final String description;

    WarningRecommendation(
            String displayName,
            String description
    ) {
        this.displayName = displayName;
        this.description = description;
    }

    public String displayName() {
        return displayName;
    }

    public String description() {
        return description;
    }

    public static WarningRecommendation fromPoints(
            int activePoints
    ) {
        if (activePoints <= 0) {
            return NONE;
        }

        if (activePoints == 1) {
            return MONITOR;
        }

        if (activePoints == 2) {
            return STAFF_REVIEW;
        }

        if (activePoints == 3) {
            return CONSIDER_FINAL_WARNING;
        }

        return CONSIDER_PUNISHMENT;
    }
}