package com.swornhero.steward.module.punishment.model;

import java.time.Instant;
import java.util.UUID;

public final class PunishmentRecord {

    private final UUID punishmentId;

    private final PunishmentType type;

    private final UUID targetUuid;
    private final String targetName;

    private final UUID issuedByUuid;
    private final String issuedByName;

    private final String reason;
    private final String staffNotes;
    private final String evidenceReference;

    private final Instant issuedAt;
    private final Instant expiresAt;

    private PunishmentStatus status;

    private Instant revokedAt;
    private UUID revokedByUuid;
    private String revokedByName;
    private String revocationReason;

    private final boolean targetWasOnline;

    public PunishmentRecord(
            PunishmentType type,
            UUID targetUuid,
            String targetName,
            UUID issuedByUuid,
            String issuedByName,
            String reason,
            String staffNotes,
            String evidenceReference,
            Instant issuedAt,
            Instant expiresAt,
            boolean targetWasOnline
    ) {
        this(
                UUID.randomUUID(),
                type,
                targetUuid,
                targetName,
                issuedByUuid,
                issuedByName,
                reason,
                staffNotes,
                evidenceReference,
                issuedAt,
                expiresAt,
                initialStatus(type),
                null,
                null,
                null,
                null,
                targetWasOnline
        );
    }

    public PunishmentRecord(
            UUID punishmentId,
            PunishmentType type,
            UUID targetUuid,
            String targetName,
            UUID issuedByUuid,
            String issuedByName,
            String reason,
            String staffNotes,
            String evidenceReference,
            Instant issuedAt,
            Instant expiresAt,
            PunishmentStatus status,
            Instant revokedAt,
            UUID revokedByUuid,
            String revokedByName,
            String revocationReason,
            boolean targetWasOnline
    ) {
        this.punishmentId = punishmentId;
        this.type = type;
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.issuedByUuid = issuedByUuid;
        this.issuedByName = issuedByName;
        this.reason = reason;
        this.staffNotes = staffNotes;
        this.evidenceReference = evidenceReference;
        this.issuedAt = issuedAt;
        this.expiresAt = expiresAt;
        this.status = status;
        this.revokedAt = revokedAt;
        this.revokedByUuid = revokedByUuid;
        this.revokedByName = revokedByName;
        this.revocationReason = revocationReason;
        this.targetWasOnline = targetWasOnline;
    }

    private static PunishmentStatus initialStatus(
            PunishmentType type
    ) {
        if (type == PunishmentType.KICK) {
            return PunishmentStatus.COMPLETED;
        }

        return PunishmentStatus.ACTIVE;
    }

    public boolean refreshExpirationStatus(
            Instant now
    ) {
        if (status != PunishmentStatus.ACTIVE
                || expiresAt == null
                || now == null
                || now.isBefore(expiresAt)) {

            return false;
        }

        status = PunishmentStatus.EXPIRED;
        return true;
    }

    public boolean revoke(
            UUID staffUuid,
            String staffName,
            Instant revokedAt,
            String reason
    ) {
        if (status != PunishmentStatus.ACTIVE) {
            return false;
        }

        status = PunishmentStatus.REVOKED;
        this.revokedAt = revokedAt;
        this.revokedByUuid = staffUuid;
        this.revokedByName = staffName;
        this.revocationReason = reason;

        return true;
    }

    public UUID punishmentId() {
        return punishmentId;
    }

    public PunishmentType type() {
        return type;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public String targetName() {
        return targetName;
    }

    public UUID issuedByUuid() {
        return issuedByUuid;
    }

    public String issuedByName() {
        return issuedByName;
    }

    public String reason() {
        return reason;
    }

    public String staffNotes() {
        return staffNotes;
    }

    public String evidenceReference() {
        return evidenceReference;
    }

    public Instant issuedAt() {
        return issuedAt;
    }

    public Instant expiresAt() {
        return expiresAt;
    }

    public PunishmentStatus status() {
        return status;
    }

    public Instant revokedAt() {
        return revokedAt;
    }

    public UUID revokedByUuid() {
        return revokedByUuid;
    }

    public String revokedByName() {
        return revokedByName;
    }

    public String revocationReason() {
        return revocationReason;
    }

    public boolean targetWasOnline() {
        return targetWasOnline;
    }

    public boolean isActive() {
        return status == PunishmentStatus.ACTIVE;
    }
}