package com.swornhero.steward.module.report.model;

import java.time.Instant;
import java.util.UUID;

public record ReportEntry(
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

    public static ReportEntry fromRecord(ReportRecord record) {
        return new ReportEntry(
                record.reportId(),
                record.reporterUuid(),
                record.reporterName(),
                record.targetUuid(),
                record.targetName(),
                record.reason(),
                record.createdAt(),
                record.status(),
                record.claimedByUuid(),
                record.claimedByName(),
                record.claimedAt(),
                record.closedByUuid(),
                record.closedByName(),
                record.closedAt(),
                record.resolutionNote(),
                record.reporterNotified()
        );
    }

    public ReportRecord toRecord() {
        validate();

        return new ReportRecord(
                reportId,
                reporterUuid,
                reporterName,
                targetUuid,
                targetName,
                reason,
                createdAt,
                status,
                claimedByUuid,
                claimedByName,
                claimedAt,
                closedByUuid,
                closedByName,
                closedAt,
                resolutionNote,
                reporterNotified
        );
    }

    private void validate() {
        if (reportId == null) {
            throw new IllegalStateException("Report ID cannot be null.");
        }

        if (reporterUuid == null || isBlank(reporterName)) {
            throw new IllegalStateException("Report reporter is incomplete.");
        }

        if (targetUuid == null || isBlank(targetName)) {
            throw new IllegalStateException("Report target is incomplete.");
        }

        if (isBlank(reason)) {
            throw new IllegalStateException("Report reason cannot be blank.");
        }

        if (createdAt == null || status == null) {
            throw new IllegalStateException(
                    "Report creation time and status are required."
            );
        }

        if (status == ReportStatus.CLAIMED
                && (claimedByUuid == null || claimedAt == null)) {

            throw new IllegalStateException(
                    "Claimed reports require a claimer and claim time."
            );
        }

        if (!status.isActive()
                && (closedByUuid == null || closedAt == null)) {

            throw new IllegalStateException(
                    "Closed reports require a closer and close time."
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
