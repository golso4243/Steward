package com.swornhero.steward.module.warning.model;

public enum WarningCategory {

    CHAT_MISCONDUCT(
            "Chat Misconduct"
    ),

    SPAM(
            "Spam"
    ),

    HARASSMENT(
            "Harassment"
    ),

    PLAYER_DISRESPECT(
            "Disrespect Toward Players"
    ),

    STAFF_DISRESPECT(
            "Disrespect Toward Staff"
    ),

    GRIEFING(
            "Griefing"
    ),

    STEALING(
            "Stealing"
    ),

    TRESPASSING(
            "Trespassing or Base Prowling"
    ),

    EXPLOITING(
            "Exploiting"
    ),

    INAPPROPRIATE_CONTENT(
            "Inappropriate Build or Skin"
    ),

    ADVERTISING(
            "Advertising"
    ),

    FAILURE_TO_FOLLOW_STAFF(
            "Failure to Follow Staff Instructions"
    ),

    OTHER(
            "Other"
    );

    private final String displayName;

    WarningCategory(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}