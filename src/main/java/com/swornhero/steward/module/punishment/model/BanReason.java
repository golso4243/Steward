package com.swornhero.steward.module.punishment.model;

public enum BanReason {

    REPEATED_RULE_VIOLATIONS(
            19,
            "Repeated Rule Violations"
    ),

    HARASSMENT_OR_ABUSE(
            20,
            "Harassment or Abuse"
    ),

    GRIEFING_OR_THEFT(
            21,
            "Griefing or Theft"
    ),

    CHEATING_OR_EXPLOITATION(
            22,
            "Cheating or Exploitation"
    ),

    THREATS_OR_SEVERE_MISCONDUCT(
            23,
            "Threats or Severe Misconduct"
    ),

    EVADING_MODERATION(
            24,
            "Evading Moderation"
    ),

    FAILURE_TO_FOLLOW_STAFF_DIRECTION(
            25,
            "Failure to Follow Staff Direction"
    ),

    OTHER(
            31,
            "Other"
    );

    private final int slot;
    private final String displayName;

    BanReason(
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

    public static BanReason fromSlot(
            int slot
    ) {
        for (BanReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}