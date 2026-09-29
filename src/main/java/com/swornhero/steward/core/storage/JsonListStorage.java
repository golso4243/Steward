package com.swornhero.steward.core.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.swornhero.steward.Steward;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Atomic JSON persistence for a list of entries: data is written to a
 * temporary file and moved over the real file so a failed write never
 * truncates existing records.
 */
public final class JsonListStorage<T> {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private final Path directory;
    private final Path file;
    private final Path tempFile;
    private final Type listType;
    private final String description;

    public JsonListStorage(
            Path directory,
            String fileName,
            Type listType,
            String description
    ) {
        this.directory = directory;
        this.file = directory.resolve(fileName);
        this.tempFile = directory.resolve(fileName + ".tmp");
        this.listType = listType;
        this.description = description;
    }

    public List<T> load() {
        if (!Files.exists(file)) {
            Steward.LOGGER.info("No {} file found.", description);
            return List.of();
        }

        try (Reader reader = Files.newBufferedReader(file)) {
            List<T> entries = GSON.fromJson(reader, listType);
            return entries != null ? entries : List.of();
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error("Failed to load {}.", description, exception);
            return List.of();
        }
    }

    public synchronized boolean save(List<T> entries) {
        try {
            Files.createDirectories(directory);

            try (Writer writer = Files.newBufferedWriter(tempFile)) {
                GSON.toJson(entries, listType, writer);
            }

            try {
                Files.move(
                        tempFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE
                );
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        tempFile,
                        file,
                        StandardCopyOption.REPLACE_EXISTING
                );
            }

            return true;
        } catch (IOException exception) {
            Steward.LOGGER.error("Failed to save {}.", description, exception);
            return false;
        }
    }
}
