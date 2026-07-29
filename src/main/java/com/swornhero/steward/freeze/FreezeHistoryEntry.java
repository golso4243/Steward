package com.swornhero.steward.freeze;

import java.time.Instant;
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
        String dimension,
        double x,
        double y,
        double z,
        Instant frozenAt,
        Instant unfrozenAt,
        int disconnectCount,
        int reconnectCount
) {
    public static FreezeHistoryEntry fromRecord(
            FreezeRecord record
    ) {
        return new FreezeHistoryEntry(
                record.freezeId(),
                record.targetUuid(),
                record.targetName(),
                record.frozenByUuid(),
                record.frozenByName(),
                record.unfrozenByUuid(),
                record.unfrozenByName(),
                record.reason(),
                record.position()
                        .dimension()
                        .identifier()
                        .toString(),
                record.position().x(),
                record.position().y(),
                record.position().z(),
                record.frozenAt(),
                record.unfrozenAt(),
                record.disconnectCount(),
                record.reconnectCount()
        );
    }
}