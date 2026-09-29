package com.swornhero.steward.module.staffchat.storage;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.swornhero.steward.Steward;
import com.swornhero.steward.module.staffchat.model.StaffChatMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

/**
 * Staff Chat history as JSON Lines: one message per line, appended as
 * messages are sent, so the file never has to be rewritten.
 */
public final class StaffChatHistoryStorage {

    private static final Gson GSON = new Gson();
    private static final Path DIRECTORY = Path.of("steward", "history");
    private static final Path FILE = DIRECTORY.resolve("staff-chat.jsonl");

    private StaffChatHistoryStorage() {
    }

    /**
     * Loads the most recent messages, oldest first. Malformed lines are
     * skipped so one damaged line cannot hide the rest of the history.
     */
    public static List<StaffChatMessage> loadRecent(int limit) {
        if (!Files.exists(FILE)) {
            return List.of();
        }

        Deque<StaffChatMessage> recent = new ArrayDeque<>();
        int skipped = 0;

        try (BufferedReader reader = Files.newBufferedReader(FILE, StandardCharsets.UTF_8)) {
            String line;

            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }

                try {
                    StaffChatMessage message = GSON.fromJson(line, StaffChatMessage.class);

                    if (message == null || message.sentAt() == null || message.text() == null) {
                        skipped++;
                        continue;
                    }

                    recent.addLast(message);

                    if (recent.size() > limit) {
                        recent.removeFirst();
                    }
                } catch (JsonParseException exception) {
                    skipped++;
                }
            }
        } catch (IOException exception) {
            Steward.LOGGER.error("Failed to load staff-chat history.", exception);
        }

        if (skipped > 0) {
            Steward.LOGGER.warn("Skipped {} malformed staff-chat history lines.", skipped);
        }

        return List.copyOf(recent);
    }

    public static synchronized boolean append(StaffChatMessage message) {
        try {
            Files.createDirectories(DIRECTORY);
            Files.writeString(
                    FILE,
                    GSON.toJson(message) + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
            return true;
        } catch (IOException exception) {
            Steward.LOGGER.error("Failed to append staff-chat history.", exception);
            return false;
        }
    }
}
