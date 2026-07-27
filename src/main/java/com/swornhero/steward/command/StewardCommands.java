package com.swornhero.steward.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

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
                        .requires(StewardCommands::canUseStaffCommands)
                        .executes(StewardCommands::openStaffMenu)
        );

        dispatcher.register(
                Commands.literal("staff")
                        .requires(StewardCommands::canUseStaffCommands)
                        .executes(StewardCommands::openStaffMenu)
        );
    }

    private static boolean canUseStaffCommands(CommandSourceStack source) {
        return source.permissions().hasPermission(
                net.minecraft.server.permissions.Permissions.COMMANDS_MODERATOR
        );
    }

    private static int openStaffMenu(
            CommandContext<CommandSourceStack> context
    ) {
        context.getSource().sendSuccess(
                () -> Component.literal(
                        "Steward staff menu will open here."
                ),
                false
        );

        return 1;
    }
}