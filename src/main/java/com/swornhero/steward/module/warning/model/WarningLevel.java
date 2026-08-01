package com.swornhero.steward.module.warning.model;

public enum WarningLevel {

    VERBAL(
            "Verbal Warning",
            0
    ),

    FORMAL(
            "Formal Warning",
            1
    ),

    FINAL(
            "Final Warning",
            2
    );

    private final String displayName;
    private final int points;

    WarningLevel(
            String displayName,
            int points
    ) {
        this.displayName = displayName;
        this.points = points;
    }

    public String displayName() {
        return displayName;
    }

    public int points() {
        return points;
    }
}