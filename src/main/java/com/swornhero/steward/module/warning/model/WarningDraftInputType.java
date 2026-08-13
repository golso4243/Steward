package com.swornhero.steward.module.warning.model;

public enum WarningDraftInputType {
    STAFF_NOTES("staff notes", 500),
    EVIDENCE_REFERENCE("evidence reference", 300);

    private final String displayName;
    private final int maximumLength;

    WarningDraftInputType(
            String displayName,
            int maximumLength
    ) {
        this.displayName = displayName;
        this.maximumLength = maximumLength;
    }

    public String displayName() {
        return displayName;
    }

    public int maximumLength() {
        return maximumLength;
    }
}
