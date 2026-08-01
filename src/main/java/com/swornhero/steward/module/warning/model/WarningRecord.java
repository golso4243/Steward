package com.swornhero.steward.module.warning.model;

import java.time.Instant;
import java.util.UUID;

public final class WarningRecord {

    private final UUID warningId;

    private final UUID targetUuid;
    private final String targetName;

    private final WarningLevel level;
    private final WarningCategory category;
    private final String reason;

    private final UUID issuedByUuid;
    private final String issuedByName;
    private final Instant issuedAt;

    private final String staffNotes;
    private final String evidenceReference;

    private final boolean targetWasOnline;
    private final Instant expiresAt;

    private WarningStatus status;

    private boolean acknowledged;
    private Instant acknowledgedAt;

    private UUID revokedByUuid;
    private String revokedByName;
    private Instant revokedAt;
    private String revocationReason;

    private Instant escalatedAt;

    public WarningRecord(
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
            Instant expiresAt
    ) {
        this(
                UUID.randomUUID(),
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
                WarningStatus.ACTIVE
        );
    }

    public WarningRecord(
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
            WarningStatus status
    ) {
        this.warningId =
                warningId != null
                        ? warningId
                        : UUID.randomUUID();

        this.targetUuid = targetUuid;
        this.targetName = targetName;

        this.level = level;
        this.category = category;
        this.reason = reason;

        this.issuedByUuid = issuedByUuid;
        this.issuedByName = issuedByName;
        this.issuedAt = issuedAt;

        this.staffNotes = staffNotes;
        this.evidenceReference = evidenceReference;

        this.targetWasOnline = targetWasOnline;
        this.expiresAt = expiresAt;

        this.status =
                status != null
                        ? status
                        : WarningStatus.ACTIVE;
    }

    public UUID warningId() {
        return warningId;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public String targetName() {
        return targetName;
    }

    public WarningLevel level() {
        return level;
    }

    public WarningCategory category() {
        return category;
    }

    public String reason() {
        return reason;
    }

    public UUID issuedByUuid() {
        return issuedByUuid;
    }

    public String issuedByName() {
        return issuedByName;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public String staffNotes() {
        return staffNotes;
    }

    public String evidenceReference() {
        return evidenceReference;
    }

    public boolean targetWasOnline() {
        return targetWasOnline;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public WarningStatus status() {
        refreshExpirationStatus(
                Instant.now()
        );

        return status;
    }

    public boolean acknowledged() {
        return acknowledged;
    }

    public Instant acknowledgedAt() {
        return acknowledgedAt;
    }

    public UUID revokedByUuid() {
        return revokedByUuid;
    }

    public String revokedByName() {
        return revokedByName;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public String revocationReason() {
        return revocationReason;
    }

    public Instant escalatedAt() {
        return escalatedAt;
    }

    public boolean isActive() {
        refreshExpirationStatus(
                Instant.now()
        );

        return status == WarningStatus.ACTIVE;
    }

    public boolean contributesActivePoints() {
        return isActive()
                && level != null
                && level.points() > 0;
    }

    public int activePoints() {
        if (!contributesActivePoints()) {
            return 0;
        }

        return level.points();
    }

    public boolean shouldExpire(Instant currentTime) {
        return status == WarningStatus.ACTIVE
                && expiresAt != null
                && currentTime != null
                && !currentTime.isBefore(expiresAt);
    }

    public boolean refreshExpirationStatus(
            Instant currentTime
    ) {
        if (!shouldExpire(currentTime)) {
            return false;
        }

        status = WarningStatus.EXPIRED;
        return true;
    }

    public void acknowledge(Instant acknowledgedAt) {
        if (acknowledged) {
            return;
        }

        this.acknowledged = true;
        this.acknowledgedAt =
                acknowledgedAt != null
                        ? acknowledgedAt
                        : Instant.now();
    }

    public void restoreAcknowledgment(
            boolean acknowledged,
            Instant acknowledgedAt
    ) {
        this.acknowledged = acknowledged;

        this.acknowledgedAt =
                acknowledged
                        ? acknowledgedAt
                        : null;
    }

    public boolean revoke(
            UUID staffUuid,
            String staffName,
            Instant revokedAt,
            String reason
    ) {
        refreshExpirationStatus(
                revokedAt != null
                        ? revokedAt
                        : Instant.now()
        );

        if (status == WarningStatus.REVOKED) {
            return false;
        }

        if (status == WarningStatus.ESCALATED) {
            return false;
        }

        status = WarningStatus.REVOKED;

        revokedByUuid = staffUuid;
        revokedByName = staffName;

        this.revokedAt =
                revokedAt != null
                        ? revokedAt
                        : Instant.now();

        revocationReason = reason;

        return true;
    }

    public void restoreRevocation(
            UUID staffUuid,
            String staffName,
            Instant revokedAt,
            String reason
    ) {
        revokedByUuid = staffUuid;
        revokedByName = staffName;
        this.revokedAt = revokedAt;
        revocationReason = reason;

        if (revokedAt != null) {
            status = WarningStatus.REVOKED;
        }
    }

    public boolean escalate(Instant escalatedAt) {
        refreshExpirationStatus(
                escalatedAt != null
                        ? escalatedAt
                        : Instant.now()
        );

        if (status != WarningStatus.ACTIVE) {
            return false;
        }

        status = WarningStatus.ESCALATED;

        this.escalatedAt =
                escalatedAt != null
                        ? escalatedAt
                        : Instant.now();

        return true;
    }

    public void restoreEscalation(
            Instant escalatedAt
    ) {
        this.escalatedAt = escalatedAt;

        if (escalatedAt != null) {
            status = WarningStatus.ESCALATED;
        }
    }
}