package com.swornhero.steward.freeze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.freeze.model.ActiveFreezeEntry;
import com.swornhero.steward.module.freeze.model.FreezeRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ActiveFreezeStorageService {
    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type ENTRY_LIST_TYPE =
            new TypeToken<List<ActiveFreezeEntry>>() {
            }.getType();

    private static final Path STEWARD_DIRECTORY =
            Path.of("steward");

    private static final Path DATA_DIRECTORY =
            STEWARD_DIRECTORY.resolve("data");

    private static final Path ACTIVE_FREEZES_FILE =
            DATA_DIRECTORY.resolve(
                    "active-freezes.json"
            );

    private static final Path TEMP_FILE =
            DATA_DIRECTORY.resolve(
                    "active-freezes.json.tmp"
            );

    private ActiveFreezeStorageService() {
        // Utility class
    }

    public static List<FreezeRecord> load() {
        if (!Files.exists(ACTIVE_FREEZES_FILE)) {
            Steward.LOGGER.info(
                    "No active freeze data file found."
            );

            return List.of();
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             ACTIVE_FREEZES_FILE
                     )) {

            List<ActiveFreezeEntry> entries =
                    GSON.fromJson(
                            reader,
                            ENTRY_LIST_TYPE
                    );

            if (entries == null || entries.isEmpty()) {
                return List.of();
            }

            List<FreezeRecord> records =
                    new ArrayList<>();

            for (ActiveFreezeEntry entry : entries) {
                try {
                    records.add(entry.toRecord());
                } catch (RuntimeException exception) {
                    Steward.LOGGER.error(
                            "Failed to restore active freeze for {}.",
                            entry.targetUuid(),
                            exception
                    );
                }
            }

            Steward.LOGGER.info(
                    "Loaded {} active freeze records.",
                    records.size()
            );

            return List.copyOf(records);
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load active freeze data.",
                    exception
            );

            return List.of();
        }
    }

    public static synchronized void save(
            Iterable<FreezeRecord> records
    ) {
        List<ActiveFreezeEntry> entries =
                new ArrayList<>();

        for (FreezeRecord record : records) {
            if (!record.isActive()) {
                continue;
            }

            entries.add(
                    ActiveFreezeEntry.fromRecord(record)
            );
        }

        entries.sort(
                Comparator.comparing(
                        ActiveFreezeEntry::frozenAt
                )
        );

        try {
            Files.createDirectories(DATA_DIRECTORY);

            try (Writer writer =
                         Files.newBufferedWriter(TEMP_FILE)) {

                GSON.toJson(
                        entries,
                        ENTRY_LIST_TYPE,
                        writer
                );
            }

            replaceDataFile();

            Steward.LOGGER.debug(
                    "Saved {} active freeze records.",
                    entries.size()
            );
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save active freeze data.",
                    exception
            );
        }
    }

    private static void replaceDataFile()
            throws IOException {

        try {
            Files.move(
                    TEMP_FILE,
                    ACTIVE_FREEZES_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    TEMP_FILE,
                    ACTIVE_FREEZES_FILE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}