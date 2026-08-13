package com.swornhero.steward.module.warning.model;

public enum WarningRevocationReason {

    ISSUED_IN_ERROR(
            19,
            "Issued in Error"
    ),

    INSUFFICIENT_EVIDENCE(
            20,
            "Insufficient Evidence"
    ),

    SUCCESSFUL_APPEAL(
            21,
            "Successful Appeal"
    ),

    STAFF_DISCRETION(
            22,
            "Staff Discretion"
    ),

    DUPLICATE_WARNING(
            23,
            "Duplicate Warning"
    ),

    REPLACED_BY_ANOTHER_ACTION(
            24,
            "Replaced by Another Action"
    ),

    OTHER(
            31,
            "Other"
    );

    private final int slot;
    private final String displayName;

    WarningRevocationReason(
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

    public static WarningRevocationReason fromSlot(
            int slot
    ) {
        for (WarningRevocationReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}