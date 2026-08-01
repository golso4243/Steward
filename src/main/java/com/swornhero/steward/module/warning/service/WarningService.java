package com.swornhero.steward.module.warning.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.warning.model.WarningCategory;
import com.swornhero.steward.module.warning.model.WarningExpiration;
import com.swornhero.steward.module.warning.model.WarningLevel;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.storage.WarningStorageService;

import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class WarningService {

    private static final Map<UUID, WarningRecord> WARNINGS =
            new HashMap<>();

    private WarningService() {
        // Utility class
    }

    public static void register() {
        restoreWarnings();
    }

    private static void restoreWarnings() {
        WARNINGS.clear();

        Set<UUID> loadedWarningIds =
                new HashSet<>();

        for (WarningRecord record
                : WarningStorageService.load()) {

            if (record == null) {
                continue;
            }

            if (!loadedWarningIds.add(
                    record.warningId()
            )) {
                Steward.LOGGER.error(
                        "Skipped duplicate warning ID {} "
                                + "for player {}.",
                        record.warningId(),
                        record.targetName()
                );

                continue;
            }

            WARNINGS.put(
                    record.warningId(),
                    record
            );
        }

        boolean changed =
                refreshExpiredWarnings();

        if (changed) {
            save();
        }

        Steward.LOGGER.info(
                "Warning service restored {} records.",
                WARNINGS.size()
        );
    }

    public static WarningRecord issueWarning(
            UUID targetUuid,
            String targetName,
            WarningLevel level,
            WarningCategory category,
            String reason,
            UUID issuedByUuid,
            String issuedByName,
            String staffNotes,
            String evidenceReference,
            boolean targetWasOnline,
            WarningExpiration expiration
    ) {
        validateIssueRequest(
                targetUuid,
                targetName,
                level,
                category,
                reason,
                issuedByUuid,
                issuedByName
        );

        Instant issuedAt =
                Instant.now();

        WarningExpiration selectedExpiration =
                expiration != null
                        ? expiration
                        : WarningExpiration.DEFAULT;

        Instant expiresAt =
                selectedExpiration.calculateExpiration(
                        level,
                        issuedAt
                );

        WarningRecord record =
                new WarningRecord(
                        targetUuid,
                        targetName,
                        level,
                        category,
                        reason.trim(),
                        issuedByUuid,
                        issuedByName,
                        issuedAt,
                        normalizeOptionalText(
                                staffNotes
                        ),
                        normalizeOptionalText(
                                evidenceReference
                        ),
                        targetWasOnline,
                        expiresAt
                );

        WARNINGS.put(
                record.warningId(),
                record
        );

        save();

        Steward.LOGGER.info(
                "Warning {} issued to {} by {}. "
                        + "Level: {}. Category: {}.",
                formatWarningId(
                        record.warningId()
                ),
                record.targetName(),
                record.issuedByName(),
                record.level(),
                record.category()
        );

        return record;
    }

    public static WarningRecord findById(
            UUID warningId
    ) {
        if (warningId == null) {
            return null;
        }

        WarningRecord record =
                WARNINGS.get(warningId);

        if (record != null
                && record.refreshExpirationStatus(
                Instant.now()
        )) {
            save();
        }

        return record;
    }

    public static List<WarningRecord> allWarnings() {
        refreshExpiredWarnings();

        return WARNINGS.values()
                .stream()
                .sorted(
                        Comparator.comparing(
                                WarningRecord::issuedAt
                        ).reversed()
                )
                .toList();
    }

    public static List<WarningRecord> warningsFor(
            UUID targetUuid
    ) {
        if (targetUuid == null) {
            return List.of();
        }

        refreshExpiredWarnings();

        return WARNINGS.values()
                .stream()
                .filter(
                        record ->
                                targetUuid.equals(
                                        record.targetUuid()
                                )
                )
                .sorted(
                        Comparator.comparing(
                                WarningRecord::issuedAt
                        ).reversed()
                )
                .toList();
    }

    public static int lifetimeWarningCount(
            UUID targetUuid
    ) {
        return warningsFor(targetUuid).size();
    }

    public static int activeWarningCount(
            UUID targetUuid
    ) {
        return (int) warningsFor(targetUuid)
                .stream()
                .filter(
                        WarningRecord::isActive
                )
                .count();
    }

    public static int activeWarningPoints(
            UUID targetUuid
    ) {
        return warningsFor(targetUuid)
                .stream()
                .mapToInt(
                        WarningRecord::activePoints
                )
                .sum();
    }

    public static boolean refreshExpiredWarnings() {
        Instant currentTime =
                Instant.now();

        boolean changed =
                false;

        for (WarningRecord record
                : WARNINGS.values()) {

            if (record.refreshExpirationStatus(
                    currentTime
            )) {
                changed = true;
            }
        }

        return changed;
    }

    public static void save() {
        WarningStorageService.save(
                WARNINGS.values()
        );
    }

    public static String formatWarningId(
            UUID warningId
    ) {
        if (warningId == null) {
            return "WRN-UNKNOWN";
        }

        String compactId =
                warningId.toString()
                        .replace("-", "")
                        .substring(0, 8)
                        .toUpperCase();

        return "WRN-" + compactId;
    }

    private static void validateIssueRequest(
            UUID targetUuid,
            String targetName,
            WarningLevel level,
            WarningCategory category,
            String reason,
            UUID issuedByUuid,
            String issuedByName
    ) {
        if (targetUuid == null) {
            throw new IllegalArgumentException(
                    "Warning target UUID cannot be null."
            );
        }

        if (targetName == null
                || targetName.isBlank()) {

            throw new IllegalArgumentException(
                    "Warning target name cannot be blank."
            );
        }

        if (level == null) {
            throw new IllegalArgumentException(
                    "Warning level cannot be null."
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Warning category cannot be null."
            );
        }

        if (reason == null
                || reason.isBlank()) {

            throw new IllegalArgumentException(
                    "Warning reason cannot be blank."
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
    }

    private static String normalizeOptionalText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}