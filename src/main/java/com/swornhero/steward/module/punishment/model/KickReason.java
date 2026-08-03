package com.swornhero.steward.module.punishment.model;

public enum KickReason {

    DISRUPTIVE_BEHAVIOR(
            19,
            "Disruptive Behavior"
    ),

    FAILURE_TO_FOLLOW_STAFF_DIRECTION(
            20,
            "Failure to Follow Staff Direction"
    ),

    SPAM_OR_CHAT_ABUSE(
            21,
            "Spam or Chat Abuse"
    ),

    INAPPROPRIATE_CONDUCT(
            22,
            "Inappropriate Conduct"
    ),

    AFK_OR_INACTIVE(
            23,
            "AFK or Inactive"
    ),

    SERVER_RULE_VIOLATION(
            24,
            "Server Rule Violation"
    ),

    TECHNICAL_OR_CONNECTION_ISSUE(
            25,
            "Technical or Connection Issue"
    ),

    OTHER(
            31,
            "Other"
    );

    private final int slot;
    private final String displayName;

    KickReason(
            int slot,
            String displayName
    ) {
        this.slot = slot;
        this.displayName = displayName;
    }

    public int slot() {
        return slot;
    }

    public String displayName() {
        return displayName;
    }

    public static KickReason fromSlot(
            int slot
    ) {
        for (KickReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}