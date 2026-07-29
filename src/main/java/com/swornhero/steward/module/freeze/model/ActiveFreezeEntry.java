package com.swornhero.steward.module.freeze.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ActiveFreezeEntry(
        UUID freezeId,
        UUID targetUuid,
        String targetName,
        UUID frozenByUuid,
        String frozenByName,
        String reason,
        FreezePositionEntry originalPosition,
        FreezePositionEntry currentPosition,
        Instant frozenAt,
        int disconnectCount,
        int reconnectCount,
        List<FreezeRelocationEntry> relocations
) {

    public static ActiveFreezeEntry fromRecord(
            FreezeRecord record
    ) {
        List<FreezeRelocationEntry> relocationEntries =
                record.relocations()
                        .stream()
                        .map(
                                FreezeRelocationEntry::fromRelocation
                        )
                        .toList();

        return new ActiveFreezeEntry(
                record.freezeId(),
                record.targetUuid(),
                record.targetName(),
                record.frozenByUuid(),
                record.frozenByName(),
                record.reason(),
                FreezePositionEntry.fromPosition(
                        record.position()
                ),
                FreezePositionEntry.fromPosition(
                        record.currentPosition()
                ),
                record.frozenAt(),
                record.disconnectCount(),
                record.reconnectCount(),
                relocationEntries
        );
    }

    public FreezeRecord toRecord() {
        validate();

        FreezePosition restoredOriginalPosition =
                originalPosition.toPosition();

        FreezeRecord record =
                new FreezeRecord(
                        freezeId,
                        targetUuid,
                        targetName,
                        frozenByUuid,
                        frozenByName,
                        restoredOriginalPosition,
                        reason,
                        frozenAt
                );

        if (currentPosition != null) {
            record.restoreCurrentPosition(
                    currentPosition.toPosition()
            );
        }

        List<FreezeRelocation> restoredRelocations =
                new ArrayList<>();

        if (relocations != null) {
            for (FreezeRelocationEntry relocation
                    : relocations) {

                if (relocation == null) {
                    continue;
                }

                restoredRelocations.add(
                        relocation.toRelocation()
                );
            }
        }

        record.restoreRelocations(
                restoredRelocations
        );

        record.restoreConnectionCounts(
                disconnectCount,
                reconnectCount
        );

        return record;
    }

    private void validate() {
        if (targetUuid == null) {
            throw new IllegalStateException(
                    "Active freeze target UUID is missing."
            );
        }

        if (targetName == null || targetName.isBlank()) {
            throw new IllegalStateException(
                    "Active freeze target name is missing."
            );
        }

        if (frozenAt == null) {
            throw new IllegalStateException(
                    "Active freeze timestamp is missing."
            );
        }

        if (originalPosition == null) {
            throw new IllegalStateException(
                    "Original freeze position is missing."
            );
        }
    }
}