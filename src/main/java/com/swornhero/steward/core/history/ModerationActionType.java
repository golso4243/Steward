package com.swornhero.steward.core.history;

public enum ModerationActionType {

    WARNING(
            "Warning"
    ),

    FREEZE(
            "Freeze"
    );

    private final String displayName;

    ModerationActionType(
            String displayName
    ) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}