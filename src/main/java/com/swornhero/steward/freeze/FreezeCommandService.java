package com.swornhero.steward.freeze;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Set;

public final class FreezeCommandService {
    private static final Set<String> ALLOWED_COMMANDS = Set.of(
            "msg",
            "tell",
            "w",
            "reply",
            "r"
    );

    private FreezeCommandService() {
        // Utility class
    }

    public static boolean isAllowed(String rawCommand) {
        String commandRoot = extractCommandRoot(rawCommand);

        return ALLOWED_COMMANDS.contains(commandRoot);
    }

    public static void notifyBlocked(ServerPlayer player) {
        player.sendSystemMessage(
                Component.literal(
                        "You cannot use that command while frozen."
                )
        );
    }

    private static String extractCommandRoot(String rawCommand) {
        if (rawCommand == null) {
            return "";
        }

        String normalized = rawCommand.trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        int firstSpace = normalized.indexOf(' ');

        if (firstSpace >= 0) {
            normalized = normalized.substring(0, firstSpace);
        }

        int namespaceSeparator = normalized.indexOf(':');

        if (namespaceSeparator >= 0) {
            normalized = normalized.substring(
                    namespaceSeparator + 1
            );
        }

        return normalized.toLowerCase();
    }
}