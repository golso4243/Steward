package com.swornhero.steward.module.freeze.service;

import com.swornhero.steward.module.freeze.config.FreezePolicyService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;

public final class FreezeCommandService {

    private FreezeCommandService() {
        // Utility class
    }

    public static boolean isAllowed(
            String rawCommand
    ) {
        String commandRoot =
                extractCommandRoot(rawCommand);

        if (commandRoot.isBlank()) {
            return false;
        }

        List<String> allowedCommands =
                FreezePolicyService.get()
                        .allowedCommands();

        for (String allowedCommand : allowedCommands) {
            if (allowedCommand == null) {
                continue;
            }

            String normalizedAllowedCommand =
                    normalizeConfiguredCommand(
                            allowedCommand
                    );

            if (commandRoot.equals(
                    normalizedAllowedCommand
            )) {
                return true;
            }
        }

        return false;
    }

    public static void notifyBlocked(
            ServerPlayer player
    ) {
        String message =
                FreezePolicyService.get()
                        .blockedCommandMessage();

        if (message == null || message.isBlank()) {
            message =
                    "You cannot use that command while frozen.";
        }

        player.sendSystemMessage(
                Component.literal(message)
        );
    }

    private static String extractCommandRoot(
            String rawCommand
    ) {
        if (rawCommand == null) {
            return "";
        }

        String normalized =
                rawCommand.trim();

        if (normalized.startsWith("/")) {
            normalized =
                    normalized.substring(1);
        }

        int firstSpace =
                normalized.indexOf(' ');

        if (firstSpace >= 0) {
            normalized =
                    normalized.substring(
                            0,
                            firstSpace
                    );
        }

        int namespaceSeparator =
                normalized.indexOf(':');

        if (namespaceSeparator >= 0) {
            normalized =
                    normalized.substring(
                            namespaceSeparator + 1
                    );
        }

        return normalized.toLowerCase(
                Locale.ROOT
        );
    }

    private static String normalizeConfiguredCommand(
            String configuredCommand
    ) {
        String normalized =
                configuredCommand.trim();

        if (normalized.startsWith("/")) {
            normalized =
                    normalized.substring(1);
        }

        int namespaceSeparator =
                normalized.indexOf(':');

        if (namespaceSeparator >= 0) {
            normalized =
                    normalized.substring(
                            namespaceSeparator + 1
                    );
        }

        int firstSpace =
                normalized.indexOf(' ');

        if (firstSpace >= 0) {
            normalized =
                    normalized.substring(
                            0,
                            firstSpace
                    );
        }

        return normalized.toLowerCase(
                Locale.ROOT
        );
    }
}