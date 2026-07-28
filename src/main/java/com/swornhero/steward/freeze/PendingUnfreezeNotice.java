package com.swornhero.steward.freeze;

import java.util.UUID;

public record PendingUnfreezeNotice(
        UUID targetUuid,
        String targetName,
        UUID staffUuid,
        String staffName,
        long unfrozenAtEpochMillis
) {
}