package com.swornhero.steward.module.punishment.model;

import java.time.Instant;
import java.util.UUID;

public record PunishmentEntry(
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

    public static PunishmentEntry fromRecord(
            PunishmentRecord record
    ) {
        return new PunishmentEntry(
                record.punishmentId(),

                record.type(),

                record.targetUuid(),
                record.targetName(),

                record.issuedByUuid(),
                record.issuedByName(),

                record.reason(),
                record.staffNotes(),
                record.evidenceReference(),

                record.issuedAt(),
                record.expiresAt(),

                record.status(),

                record.revokedAt(),
                record.revokedByUuid(),
                record.revokedByName(),
                record.revocationReason(),

                record.targetWasOnline()
        );
    }

    public PunishmentRecord toRecord() {
        validate();

        PunishmentRecord record =
                new PunishmentRecord(
                        punishmentId,
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
                        status,
                        revokedAt,
                        revokedByUuid,
                        revokedByName,
                        revocationReason,
                        targetWasOnline
                );

        record.refreshExpirationStatus(
                Instant.now()
        );

        return record;
    }

    private void validate() {
        if (punishmentId == null) {
            throw new IllegalStateException(
                    "Punishment ID cannot be null."
            );
        }

        if (type == null) {
            throw new IllegalStateException(
                    "Punishment type cannot be null."
            );
        }

        if (targetUuid == null) {
            throw new IllegalStateException(
                    "Punishment target UUID cannot be null."
            );
        }

        if (targetName == null
                || targetName.isBlank()) {

            throw new IllegalStateException(
                    "Punishment target name cannot be blank."
            );
        }

        if (issuedByUuid == null) {
            throw new IllegalStateException(
                    "Issuing staff UUID cannot be null."
            );
        }

        if (issuedByName == null
                || issuedByName.isBlank()) {

            throw new IllegalStateException(
                    "Issuing staff name cannot be blank."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalStateException(
                    "Punishment reason cannot be blank."
            );
        }

        if (issuedAt == null) {
            throw new IllegalStateException(
                    "Punishment issue time cannot be null."
            );
        }

        if (status == null) {
            throw new IllegalStateException(
                    "Punishment status cannot be null."
            );
        }

        if (type == PunishmentType.KICK
                && status != PunishmentStatus.COMPLETED) {

            throw new IllegalStateException(
                    "Kick punishments must be completed."
            );
        }

        if (type == PunishmentType.TEMPORARY_BAN
                && expiresAt == null) {

            throw new IllegalStateException(
                    "Temporary bans require an expiration time."
            );
        }

        if (type == PunishmentType.PERMANENT_BAN
                && expiresAt != null) {

            throw new IllegalStateException(
                    "Permanent bans cannot have an expiration time."
            );
        }

        if (status == PunishmentStatus.REVOKED) {
            if (revokedAt == null) {
                throw new IllegalStateException(
                        "Revoked punishments require a revocation time."
                );
            }

            if (revokedByUuid == null) {
                throw new IllegalStateException(
                        "Revoked punishments require a staff UUID."
                );
            }

            if (revokedByName == null
                    || revokedByName.isBlank()) {

                throw new IllegalStateException(
                        "Revoked punishments require a staff name."
                );
            }

            if (revocationReason == null
                    || revocationReason.isBlank()) {

                throw new IllegalStateException(
                        "Revoked punishments require a reason."
                );
            }
        }
    }
}