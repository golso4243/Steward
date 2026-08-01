package com.swornhero.steward.module.warning.model;

public enum WarningLevel {

    VERBAL(
            "Verbal Warning",
            0,
            20
    ),

    FORMAL(
            "Formal Warning",
            1,
            22
    ),

    FINAL(
            "Final Warning",
            2,
            24
    );

    private final String displayName;
    private final int points;
    private final int slot;

    WarningLevel(
            String displayName,
            int points,
            int slot
    ) {
        this.displayName = displayName;
        this.points = points;
        this.slot = slot;
    }

    public String displayName() {
        return displayName;
    }

    public int points() {
        return points;
    }

    public int slot() {
        return slot;
    }

    public static WarningLevel fromSlot(int slot) {
        for (WarningLevel level : values()) {
            if (level.slot == slot) {
                return level;
            }
        }

        return null;
    }
}