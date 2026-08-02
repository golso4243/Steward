package com.swornhero.steward.module.punishment.model;

public enum PunishmentStatus {

    ACTIVE("Active"),

    EXPIRED("Expired"),

    REVOKED("Revoked"),

    COMPLETED("Completed");

    private final String displayName;

    PunishmentStatus(
            String displayName
    ) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isEnforced() {
        return this == ACTIVE;
    }

    public boolean isFinalState() {
        return this == EXPIRED
                || this == REVOKED
                || this == COMPLETED;
    }
}