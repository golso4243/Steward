package com.swornhero.steward.module.staffchat;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.staffchat.service.StaffChatService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public final class StaffChatModule {
    private StaffChatModule() {
    }

    /**
     * Must be registered before the punishment module so toggled staff
     * chat is routed before public-chat mute enforcement runs.
     */
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> StaffChatService.load());
        StaffChatService.register();
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        for (String name : new String[] {"sc", "staffchat"}) {
            dispatcher.register(
                    Commands.literal(name)
                            .requires(source -> StewardPermissions.has(
                                    source, StewardPermissions.STAFF_CHAT_USE))
                            .executes(StaffChatModule::toggle)
                            .then(Commands.argument("message", StringArgumentType.greedyString())
                                    .executes(StaffChatModule::send))
            );
        }
    }

    private static int toggle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        StaffChatService.toggle(context.getSource().getPlayerOrException());
        return 1;
    }

    private static int send(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return StaffChatService.send(
                context.getSource().getPlayerOrException(),
                StringArgumentType.getString(context, "message")
        ) ? 1 : 0;
    }
}
