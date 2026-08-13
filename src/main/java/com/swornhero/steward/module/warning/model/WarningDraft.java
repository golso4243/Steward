package com.swornhero.steward.module.warning.model;

import java.util.UUID;

public record WarningDraft(
        UUID targetUuid,
        int browserPage,
        WarningLevel level,
        WarningCategory category,
        String reason,
        WarningExpiration expiration,
        String staffNotes,
        String evidenceReference
) {

    public WarningDraft {
        if (targetUuid == null) {
            throw new IllegalArgumentException(
                    "Warning target UUID cannot be null."
            );
        }

        if (level == null
                || category == null
                || expiration == null) {

            throw new IllegalArgumentException(
                    "Warning selections cannot be null."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Warning reason cannot be blank."
            );
        }

        reason = reason.trim();
        staffNotes = normalize(staffNotes);
        evidenceReference = normalize(evidenceReference);
    }

    public WarningDraft withStaffNotes(String value) {
        return new WarningDraft(
                targetUuid,
                browserPage,
                level,
                category,
                reason,
                expiration,
                value,
                evidenceReference
        );
    }

    public WarningDraft withEvidenceReference(String value) {
        return new WarningDraft(
                targetUuid,
                browserPage,
                level,
                category,
                reason,
                expiration,
                staffNotes,
                value
        );
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
