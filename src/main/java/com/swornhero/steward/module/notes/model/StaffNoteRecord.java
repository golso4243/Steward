package com.swornhero.steward.module.notes.model;

import java.time.Instant;
import java.util.UUID;

public record StaffNoteRecord(
        UUID noteId,

        UUID targetUuid,
        String targetName,

        UUID authorUuid,
        String authorName,

        String text,
        Instant createdAt,

        boolean archived,
        UUID archivedByUuid,
        String archivedByName,
        Instant archivedAt
) {

    public static final int MAX_TEXT_LENGTH = 500;

    public static StaffNoteRecord create(
            UUID targetUuid,
            String targetName,
            UUID authorUuid,
            String authorName,
            String text,
            Instant createdAt
    ) {
        return new StaffNoteRecord(
                UUID.randomUUID(),
                targetUuid,
                targetName,
                authorUuid,
                authorName,
                text,
                createdAt,
                false,
                null,
                null,
                null
        );
    }

    public StaffNoteRecord withArchive(
            UUID staffUuid,
            String staffName,
            Instant at
    ) {
        return new StaffNoteRecord(
                noteId,
                targetUuid,
                targetName,
                authorUuid,
                authorName,
                text,
                createdAt,
                true,
                staffUuid,
                staffName,
                at
        );
    }
}
