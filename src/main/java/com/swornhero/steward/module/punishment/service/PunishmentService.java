package com.swornhero.steward.module.punishment.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.punishment.model.PunishmentDuration;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentStatus;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import com.swornhero.steward.module.punishment.storage.PunishmentStorageService;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class PunishmentService {

    private static final Map<UUID, PunishmentRecord> PUNISHMENTS =
            new HashMap<>();

    private PunishmentService() {
        // Utility class
    }

    public static void register() {
        restorePunishments();
    }

    private static void restorePunishments() {
        PUNISHMENTS.clear();

        Set<UUID> loadedPunishmentIds =
                new HashSet<>();

        for (PunishmentRecord record
                : PunishmentStorageService.load()) {

            if (record == null) {
                continue;
            }

            if (!loadedPunishmentIds.add(
                    record.punishmentId()
            )) {
                Steward.LOGGER.error(
                        "Skipped duplicate punishment ID {} "
                                + "for player {}.",
                        record.punishmentId(),
                        record.targetName()
                );

                continue;
            }

            PUNISHMENTS.put(
                    record.punishmentId(),
                    record
            );
        }

        boolean changed =
                refreshExpiredPunishments();

        if (changed) {
            save();
        }

        Steward.LOGGER.info(
                "Punishment service restored {} records.",
                PUNISHMENTS.size()
        );
    }

    public static PunishmentRecord createPunishment(
            PunishmentType type,
            UUID targetUuid,
            String targetName,
            UUID issuedByUuid,
            String issuedByName,
            String reason,
            String staffNotes,
            String evidenceReference,
            boolean targetWasOnline,
            PunishmentDuration duration
    ) {
        validateCreateRequest(
                type,
                targetUuid,
                targetName,
                issuedByUuid,
                issuedByName,
                reason,
                duration
        );

        Instant issuedAt =
                Instant.now();

        Instant expiresAt =
                calculateExpiration(
                        type,
                        duration,
                        issuedAt
                );

        PunishmentRecord record =
                new PunishmentRecord(
                        type,
                        targetUuid,
                        targetName.trim(),
                        issuedByUuid,
                        issuedByName.trim(),
                        reason.trim(),
                        normalizeOptionalText(
                                staffNotes
                        ),
                        normalizeOptionalText(
                                evidenceReference
                        ),
                        issuedAt,
                        expiresAt,
                        targetWasOnline
                );

        PUNISHMENTS.put(
                record.punishmentId(),
                record
        );

        save();

        Steward.LOGGER.info(
                "Punishment {} issued to {} by {}. Type: {}.",
                formatPunishmentId(
                        record.punishmentId()
                ),
                record.targetName(),
                record.issuedByName(),
                record.type()
        );

        return record;
    }

    public static PunishmentRecord findById(
            UUID punishmentId
    ) {
        if (punishmentId == null) {
            return null;
        }

        PunishmentRecord record =
                PUNISHMENTS.get(
                        punishmentId
                );

        if (record != null
                && record.refreshExpirationStatus(
                Instant.now()
        )) {
            save();
        }

        return record;
    }

    public static PunishmentRecord findByDisplayId(
            String displayId
    ) {
        if (displayId == null
                || displayId.isBlank()) {

            return null;
        }

        String normalized =
                displayId.trim()
                        .toUpperCase();

        if (normalized.startsWith("PUN-")) {
            normalized =
                    normalized.substring(4);
        }

        if (normalized.length() != 8) {
            return null;
        }

        PunishmentRecord match =
                null;

        for (PunishmentRecord record
                : PUNISHMENTS.values()) {

            String compactId =
                    record.punishmentId()
                            .toString()
                            .replace("-", "")
                            .substring(0, 8)
                            .toUpperCase();

            if (!compactId.equals(normalized)) {
                continue;
            }

            if (match != null) {
                Steward.LOGGER.error(
                        "Punishment display ID PUN-{} is ambiguous.",
                        normalized
                );

                return null;
            }

            match = record;
        }

        return match;
    }

    public static boolean revokePunishment(
            UUID punishmentId,
            UUID staffUuid,
            String staffName,
            String reason
    ) {
        if (punishmentId == null
                || staffUuid == null
                || staffName == null
                || staffName.isBlank()
                || reason == null
                || reason.isBlank()) {

            return false;
        }

        PunishmentRecord record =
                PUNISHMENTS.get(
                        punishmentId
                );

        if (record == null) {
            return false;
        }

        boolean revoked =
                record.revoke(
                        staffUuid,
                        staffName.trim(),
                        Instant.now(),
                        reason.trim()
                );

        if (!revoked) {
            return false;
        }

        save();

        Steward.LOGGER.info(
                "Punishment {} for {} was revoked by {}. Reason: {}",
                formatPunishmentId(
                        punishmentId
                ),
                record.targetName(),
                staffName.trim(),
                reason.trim()
        );

        return true;
    }

    public static List<PunishmentRecord> allPunishments() {
        if (refreshExpiredPunishments()) {
            save();
        }

        return PUNISHMENTS.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                PunishmentRecord::issuedAt
                        ).reversed()
                )
                .toList();
    }

    public static List<PunishmentRecord> punishmentsFor(
            UUID targetUuid
    ) {
        if (targetUuid == null) {
            return List.of();
        }

        if (refreshExpiredPunishments()) {
            save();
        }

        return PUNISHMENTS.values()
                .stream()
                .filter(record ->
                        targetUuid.equals(
                                record.targetUuid()
                        )
                )
                .sorted(
                        Comparator.comparing(
                                PunishmentRecord::issuedAt
                        ).reversed()
                )
                .toList();
    }

    public static List<PunishmentRecord> activePunishments() {
        if (refreshExpiredPunishments()) {
            save();
        }

        return PUNISHMENTS.values()
                .stream()
                .filter(PunishmentRecord::isActive)
                .sorted(
                        Comparator.comparing(
                                PunishmentRecord::issuedAt
                        ).reversed()
                )
                .toList();
    }

    public static List<PunishmentRecord> activePunishmentsFor(
            UUID targetUuid
    ) {
        if (targetUuid == null) {
            return List.of();
        }

        return punishmentsFor(targetUuid)
                .stream()
                .filter(PunishmentRecord::isActive)
                .toList();
    }

    public static List<PunishmentRecord> activePunishmentsFor(
            UUID targetUuid,
            PunishmentType type
    ) {
        if (targetUuid == null || type == null) {
            return List.of();
        }

        return activePunishmentsFor(targetUuid)
                .stream()
                .filter(record ->
                        record.type() == type
                )
                .toList();
    }

    public static int lifetimePunishmentCount(
            UUID targetUuid
    ) {
        return punishmentsFor(targetUuid).size();
    }

    public static int activePunishmentCount(
            UUID targetUuid
    ) {
        return activePunishmentsFor(
                targetUuid
        ).size();
    }

    public static String formatPunishmentId(
            UUID punishmentId
    ) {
        if (punishmentId == null) {
            return "PUN-UNKNOWN";
        }

        return "PUN-"
                + punishmentId.toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }

    private static Instant calculateExpiration(
            PunishmentType type,
            PunishmentDuration duration,
            Instant issuedAt
    ) {
        if (type == PunishmentType.KICK
                || type == PunishmentType.PERMANENT_BAN) {

            return null;
        }

        return duration.calculateExpiration(
                issuedAt
        );
    }

    private static void validateCreateRequest(
            PunishmentType type,
            UUID targetUuid,
            String targetName,
            UUID issuedByUuid,
            String issuedByName,
            String reason,
            PunishmentDuration duration
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Punishment type cannot be null."
            );
        }

        if (targetUuid == null) {
            throw new IllegalArgumentException(
                    "Target UUID cannot be null."
            );
        }

        if (targetName == null
                || targetName.isBlank()) {

            throw new IllegalArgumentException(
                    "Target name cannot be blank."
            );
        }

        if (issuedByUuid == null) {
            throw new IllegalArgumentException(
                    "Issuing staff UUID cannot be null."
            );
        }

        if (issuedByName == null
                || issuedByName.isBlank()) {

            throw new IllegalArgumentException(
                    "Issuing staff name cannot be blank."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Punishment reason cannot be blank."
            );
        }

        if (type.supportsExpiration()) {
            if (duration == null
                    || duration.isPermanent()) {

                throw new IllegalArgumentException(
                        type.displayName()
                                + " requires a temporary duration."
                );
            }
        }

        if (type == PunishmentType.PERMANENT_BAN
                && duration != null
                && !duration.isPermanent()) {

            throw new IllegalArgumentException(
                    "Permanent bans cannot use a temporary duration."
            );
        }
    }

    private static boolean refreshExpiredPunishments() {
        boolean changed =
                false;

        Instant now =
                Instant.now();

        for (PunishmentRecord record
                : PUNISHMENTS.values()) {

            if (record.refreshExpirationStatus(now)) {
                changed = true;
            }
        }

        return changed;
    }

    private static String normalizeOptionalText(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    private static void save() {
        PunishmentStorageService.save(
                PUNISHMENTS.values()
        );
    }
}