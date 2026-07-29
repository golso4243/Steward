package com.swornhero.steward.freeze;

import java.time.Instant;
import java.util.UUID;

public record FreezeRelocationEntry(
        FreezePositionEntry fromPosition,
        FreezePositionEntry toPosition,
        UUID relocatedByUuid,
        String relocatedByName,
        Instant relocatedAt,
        String note
) {

    public static FreezeRelocationEntry fromRelocation(
            FreezeRelocation relocation
    ) {
        return new FreezeRelocationEntry(
                FreezePositionEntry.fromPosition(
                        relocation.fromPosition()
                ),
                FreezePositionEntry.fromPosition(
                        relocation.toPosition()
                ),
                relocation.relocatedByUuid(),
                relocation.relocatedByName(),
                relocation.relocatedAt(),
                relocation.note()
        );
    }

    public FreezeRelocation toRelocation() {
        if (fromPosition == null || toPosition == null) {
            throw new IllegalStateException(
                    "Relocation positions are missing."
            );
        }

        if (relocatedAt == null) {
            throw new IllegalStateException(
                    "Relocation timestamp is missing."
            );
        }

        return new FreezeRelocation(
                fromPosition.toPosition(),
                toPosition.toPosition(),
                relocatedByUuid,
                relocatedByName,
                relocatedAt,
                note
        );
    }
}