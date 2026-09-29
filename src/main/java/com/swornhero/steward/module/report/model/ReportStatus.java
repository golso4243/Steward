package com.swornhero.steward.module.report.model;

public enum ReportStatus {

    OPEN("Open", true),
    CLAIMED("Claimed", true),
    RESOLVED("Resolved", false),
    DISMISSED("Dismissed", false);

    private final String displayName;
    private final boolean active;

    ReportStatus(String displayName, boolean active) {
        this.displayName = displayName;
        this.active = active;
    }

    public String displayName() {
        return displayName;
    }

    /**
     * Open and claimed reports still need staff attention.
     */
    public boolean isActive() {
        return active;
    }
}
