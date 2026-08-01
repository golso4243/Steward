package com.swornhero.steward.module.warning.model;

public enum WarningCategory {

    CHAT_MISCONDUCT(
            "Chat Misconduct",
            10
    ),

    SPAM(
            "Spam",
            11
    ),

    HARASSMENT(
            "Harassment",
            12
    ),

    PLAYER_DISRESPECT(
            "Disrespect Toward Players",
            13
    ),

    STAFF_DISRESPECT(
            "Disrespect Toward Staff",
            14
    ),

    GRIEFING(
            "Griefing",
            15
    ),

    STEALING(
            "Stealing",
            16
    ),

    TRESPASSING(
            "Trespassing or Base Prowling",
            19
    ),

    EXPLOITING(
            "Exploiting",
            20
    ),

    INAPPROPRIATE_CONTENT(
            "Inappropriate Build or Skin",
            21
    ),

    ADVERTISING(
            "Advertising",
            22
    ),

    FAILURE_TO_FOLLOW_STAFF(
            "Failure to Follow Staff Instructions",
            23
    ),

    OTHER(
            "Other",
            24
    );

    private final String displayName;
    private final int slot;

    WarningCategory(
            String displayName,
            int slot
    ) {
        this.displayName = displayName;
        this.slot = slot;
    }

    public String displayName() {
        return displayName;
    }

    public int slot() {
        return slot;
    }

    public static WarningCategory fromSlot(
            int slot
    ) {
        for (WarningCategory category : values()) {
            if (category.slot == slot) {
                return category;
            }
        }

        return null;
    }
}