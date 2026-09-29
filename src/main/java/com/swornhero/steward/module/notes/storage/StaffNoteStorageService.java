package com.swornhero.steward.module.notes.storage;

import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.core.storage.JsonListStorage;
import com.swornhero.steward.module.notes.model.StaffNoteEntry;
import com.swornhero.steward.module.notes.model.StaffNoteRecord;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class StaffNoteStorageService {

    private static final JsonListStorage<StaffNoteEntry> STORAGE =
            new JsonListStorage<>(
                    Path.of("steward", "history"),
                    "staff-notes.json",
                    new TypeToken<List<StaffNoteEntry>>() {
                    }.getType(),
                    "staff note history"
            );

    private StaffNoteStorageService() {
        // Utility class
    }

    public static List<StaffNoteRecord> load() {
        List<StaffNoteRecord> records = new ArrayList<>();
        Set<UUID> loadedIds = new HashSet<>();

        for (StaffNoteEntry entry : STORAGE.load()) {
            if (entry == null) {
                Steward.LOGGER.warn("Skipped null staff note entry.");
                continue;
            }

            try {
                StaffNoteRecord record = entry.toRecord();

                if (!loadedIds.add(record.noteId())) {
                    Steward.LOGGER.error(
                            "Skipped duplicate staff note ID {}.",
                            record.noteId()
                    );

                    continue;
                }

                records.add(record);
            } catch (RuntimeException exception) {
                Steward.LOGGER.error(
                        "Failed to restore staff note entry.",
                        exception
                );
            }
        }

        Steward.LOGGER.info("Loaded {} staff notes.", records.size());
        return List.copyOf(records);
    }

    public static boolean save(Collection<StaffNoteRecord> records) {
        return STORAGE.save(
                records.stream()
                        .sorted(Comparator.comparing(StaffNoteRecord::createdAt))
                        .map(StaffNoteEntry::fromRecord)
                        .toList()
        );
    }
}
