package com.swornhero.steward.module.warning.model;

public enum WarningReason {

    FIRST_OFFENSE(
            "First Offense",
            "First documented offense",
            10
    ),

    REPEATED_OFFENSE(
            "Repeated Offense",
            "Repeated or continuing offense",
            11
    ),

    CONTINUED_AFTER_WARNING(
            "Continued After Warning",
            "Behavior continued after a prior warning",
            12
    ),

    IGNORED_STAFF_DIRECTION(
            "Ignored Staff Direction",
            "Failed to comply with direct staff instruction",
            13
    ),

    CONFIRMED_PLAYER_REPORT(
            "Confirmed Player Report",
            "Confirmed through a player report",
            14
    ),

    DIRECT_STAFF_OBSERVATION(
            "Direct Staff Observation",
            "Observed directly by staff",
            15
    ),

    EVIDENCE_REVIEWED(
            "Evidence Reviewed",
            "Supported by reviewed evidence",
            16
    ),

    OTHER(
            "Other",
            "Other documented reason",
            22
    );

    private final String displayName;
    private final String reasonText;
    private final int slot;

    WarningReason(
            String displayName,
            String reasonText,
            int slot
    ) {
        this.displayName = displayName;
        this.reasonText = reasonText;
        this.slot = slot;
    }

    public String displayName() {
        return displayName;
    }

    public String reasonText() {
        return reasonText;
    }

    public int slot() {
        return slot;
    }

    public static WarningReason fromSlot(
            int slot
    ) {
        for (WarningReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }

    public String createReason(
            WarningCategory category
    ) {
        if (category == null) {
            return reasonText;
        }

        return category.displayName()
                + ": "
                + reasonText;
    }
}