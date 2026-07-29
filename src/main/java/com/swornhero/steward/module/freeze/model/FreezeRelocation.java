package com.swornhero.steward.module.freeze.model;

import java.time.Instant;
import java.util.UUID;

public record FreezeRelocation(
        FreezePosition fromPosition,
        FreezePosition toPosition,
        UUID relocatedByUuid,
        String relocatedByName,
        Instant relocatedAt,
        String note
) {
}