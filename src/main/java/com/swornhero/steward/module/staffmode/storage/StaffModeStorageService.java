package com.swornhero.steward.module.staffmode.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.staffmode.model.StaffModeSnapshotEntry;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.nio.file.Path;

public final class StaffModeStorageService {

    private static final Path STEWARD_DIRECTORY =
            Path.of("steward");

    private static final Path STAFF_MODE_DIRECTORY =
            STEWARD_DIRECTORY.resolve(
                    "staff-mode"
            );

    private static final Path SNAPSHOT_FILE =
            STAFF_MODE_DIRECTORY.resolve(
                    "snapshots.json"
            );

    private static final Path TEMP_FILE =
            STAFF_MODE_DIRECTORY.resolve(
                    "snapshots.json.tmp"
            );

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type ENTRY_LIST_TYPE =
            new TypeToken<List<StaffModeSnapshotEntry>>() {
            }.getType();

    private StaffModeStorageService() {
        // Utility class
    }

    public static Path snapshotFile() {
        return SNAPSHOT_FILE;
    }

    public static Path tempFile() {
        return TEMP_FILE;
    }

    public static List<StaffModeSnapshotEntry> load() {
        if (!Files.exists(SNAPSHOT_FILE)) {
            return List.of();
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             SNAPSHOT_FILE
                     )) {

            List<StaffModeSnapshotEntry> entries =
                    GSON.fromJson(
                            reader,
                            ENTRY_LIST_TYPE
                    );

            if (entries == null) {
                return List.of();
            }

            return List.copyOf(entries);
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load Staff Mode recovery snapshots.",
                    exception
            );

            return List.of();
        }
    }

    public static synchronized boolean save(
            List<StaffModeSnapshotEntry> entries
    ) {
        if (entries == null) {
            return false;
        }

        try {
            Files.createDirectories(
                    STAFF_MODE_DIRECTORY
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

            replaceSnapshotFile();

            return true;
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save Staff Mode recovery snapshots.",
                    exception
            );

            return false;
        }
    }

    private static void replaceSnapshotFile()
            throws IOException {

        try {
            Files.move(
                    TEMP_FILE,
                    SNAPSHOT_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    TEMP_FILE,
                    SNAPSHOT_FILE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}