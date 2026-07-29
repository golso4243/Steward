package com.swornhero.steward.module.freeze.model;

import java.util.UUID;

public record PendingUnfreezeNotice(
        UUID targetUuid,
        String targetName,
        UUID staffUuid,
        String staffName,
        long unfrozenAtEpochMillis
) {
}