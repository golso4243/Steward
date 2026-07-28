package com.swornhero.steward.freeze;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class PendingNotificationService {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Type NOTICE_LIST_TYPE =
            new TypeToken<List<PendingUnfreezeNotice>>() {
            }.getType();

    private static final Path DATA_DIRECTORY =
            Path.of("steward")
                    .resolve("data");

    private static final Path NOTIFICATIONS_FILE =
            DATA_DIRECTORY.resolve(
                    "pending-notifications.json"
            );

    private static final Path TEMP_FILE =
            DATA_DIRECTORY.resolve(
                    "pending-notifications.json.tmp"
            );

    private static final List<PendingUnfreezeNotice> NOTICES =
            new ArrayList<>();

    private PendingNotificationService() {
        // Utility class
    }

    public static void register() {
        load();
    }

    public static synchronized void queueUnfreezeNotice(
            UUID targetUuid,
            String targetName,
            UUID staffUuid,
            String staffName
    ) {
        /*
         * Only one pending unfreeze notice should exist for a
         * player. Replace an older notice if one somehow remains.
         */
        NOTICES.removeIf(
                notice ->
                        notice.targetUuid()
                                .equals(targetUuid)
        );

        NOTICES.add(
                new PendingUnfreezeNotice(
                        targetUuid,
                        targetName,
                        staffUuid,
                        staffName,
                        System.currentTimeMillis()
                )
        );

        save();
    }

    public static synchronized boolean deliverPendingNotice(
            ServerPlayer player
    ) {
        PendingUnfreezeNotice notice =
                findNotice(player.getUUID());

        if (notice == null) {
            return false;
        }

        player.sendSystemMessage(
                Component.literal(
                        "[Steward] You were unfrozen by "
                                + notice.staffName()
                                + " while you were offline."
                )
        );

        player.sendSystemMessage(
                Component.literal(
                        "If you have questions about this "
                                + "moderation action, please contact "
                                + notice.staffName()
                                + "."
                )
        );

        NOTICES.remove(notice);
        save();

        Steward.LOGGER.info(
                "Delivered pending unfreeze notice to {}.",
                player.getName().getString()
        );

        return true;
    }

    private static PendingUnfreezeNotice findNotice(
            UUID targetUuid
    ) {
        for (PendingUnfreezeNotice notice : NOTICES) {
            if (notice.targetUuid().equals(targetUuid)) {
                return notice;
            }
        }

        return null;
    }

    private static synchronized void load() {
        NOTICES.clear();

        if (!Files.exists(NOTIFICATIONS_FILE)) {
            Steward.LOGGER.info(
                    "No pending notification file found."
            );

            return;
        }

        try (Reader reader =
                     Files.newBufferedReader(
                             NOTIFICATIONS_FILE
                     )) {

            List<PendingUnfreezeNotice> loaded =
                    GSON.fromJson(
                            reader,
                            NOTICE_LIST_TYPE
                    );

            if (loaded != null) {
                NOTICES.addAll(loaded);
            }

            Steward.LOGGER.info(
                    "Loaded {} pending player notifications.",
                    NOTICES.size()
            );
        } catch (IOException | JsonParseException exception) {
            Steward.LOGGER.error(
                    "Failed to load pending notifications.",
                    exception
            );
        }
    }

    private static synchronized void save() {
        try {
            Files.createDirectories(
                    DATA_DIRECTORY
            );

            try (Writer writer =
                         Files.newBufferedWriter(
                                 TEMP_FILE
                         )) {

                GSON.toJson(
                        NOTICES,
                        NOTICE_LIST_TYPE,
                        writer
                );
            }

            replaceDataFile();
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save pending notifications.",
                    exception
            );
        }
    }

    private static void replaceDataFile()
            throws IOException {

        try {
            Files.move(
                    TEMP_FILE,
                    NOTIFICATIONS_FILE,
                    StandardCopyOption.REPLACE_EXISTING,
                    StandardCopyOption.ATOMIC_MOVE
            );
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(
                    TEMP_FILE,
                    NOTIFICATIONS_FILE,
                    StandardCopyOption.REPLACE_EXISTING
            );
        }
    }
}