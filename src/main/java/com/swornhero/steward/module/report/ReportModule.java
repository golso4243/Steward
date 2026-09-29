package com.swornhero.steward.module.report;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.core.player.KnownPlayer;
import com.swornhero.steward.core.player.KnownPlayerService;
import com.swornhero.steward.module.report.service.ReportService;
import com.swornhero.steward.module.vanish.service.VanishService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

public final class ReportModule {

    private ReportModule() {
        // Utility class
    }

    public static void register() {
        ReportService.register();

        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) -> registerCommands(dispatcher)
        );
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("report")
                        .requires(source -> source.isPlayer()
                                && StewardPermissions.hasDefault(
                                source, StewardPermissions.REPORT_SUBMIT))
                        .then(Commands.argument("player", StringArgumentType.word())
                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                        visiblePlayerNames(context.getSource()),
                                        builder
                                ))
                                .then(Commands.argument("reason", StringArgumentType.greedyString())
                                        .executes(ReportModule::submitReport)))
        );
    }

    private static Iterable<String> visiblePlayerNames(CommandSourceStack source) {
        ServerPlayer viewer = source.getPlayer();

        return source.getServer().getPlayerList().getPlayers().stream()
                .filter(target -> viewer == null || VanishService.canSee(viewer, target))
                .filter(target -> viewer == null || !target.getUUID().equals(viewer.getUUID()))
                .map(target -> target.getName().getString())
                .toList();
    }

    private static int submitReport(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {

        ServerPlayer reporter = context.getSource().getPlayerOrException();
        String targetName = StringArgumentType.getString(context, "player");
        String reason = StringArgumentType.getString(context, "reason");

        if (!ReportService.checkCooldown(reporter)) {
            return 0;
        }

        ServerPlayer onlineTarget = context.getSource()
                .getServer()
                .getPlayerList()
                .getPlayerByName(targetName);

        if (onlineTarget != null) {
            return ReportService.submit(
                    reporter,
                    onlineTarget.getUUID(),
                    onlineTarget.getName().getString(),
                    reason
            ) != null ? 1 : 0;
        }

        Optional<KnownPlayer> knownTarget = KnownPlayerService.findByName(targetName);

        if (knownTarget.isEmpty()) {
            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] No player named " + targetName
                                    + " has played on this server."
                    )
            );

            return 0;
        }

        return ReportService.submit(
                reporter,
                knownTarget.get().uuid(),
                knownTarget.get().name(),
                reason
        ) != null ? 1 : 0;
    }
}
