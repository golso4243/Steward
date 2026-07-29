package com.swornhero.steward.status;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public final class StewardStatusService {

    private static final String STEWARD_MOD_ID =
            "steward";

    private static final String MINECRAFT_MOD_ID =
            "minecraft";

    private static final String FABRIC_LOADER_MOD_ID =
            "fabricloader";

    private static final String WEBSITE =
            "https://www.swornhero.com";

    private StewardStatusService() {
        // Utility class
    }

    public static int sendStatus(
            CommandSourceStack source
    ) {
        String stewardVersion =
                getVersion(
                        STEWARD_MOD_ID
                );

        String minecraftVersion =
                getVersion(
                        MINECRAFT_MOD_ID
                );

        String fabricLoaderVersion =
                getVersion(
                        FABRIC_LOADER_MOD_ID
                );

        String javaVersion =
                System.getProperty(
                        "java.version",
                        "Unknown"
                );

        sendHeader(source);

        sendLabelValue(
                source,
                "Version",
                stewardVersion,
                ChatFormatting.WHITE
        );

        sendLabelValue(
                source,
                "Minecraft",
                minecraftVersion,
                ChatFormatting.WHITE
        );

        sendLabelValue(
                source,
                "Fabric Loader",
                fabricLoaderVersion,
                ChatFormatting.WHITE
        );

        sendLabelValue(
                source,
                "Java",
                javaVersion,
                ChatFormatting.WHITE
        );

        sendBlankLine(source);
        sendModuleHeader(source);

        sendModule(
                source,
                "Staff Interface",
                "Active",
                ChatFormatting.GREEN
        );

        sendModule(
                source,
                "Freeze",
                "Active",
                ChatFormatting.GREEN
        );

        sendModule(
                source,
                "Warnings",
                "Not Implemented",
                ChatFormatting.GRAY
        );

        sendModule(
                source,
                "Mute",
                "Not Implemented",
                ChatFormatting.GRAY
        );

        sendModule(
                source,
                "Reports",
                "Not Implemented",
                ChatFormatting.GRAY
        );

        sendModule(
                source,
                "Punishments",
                "Not Implemented",
                ChatFormatting.GRAY
        );

        sendBlankLine(source);
        sendDeveloperInformation(source);
        sendFooter(source);

        return 1;
    }

    private static String getVersion(
            String modId
    ) {
        return FabricLoader.getInstance()
                .getModContainer(modId)
                .map(container ->
                        container.getMetadata()
                                .getVersion()
                                .getFriendlyString()
                )
                .orElse("Unknown");
    }

    private static void sendHeader(
            CommandSourceStack source
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        "━━━━━━━━ Steward Status ━━━━━━━━"
                ).withStyle(
                        ChatFormatting.GOLD,
                        ChatFormatting.BOLD
                ),
                false
        );
    }

    private static void sendModuleHeader(
            CommandSourceStack source
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        "Modules"
                ).withStyle(
                        ChatFormatting.GOLD,
                        ChatFormatting.BOLD
                ),
                false
        );
    }

    private static void sendModule(
            CommandSourceStack source,
            String module,
            String status,
            ChatFormatting statusColor
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        " • " + module + ": "
                ).withStyle(
                        ChatFormatting.GRAY
                ).append(
                        Component.literal(status)
                                .withStyle(statusColor)
                ),
                false
        );
    }

    private static void sendLabelValue(
            CommandSourceStack source,
            String label,
            String value,
            ChatFormatting valueColor
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        label + ": "
                ).withStyle(
                        ChatFormatting.GRAY
                ).append(
                        Component.literal(value)
                                .withStyle(valueColor)
                ),
                false
        );
    }

    private static void sendBlankLine(
            CommandSourceStack source
    ) {
        source.sendSuccess(
                () -> Component.literal(" "),
                false
        );
    }

    private static void sendDeveloperInformation(
            CommandSourceStack source
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        "Developed by "
                ).withStyle(
                        ChatFormatting.GRAY
                ).append(
                        Component.literal(
                                "SwornHero"
                        ).withStyle(
                                ChatFormatting.AQUA
                        )
                ),
                false
        );

        source.sendSuccess(
                () -> Component.literal(
                        WEBSITE
                ).withStyle(
                        ChatFormatting.BLUE,
                        ChatFormatting.UNDERLINE
                ),
                false
        );
    }

    private static void sendFooter(
            CommandSourceStack source
    ) {
        source.sendSuccess(
                () -> Component.literal(
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
                ).withStyle(
                        ChatFormatting.GOLD
                ),
                false
        );
    }
}