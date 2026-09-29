package com.swornhero.steward.module.report.model;

import java.time.Instant;
import java.util.UUID;

public record ReportRecord(
        UUID reportId,

        UUID reporterUuid,
        String reporterName,

        UUID targetUuid,
        String targetName,

        String reason,
        Instant createdAt,

        ReportStatus status,

        UUID claimedByUuid,
        String claimedByName,
        Instant claimedAt,

        UUID closedByUuid,
        String closedByName,
        Instant closedAt,
        String resolutionNote,

        boolean reporterNotified
) {

    public static final int MAX_REASON_LENGTH = 256;
    public static final int MAX_RESOLUTION_LENGTH = 500;

    public static ReportRecord create(
            UUID reporterUuid,
            String reporterName,
            UUID targetUuid,
            String targetName,
            String reason,
            Instant createdAt
    ) {
        return new ReportRecord(
                UUID.randomUUID(),
                reporterUuid,
                reporterName,
                targetUuid,
                targetName,
                reason,
                createdAt,
                ReportStatus.OPEN,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false
        );
    }

    public boolean isActive() {
        return status.isActive();
    }

    public boolean isClaimedBy(UUID staffUuid) {
        return status == ReportStatus.CLAIMED
                && claimedByUuid != null
                && claimedByUuid.equals(staffUuid);
    }

    public ReportRecord withClaim(
            UUID staffUuid,
            String staffName,
            Instant at
    ) {
        return new ReportRecord(
                reportId, reporterUuid, reporterName,
                targetUuid, targetName, reason, createdAt,
                ReportStatus.CLAIMED,
                staffUuid, staffName, at,
                null, null, null, null,
                reporterNotified
        );
    }

    public ReportRecord withoutClaim() {
        return new ReportRecord(
                reportId, reporterUuid, reporterName,
                targetUuid, targetName, reason, createdAt,
                ReportStatus.OPEN,
                null, null, null,
                null, null, null, null,
                reporterNotified
        );
    }

    public ReportRecord withClosure(
            ReportStatus closedStatus,
            UUID staffUuid,
            String staffName,
            Instant at,
            String note
    ) {
        if (closedStatus.isActive()) {
            throw new IllegalArgumentException(
                    "Closure status must be resolved or dismissed."
            );
        }

        return new ReportRecord(
                reportId, reporterUuid, reporterName,
                targetUuid, targetName, reason, createdAt,
                closedStatus,
                claimedByUuid, claimedByName, claimedAt,
                staffUuid, staffName, at, note,
                false
        );
    }

    public ReportRecord withReporterNotified() {
        return new ReportRecord(
                reportId, reporterUuid, reporterName,
                targetUuid, targetName, reason, createdAt,
                status,
                claimedByUuid, claimedByName, claimedAt,
                closedByUuid, closedByName, closedAt, resolutionNote,
                true
        );
    }
}
