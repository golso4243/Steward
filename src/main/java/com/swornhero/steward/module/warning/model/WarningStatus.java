package com.swornhero.steward.module.warning.model;

public enum WarningStatus {

    ACTIVE(
            "Active"
    ),

    EXPIRED(
            "Expired"
    ),

    REVOKED(
            "Revoked"
    ),

    ESCALATED(
            "Escalated"
    );

    private final String displayName;

    WarningStatus(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean contributesActivePoints() {
        return this == ACTIVE;
    }
}