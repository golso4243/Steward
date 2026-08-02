package com.swornhero.steward.core.history;

import java.time.Instant;
import java.util.UUID;

public record ModerationHistoryItem(
        ModerationActionType type,
        UUID recordId,
        UUID targetUuid,
        String targetName,
        String summary,
        Instant occurredAt
) {

    public ModerationHistoryItem {
        if (type == null) {
            throw new IllegalArgumentException(
                    "Moderation action type cannot be null."
            );
        }

        if (recordId == null) {
            throw new IllegalArgumentException(
                    "Moderation record ID cannot be null."
            );
        }

        if (targetUuid == null) {
            throw new IllegalArgumentException(
                    "Moderation target UUID cannot be null."
            );
        }

        if (targetName == null
                || targetName.isBlank()) {

            throw new IllegalArgumentException(
                    "Moderation target name cannot be blank."
            );
        }

        if (summary == null
                || summary.isBlank()) {

            throw new IllegalArgumentException(
                    "Moderation summary cannot be blank."
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "Moderation timestamp cannot be null."
            );
        }

        targetName = targetName.trim();
        summary = summary.trim();
    }
}