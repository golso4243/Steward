package com.swornhero.steward.module.notes.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.chat.RecordIds;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.notes.model.StaffNoteRecord;
import com.swornhero.steward.module.notes.storage.StaffNoteStorageService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Append-only staff notes. Notes are never edited or deleted; archiving
 * hides a note from default views while keeping it in history.
 */
public final class StaffNoteService {

    public static final String ID_PREFIX = "NTE";

    private static final Map<UUID, StaffNoteRecord> NOTES = new LinkedHashMap<>();

    private StaffNoteService() {
        // Utility class
    }

    public static synchronized void register() {
        NOTES.clear();

        for (StaffNoteRecord record : StaffNoteStorageService.load()) {
            NOTES.put(record.noteId(), record);
        }
    }

    public static synchronized StaffNoteRecord create(
            ServerPlayer author,
            UUID targetUuid,
            String targetName,
            String text
    ) {
        if (!StewardPermissions.require(author, StewardPermissions.NOTES_CREATE)) {
            return null;
        }

        String normalized = text != null ? text.trim() : "";

        if (normalized.isEmpty()
                || normalized.length() > StaffNoteRecord.MAX_TEXT_LENGTH) {

            author.sendSystemMessage(
                    Component.literal(
                            "[Steward] Staff notes must be 1-"
                                    + StaffNoteRecord.MAX_TEXT_LENGTH
                                    + " characters."
                    )
            );

            return null;
        }

        StaffNoteRecord record = StaffNoteRecord.create(
                targetUuid,
                targetName,
                author.getUUID(),
                author.getName().getString(),
                normalized,
                Instant.now()
        );

        NOTES.put(record.noteId(), record);

        if (!save()) {
            NOTES.remove(record.noteId());

            author.sendSystemMessage(
                    Component.literal(
                            "[Steward] The staff note could not be saved; no note was added."
                    )
            );

            return null;
        }

        author.sendSystemMessage(
                Component.literal(
                        "[Steward] Staff note "
                                + formatNoteId(record.noteId())
                                + " added for "
                                + targetName
                                + "."
                ).withStyle(ChatFormatting.GREEN)
        );

        Steward.LOGGER.info(
                "Staff note {} added for {} by {}.",
                formatNoteId(record.noteId()),
                targetName,
                record.authorName()
        );

        return record;
    }

    public static boolean canArchive(ServerPlayer staff, StaffNoteRecord record) {
        if (record == null || record.archived()) {
            return false;
        }

        if (StewardPermissions.has(staff, StewardPermissions.NOTES_MANAGE)) {
            return true;
        }

        return record.authorUuid().equals(staff.getUUID())
                && StewardPermissions.has(staff, StewardPermissions.NOTES_ARCHIVE_OWN);
    }

    public static synchronized boolean archive(
            ServerPlayer staff,
            UUID noteId
    ) {
        StaffNoteRecord record = NOTES.get(noteId);

        if (record == null) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] That staff note could not be found.")
            );

            return false;
        }

        if (record.archived()) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] That staff note is already archived.")
            );

            return false;
        }

        if (!canArchive(staff, record)) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You do not have permission to archive that note."
                    )
            );

            return false;
        }

        StaffNoteRecord archived = record.withArchive(
                staff.getUUID(),
                staff.getName().getString(),
                Instant.now()
        );

        NOTES.put(noteId, archived);

        if (!save()) {
            NOTES.put(noteId, record);

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] The archive could not be saved; no change was made."
                    )
            );

            return false;
        }

        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] Staff note "
                                + formatNoteId(noteId)
                                + " archived."
                ).withStyle(ChatFormatting.YELLOW)
        );

        Steward.LOGGER.info(
                "Staff note {} for {} archived by {}.",
                formatNoteId(noteId),
                record.targetName(),
                staff.getName().getString()
        );

        return true;
    }

    public static synchronized StaffNoteRecord findById(UUID noteId) {
        return noteId != null ? NOTES.get(noteId) : null;
    }

    public static synchronized StaffNoteRecord findByDisplayId(String displayId) {
        String compact = RecordIds.normalize(ID_PREFIX, displayId);

        if (compact == null) {
            return null;
        }

        StaffNoteRecord match = null;

        for (StaffNoteRecord record : NOTES.values()) {
            if (!RecordIds.compact(record.noteId()).equals(compact)) {
                continue;
            }

            if (match != null) {
                Steward.LOGGER.error("Staff note display ID {} is ambiguous.", displayId);
                return null;
            }

            match = record;
        }

        return match;
    }

    /**
     * One player's notes, newest first.
     */
    public static synchronized List<StaffNoteRecord> notesFor(
            UUID targetUuid,
            boolean includeArchived
    ) {
        return NOTES.values().stream()
                .filter(record -> record.targetUuid().equals(targetUuid))
                .filter(record -> includeArchived || !record.archived())
                .sorted(Comparator.comparing(StaffNoteRecord::createdAt).reversed())
                .toList();
    }

    /**
     * Every note, including archived notes, newest first.
     */
    public static synchronized List<StaffNoteRecord> allNotes() {
        return NOTES.values().stream()
                .sorted(Comparator.comparing(StaffNoteRecord::createdAt).reversed())
                .toList();
    }

    public static String formatNoteId(UUID noteId) {
        return RecordIds.format(ID_PREFIX, noteId);
    }

    private static boolean save() {
        return StaffNoteStorageService.save(NOTES.values());
    }
}
