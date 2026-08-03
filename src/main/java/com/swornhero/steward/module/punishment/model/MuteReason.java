package com.swornhero.steward.module.punishment.model;

public enum MuteReason {

    SPAM_OR_FLOODING(
            19,
            "Spam or Flooding"
    ),

    HARASSMENT_OR_TOXICITY(
            20,
            "Harassment or Toxicity"
    ),

    INAPPROPRIATE_LANGUAGE(
            21,
            "Inappropriate Language"
    ),

    ADVERTISING(
            22,
            "Advertising"
    ),

    IMPERSONATION(
            23,
            "Impersonation"
    ),

    SHARING_INAPPROPRIATE_CONTENT(
            24,
            "Sharing Inappropriate Content"
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

    MuteReason(
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

    public static MuteReason fromSlot(
            int slot
    ) {
        for (MuteReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}