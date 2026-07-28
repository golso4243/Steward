package com.swornhero.steward.freeze;

public enum FreezeReason {
    SUSPECTED_CHEATING(
            "Suspected Cheating",
            19
    ),

    GRIEFING_INVESTIGATION(
            "Griefing Investigation",
            20
    ),

    THEFT_INVESTIGATION(
            "Theft Investigation",
            21
    ),

    PLAYER_REPORT(
            "Player Report",
            22
    ),

    INVENTORY_INSPECTION(
            "Inventory Inspection",
            23
    ),

    STAFF_DISCUSSION(
            "Staff Discussion",
            24
    ),

    OTHER(
            "Other",
            25
    );

    private final String displayName;
    private final int slot;

    FreezeReason(
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

    public static FreezeReason fromSlot(int slot) {
        for (FreezeReason reason : values()) {
            if (reason.slot == slot) {
                return reason;
            }
        }

        return null;
    }
}