package com.swornhero.steward.module.punishment.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.punishment.model.PunishmentEntry;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;

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

public final class PunishmentStorageService {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type ENTRY_LIST_TYPE =
            new TypeToken<List<PunishmentEntry>>() {
            }.getType();

    private static final Path STEWARD_DIRECTORY =
            Path.of("steward");

    private static final Path HISTORY_DIRECTORY =
            STEWARD_DIRECTORY.resolve("history");

    private static final Path PUNISHMENTS_FILE =
            HISTORY_DIRECTORY.resolve(
                    "punishments.json"
            );

    private static final Path TEMP_FILE =
            HISTORY_DIRECTORY.resolve(
                    "punishments.json.tmp"
            );

    private PunishmentStorageService() {
        // Utility class
    }

    public static List<PunishmentRecord> load() {
        if (!Files.exists(PUNISHMENTS_FILE)) {
            Steward.LOGGER.info(
                    "No punishment history file found."
            );

            return List.of();
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             PUNISHMENTS_FILE
                     )) {

            List<PunishmentEntry> entries =
                    GSON.fromJson(
                            reader,
                            ENTRY_LIST_TYPE
                    );

            if (entries == null || entries.isEmpty()) {
                return List.of();
            }

            List<PunishmentRecord> records =
                    new ArrayList<>();

            Set<UUID> loadedPunishmentIds =
                    new HashSet<>();

            for (PunishmentEntry entry : entries) {
                if (entry == null) {
                    Steward.LOGGER.warn(
                            "Skipped null punishment history entry."
                    );

                    continue;
                }

                try {
                    PunishmentRecord record =
                            entry.toRecord();

                    if (!loadedPunishmentIds.add(
                            record.punishmentId()
                    )) {
                        Steward.LOGGER.error(
                                "Skipped duplicate punishment ID {}.",
                                record.punishmentId()
                        );

                        continue;
                    }

                    records.add(record);
                } catch (RuntimeException exception) {
                    Steward.LOGGER.error(
                            "Failed to restore punishment history entry.",
                            exception
                    );
                }
            }

            records.sort(
                    Comparator.comparing(
                            PunishmentRecord::issuedAt
                    )
            );

            Steward.LOGGER.info(
                    "Loaded {} punishment records.",
                    records.size()
            );

            return List.copyOf(records);
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load punishment history.",
                    exception
            );

            return List.of();
        }
    }

    public static synchronized void save(
            Iterable<PunishmentRecord> records
    ) {
        List<PunishmentEntry> entries =
                new ArrayList<>();

        Instant currentTime =
                Instant.now();

        for (PunishmentRecord record : records) {
            if (record == null) {
                continue;
            }

            record.refreshExpirationStatus(
                    currentTime
            );

            entries.add(
                    PunishmentEntry.fromRecord(record)
            );
        }

        entries.sort(
                Comparator.comparing(
                        PunishmentEntry::issuedAt
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
                    "Saved {} punishment records.",
                    entries.size()
            );
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save punishment history.",
                    exception
            );
        }
    }

    private static void replaceDataFile()
            throws IOException {

        try {
            Files.move(
                    TEMP_FILE,
                    PUNISHMENTS_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    TEMP_FILE,
                    PUNISHMENTS_FILE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}