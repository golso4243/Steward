package com.swornhero.steward.freeze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.model.FreezeRecord;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class FreezeHistoryService {
    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type HISTORY_TYPE =
            new TypeToken<List<FreezeHistoryEntry>>() {
            }.getType();

    private static final Path STEWARD_DIRECTORY =
            Path.of("steward");

    private static final Path HISTORY_DIRECTORY =
            STEWARD_DIRECTORY.resolve("history");

    private static final Path HISTORY_FILE =
            HISTORY_DIRECTORY.resolve(
                    "freeze-history.json"
            );

    private static final Path TEMP_FILE =
            HISTORY_DIRECTORY.resolve(
                    "freeze-history.json.tmp"
            );

    private static final List<FreezeHistoryEntry> HISTORY =
            new ArrayList<>();

    private FreezeHistoryService() {
        // Utility class
    }

    public static void register() {
        load();
    }

    public static synchronized void add(
            FreezeRecord record
    ) {
        HISTORY.add(
                FreezeHistoryEntry.fromRecord(record)
        );

        HISTORY.sort(
                Comparator.comparing(
                        FreezeHistoryEntry::frozenAt
                ).reversed()
        );

        save();
    }

    public static synchronized List<FreezeHistoryEntry> getAll() {
        return List.copyOf(HISTORY);
    }

    public static synchronized List<FreezeHistoryEntry> getForPlayer(
            UUID playerUuid
    ) {
        return HISTORY.stream()
                .filter(entry ->
                        entry.targetUuid().equals(playerUuid)
                )
                .toList();
    }

    public static synchronized int countForPlayer(
            UUID playerUuid
    ) {
        return (int) HISTORY.stream()
                .filter(entry ->
                        entry.targetUuid().equals(playerUuid)
                )
                .count();
    }

    private static synchronized void load() {
        HISTORY.clear();

        if (!Files.exists(HISTORY_FILE)) {
            Steward.LOGGER.info(
                    "No existing freeze history file found."
            );

            return;
        }

        try (Reader reader = Files.newBufferedReader(HISTORY_FILE)) {
            List<FreezeHistoryEntry> loaded =
                    GSON.fromJson(
                            reader,
                            HISTORY_TYPE
                    );

            if (loaded != null) {
                HISTORY.addAll(loaded);
            }

            HISTORY.sort(
                    Comparator.comparing(
                            FreezeHistoryEntry::frozenAt
                    ).reversed()
            );

            Steward.LOGGER.info(
                    "Loaded {} freeze history records.",
                    HISTORY.size()
            );
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load freeze history.",
                    exception
            );
        }
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(HISTORY_DIRECTORY);

            try (Writer writer = Files.newBufferedWriter(TEMP_FILE)) {
                GSON.toJson(
                        HISTORY,
                        HISTORY_TYPE,
                        writer
                );
            }

            Files.move(
                    TEMP_FILE,
                    HISTORY_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save freeze history.",
                    exception
            );
        }
    }
}