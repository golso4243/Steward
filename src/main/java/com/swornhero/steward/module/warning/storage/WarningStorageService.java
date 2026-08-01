package com.swornhero.steward.module.warning.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.warning.model.WarningEntry;
import com.swornhero.steward.module.warning.model.WarningRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class WarningStorageService {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type ENTRY_LIST_TYPE =
            new TypeToken<List<WarningEntry>>() {
            }.getType();

    private static final Path STEWARD_DIRECTORY =
            Path.of("steward");

    private static final Path HISTORY_DIRECTORY =
            STEWARD_DIRECTORY.resolve("history");

    private static final Path WARNINGS_FILE =
            HISTORY_DIRECTORY.resolve(
                    "warnings.json"
            );

    private static final Path TEMP_FILE =
            HISTORY_DIRECTORY.resolve(
                    "warnings.json.tmp"
            );

    private WarningStorageService() {
        // Utility class
    }

    public static List<WarningRecord> load() {
        if (!Files.exists(WARNINGS_FILE)) {
            Steward.LOGGER.info(
                    "No warning history file found."
            );

            return List.of();
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             WARNINGS_FILE
                     )) {

            List<WarningEntry> entries =
                    GSON.fromJson(
                            reader,
                            ENTRY_LIST_TYPE
                    );

            if (entries == null || entries.isEmpty()) {
                return List.of();
            }

            List<WarningRecord> records =
                    new ArrayList<>();

            Set<UUID> loadedWarningIds =
                    new HashSet<>();

            for (WarningEntry entry : entries) {
                if (entry == null) {
                    Steward.LOGGER.warn(
                            "Skipped null warning history entry."
                    );

                    continue;
                }

                try {
                    WarningRecord record =
                            entry.toRecord();

                    if (!loadedWarningIds.add(
                            record.warningId()
                    )) {
                        Steward.LOGGER.error(
                                "Skipped duplicate warning ID {}.",
                                record.warningId()
                        );

                        continue;
                    }

                    records.add(record);
                } catch (RuntimeException exception) {
                    Steward.LOGGER.error(
                            "Failed to restore warning history entry.",
                            exception
                    );
                }
            }

            records.sort(
                    Comparator.comparing(
                            WarningRecord::issuedAt
                    )
            );

            Steward.LOGGER.info(
                    "Loaded {} warning records.",
                    records.size()
            );

            return List.copyOf(records);
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load warning history.",
                    exception
            );

            return List.of();
        }
    }

    public static synchronized void save(
            Iterable<WarningRecord> records
    ) {
        List<WarningEntry> entries =
                new ArrayList<>();

        Instant currentTime =
                Instant.now();

        for (WarningRecord record : records) {
            if (record == null) {
                continue;
            }

            record.refreshExpirationStatus(
                    currentTime
            );

            entries.add(
                    WarningEntry.fromRecord(record)
            );
        }

        entries.sort(
                Comparator.comparing(
                        WarningEntry::issuedAt
                )
        );

        try {
            Files.createDirectories(
                    HISTORY_DIRECTORY
            );

            try (Writer writer =
                         Files.newBufferedWriter(
                                 TEMP_FILE
                         )) {

                GSON.toJson(
                        entries,
                        ENTRY_LIST_TYPE,
                        writer
                );
            }

            replaceDataFile();

            Steward.LOGGER.debug(
                    "Saved {} warning records.",
                    entries.size()
            );
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save warning history.",
                    exception
            );
        }
    }

    private static void replaceDataFile()
            throws IOException {

        try {
            Files.move(
                    TEMP_FILE,
                    WARNINGS_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    TEMP_FILE,
                    WARNINGS_FILE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}