package com.swornhero.steward.module.staffchat.storage;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;

import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class StaffChatStorageService {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Type LIST_TYPE = new TypeToken<List<String>>() { }.getType();
    private static final Path DIRECTORY = Path.of("steward", "data");
    private static final Path FILE = DIRECTORY.resolve("staff-chat-toggles.json");
    private static final Path TEMP_FILE = DIRECTORY.resolve("staff-chat-toggles.json.tmp");

    private StaffChatStorageService() {
    }

    public static Set<UUID> load() {
        if (!Files.exists(FILE)) {
            return Set.of();
        }
        try (Reader reader = Files.newBufferedReader(FILE)) {
            List<String> values = GSON.fromJson(reader, LIST_TYPE);
            Set<UUID> result = new HashSet<>();
            if (values != null) {
                for (String value : values) {
                    try {
                        result.add(UUID.fromString(value));
                    } catch (IllegalArgumentException exception) {
                        Steward.LOGGER.warn("Ignoring invalid staff-chat toggle UUID: {}", value);
                    }
                }
            }
            return Set.copyOf(result);
        } catch (Exception exception) {
            Steward.LOGGER.error("Failed to load staff-chat toggle state.", exception);
            return Set.of();
        }
    }

    public static synchronized boolean save(Collection<UUID> players) {
        List<String> values = players.stream().map(UUID::toString).sorted().toList();
        try {
            Files.createDirectories(DIRECTORY);
            try (Writer writer = Files.newBufferedWriter(TEMP_FILE)) {
                GSON.toJson(values, LIST_TYPE, writer);
            }
            try {
                Files.move(TEMP_FILE, FILE, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(TEMP_FILE, FILE, StandardCopyOption.REPLACE_EXISTING);
            }
            return true;
        } catch (Exception exception) {
            Steward.LOGGER.error("Failed to save staff-chat toggle state.", exception);
            return false;
        }
    }
}
