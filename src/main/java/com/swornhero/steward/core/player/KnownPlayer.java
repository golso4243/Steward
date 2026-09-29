package com.swornhero.steward.core.player;

import java.time.Instant;
import java.util.UUID;

public record KnownPlayer(
        UUID uuid,
        String name,
        Instant lastSeen
) {
}
