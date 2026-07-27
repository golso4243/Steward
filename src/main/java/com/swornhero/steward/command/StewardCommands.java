package com.swornhero.steward.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.gui.StaffControlScreen;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

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
    ) throws CommandSyntaxException {
        ServerPlayer player =
                context.getSource().getPlayerOrException();

        StaffControlScreen.open(player);

        return 1;
    }
}