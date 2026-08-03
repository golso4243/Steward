package com.swornhero.steward.module.punishment.model;

public enum PunishmentRevocationReason {

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

    REPLACED_BY_ANOTHER_ACTION(
            23,
            "Replaced by Another Action"
    ),

    EARLY_RELEASE(
            24,
            "Early Release"
    ),

    OTHER(
            31,
            "Other"
    );

    private final int slot;
    private final String displayName;

    PunishmentRevocationReason(
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

    public static PunishmentRevocationReason fromSlot(
            int slot
    ) {
        for (PunishmentRevocationReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}