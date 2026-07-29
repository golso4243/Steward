package com.swornhero.steward.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.swornhero.steward.Steward;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FreezePolicyService {

    private static final Gson GSON =
            new GsonBuilder()
                    .setPrettyPrinting()
                    .create();

    private static final Path CONFIG_DIRECTORY =
            FabricLoader.getInstance()
                    .getConfigDir()
                    .resolve("steward");

    private static final Path CONFIG_FILE =
            CONFIG_DIRECTORY.resolve(
                    "freeze-policy.json"
            );

    private static final String DEFAULT_CONFIG = """
            {
              "_comment_disconnectAlerts": "Notify staff when a frozen player disconnects.",
              "disconnectAlerts": true,

              "_comment_reconnectAlerts": "Notify staff when a frozen player reconnects.",
              "reconnectAlerts": true,

              "_comment_showReasonOnReconnect": "Show the frozen player their recorded freeze reason when they reconnect.",
              "showReasonOnReconnect": true,

              "_comment_offlineUnfreezeNotices": "Notify a player on their next login when staff unfroze them while they were offline.",
              "offlineUnfreezeNotices": true,

              "_comment_fallbackStaffAlerts": "Include a detailed staff warning when an unsafe or unavailable saved location requires fallback restoration.",
              "fallbackStaffAlerts": true,

              "_comment_fallbackConsoleWarnings": "Write fallback restoration warnings to the server console.",
              "fallbackConsoleWarnings": true
            
              "_comment_allowedCommands": "Commands frozen players may use. Enter only the root command without a leading slash.",
              "allowedCommands": [
                "msg",
                "tell",
                "w",
                "reply",
                "r",
                "staff",
                "steward"
              ],
            
              "_comment_blockedCommandMessage": "Message shown when a frozen player attempts to use a blocked command.",
              "blockedCommandMessage": "You cannot use that command while frozen."
            }
            """;

    private static FreezePolicy policy =
            new FreezePolicy();

    private FreezePolicyService() {
        // Utility class
    }

    public static void register() {
        load();
    }

    public static FreezePolicy get() {
        return policy;
    }

    public static void reload() {
        load();
    }

    private static void load() {
        try {
            Files.createDirectories(
                    CONFIG_DIRECTORY
            );

            if (!Files.exists(CONFIG_FILE)) {
                policy = new FreezePolicy();
                saveDefaultConfig();

                Steward.LOGGER.info(
                        "Created default freeze policy at {}.",
                        CONFIG_FILE
                );

                return;
            }

            try (Reader reader =
                         Files.newBufferedReader(
                                 CONFIG_FILE
                         )) {

                FreezePolicy loadedPolicy =
                        GSON.fromJson(
                                reader,
                                FreezePolicy.class
                        );

                if (loadedPolicy == null) {
                    throw new JsonParseException(
                            "Freeze policy file was empty."
                    );
                }

                policy = loadedPolicy;
            }

            Steward.LOGGER.info(
                    "Loaded freeze policy from {}.",
                    CONFIG_FILE
            );
        } catch (IOException | JsonParseException exception) {
            policy = new FreezePolicy();

            Steward.LOGGER.error(
                    "Failed to load freeze policy. "
                            + "Using safe default settings.",
                    exception
            );
        }
    }

    private static void saveDefaultConfig() {
        try {
            Files.writeString(
                    CONFIG_FILE,
                    DEFAULT_CONFIG
            );
        } catch (IOException exception) {
            Steward.LOGGER.error(
                    "Failed to save default freeze policy.",
                    exception
            );
        }
    }
}