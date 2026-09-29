package com.swornhero.steward.module.notes.model;

import java.time.Instant;
import java.util.UUID;

public record StaffNoteEntry(
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

    public static StaffNoteEntry fromRecord(StaffNoteRecord record) {
        return new StaffNoteEntry(
                record.noteId(),
                record.targetUuid(),
                record.targetName(),
                record.authorUuid(),
                record.authorName(),
                record.text(),
                record.createdAt(),
                record.archived(),
                record.archivedByUuid(),
                record.archivedByName(),
                record.archivedAt()
        );
    }

    public StaffNoteRecord toRecord() {
        validate();

        return new StaffNoteRecord(
                noteId,
                targetUuid,
                targetName,
                authorUuid,
                authorName,
                text,
                createdAt,
                archived,
                archivedByUuid,
                archivedByName,
                archivedAt
        );
    }

    private void validate() {
        if (noteId == null) {
            throw new IllegalStateException("Staff note ID cannot be null.");
        }

        if (targetUuid == null || isBlank(targetName)) {
            throw new IllegalStateException("Staff note target is incomplete.");
        }

        if (authorUuid == null || isBlank(authorName)) {
            throw new IllegalStateException("Staff note author is incomplete.");
        }

        if (isBlank(text)) {
            throw new IllegalStateException("Staff note text cannot be blank.");
        }

        if (createdAt == null) {
            throw new IllegalStateException("Staff note creation time is required.");
        }

        if (archived && (archivedByUuid == null || archivedAt == null)) {
            throw new IllegalStateException(
                    "Archived staff notes require an archiver and archive time."
            );
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
