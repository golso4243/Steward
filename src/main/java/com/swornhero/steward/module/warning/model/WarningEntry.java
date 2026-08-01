package com.swornhero.steward.module.warning.model;

import java.time.Instant;
import java.util.UUID;

public record WarningEntry(
        UUID warningId,

        UUID targetUuid,
        String targetName,

        WarningLevel level,
        WarningCategory category,
        String reason,

        UUID issuedByUuid,
        String issuedByName,
        Instant issuedAt,

        String staffNotes,
        String evidenceReference,

        boolean targetWasOnline,
        Instant expiresAt,

        WarningStatus status,

        boolean acknowledged,
        Instant acknowledgedAt,

        UUID revokedByUuid,
        String revokedByName,
        Instant revokedAt,
        String revocationReason,

        Instant escalatedAt
) {

    public static WarningEntry fromRecord(
            WarningRecord record
    ) {
        return new WarningEntry(
                record.warningId(),

                record.targetUuid(),
                record.targetName(),

                record.level(),
                record.category(),
                record.reason(),

                record.issuedByUuid(),
                record.issuedByName(),
                record.issuedAt(),

                record.staffNotes(),
                record.evidenceReference(),

                record.targetWasOnline(),
                record.expiresAt(),

                record.status(),

                record.acknowledged(),
                record.acknowledgedAt(),

                record.revokedByUuid(),
                record.revokedByName(),
                record.revokedAt(),
                record.revocationReason(),

                record.escalatedAt()
        );
    }

    public WarningRecord toRecord() {
        validate();

        WarningRecord record =
                new WarningRecord(
                        warningId,

                        targetUuid,
                        targetName,

                        level,
                        category,
                        reason,

                        issuedByUuid,
                        issuedByName,
                        issuedAt,

                        staffNotes,
                        evidenceReference,

                        targetWasOnline,
                        expiresAt,

                        status
                );

        record.restoreAcknowledgment(
                acknowledged,
                acknowledgedAt
        );

        record.restoreRevocation(
                revokedByUuid,
                revokedByName,
                revokedAt,
                revocationReason
        );

        record.restoreEscalation(
                escalatedAt
        );

        record.refreshExpirationStatus(
                Instant.now()
        );

        return record;
    }

    private void validate() {
        if (warningId == null) {
            throw new IllegalStateException(
                    "Warning ID cannot be null."
            );
        }

        if (targetUuid == null) {
            throw new IllegalStateException(
                    "Warning target UUID cannot be null."
            );
        }

        if (targetName == null || targetName.isBlank()) {
            throw new IllegalStateException(
                    "Warning target name cannot be blank."
            );
        }

        if (level == null) {
            throw new IllegalStateException(
                    "Warning level cannot be null."
            );
        }

        if (category == null) {
            throw new IllegalStateException(
                    "Warning category cannot be null."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException(
                    "Warning reason cannot be blank."
            );
        }

        if (issuedByUuid == null) {
            throw new IllegalStateException(
                    "Issuing staff UUID cannot be null."
            );
        }

        if (issuedByName == null || issuedByName.isBlank()) {
            throw new IllegalStateException(
                    "Issuing staff name cannot be blank."
            );
        }

        if (issuedAt == null) {
            throw new IllegalStateException(
                    "Warning issue time cannot be null."
            );
        }

        if (status == null) {
            throw new IllegalStateException(
                    "Warning status cannot be null."
            );
        }

        if (acknowledged && acknowledgedAt == null) {
            throw new IllegalStateException(
                    "Acknowledged warnings require an acknowledgment time."
            );
        }

        if (status == WarningStatus.REVOKED
                && revokedAt == null) {

            throw new IllegalStateException(
                    "Revoked warnings require a revocation time."
            );
        }

        if (status == WarningStatus.ESCALATED
                && escalatedAt == null) {

            throw new IllegalStateException(
                    "Escalated warnings require an escalation time."
            );
        }
    }
}