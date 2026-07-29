package com.swornhero.steward.module.freeze.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record FreezeHistoryEntry(
        UUID freezeId,
        UUID targetUuid,
        String targetName,
        UUID frozenByUuid,
        String frozenByName,
        UUID unfrozenByUuid,
        String unfrozenByName,
        String reason,
        FreezePositionEntry originalPosition,
        FreezePositionEntry finalPosition,
        Instant frozenAt,
        Instant unfrozenAt,
        int disconnectCount,
        int reconnectCount,
        List<FreezeRelocationEntry> relocations
) {

    public static FreezeHistoryEntry fromRecord(
            FreezeRecord record
    ) {
        List<FreezeRelocationEntry> relocationEntries =
                record.relocations()
                        .stream()
                        .map(
                                FreezeRelocationEntry::fromRelocation
                        )
                        .toList();

        return new FreezeHistoryEntry(
                record.freezeId(),
                record.targetUuid(),
                record.targetName(),
                record.frozenByUuid(),
                record.frozenByName(),
                record.unfrozenByUuid(),
                record.unfrozenByName(),
                record.reason(),
                FreezePositionEntry.fromPosition(
                        record.position()
                ),
                FreezePositionEntry.fromPosition(
                        record.currentPosition()
                ),
                record.frozenAt(),
                record.unfrozenAt(),
                record.disconnectCount(),
                record.reconnectCount(),
                relocationEntries
        );
    }

    public List<FreezeRelocationEntry> relocations() {
        if (relocations == null) {
            return List.of();
        }

        return List.copyOf(relocations);
    }
}