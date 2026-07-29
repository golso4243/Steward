package com.swornhero.steward.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.gui.StaffControlScreen;
import com.swornhero.steward.permission.StewardPermissions;
import com.swornhero.steward.status.StewardStatusService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class StewardCommands {

    private StewardCommands() {
        // Utility class
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        registerCommands(dispatcher)
        );
    }

    private static void registerCommands(
            CommandDispatcher<CommandSourceStack> dispatcher
    ) {
        dispatcher.register(
                Commands.literal("steward")
                        .executes(
                                StewardCommands::openStaffMenu
                        )
                        .then(
                                Commands.literal("status")
                                        .requires(
                                                StewardCommands
                                                        ::canViewStatus
                                        )
                                        .executes(
                                                StewardCommands
                                                        ::showStatus
                                        )
                        )
        );

        dispatcher.register(
                Commands.literal("staff")
                        .requires(
                                StewardCommands
                                        ::canUseStaffCommands
                        )
                        .executes(
                                StewardCommands::openStaffMenu
                        )
        );
    }

    private static boolean canUseStaffCommands(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.STAFF_OPEN
        );
    }

    private static boolean canViewStatus(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.STATUS_VIEW
        );
    }

    private static int openStaffMenu(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        CommandSourceStack source =
                context.getSource();

        if (!canUseStaffCommands(source)) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] You do not have permission "
                                    + "to open the staff interface."
                    )
            );

            return 0;
        }

        ServerPlayer player =
                source.getPlayerOrException();

        StaffControlScreen.open(player);

        return 1;
    }

    private static int showStatus(
            CommandContext<CommandSourceStack> context
    ) {
        return StewardStatusService.sendStatus(
                context.getSource()
        );
    }
}