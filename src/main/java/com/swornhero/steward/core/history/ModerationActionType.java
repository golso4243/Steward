package com.swornhero.steward.core.history;

public enum ModerationActionType {

    WARNING(
            "Warning",
            false
    ),

    FREEZE(
            "Freeze",
            false
    ),

    MUTE(
            "Mute",
            true
    ),

    KICK(
            "Kick",
            true
    ),

    TEMPORARY_BAN(
            "Temporary Ban",
            true
    ),

    PERMANENT_BAN(
            "Permanent Ban",
            true
    );

    private final String displayName;
    private final boolean punishment;

    ModerationActionType(
            String displayName,
            boolean punishment
    ) {
        this.displayName = displayName;
        this.punishment = punishment;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isPunishment() {
        return punishment;
    }
}