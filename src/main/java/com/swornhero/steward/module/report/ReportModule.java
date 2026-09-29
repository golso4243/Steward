package com.swornhero.steward.module.report;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.report.service.ReportService;
import com.swornhero.steward.module.vanish.service.VanishService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class ReportModule {

    /**
     * Offline names may require a Mojang profile lookup, so unresolved
     * names are throttled separately from the successful-report cooldown.
     */
    private static final Duration OFFLINE_LOOKUP_THROTTLE = Duration.ofSeconds(5);

    private static final Map<UUID, Instant> LAST_OFFLINE_LOOKUP = new HashMap<>();

    private ReportModule() {
        // Utility class
    }

    public static void register() {
        ReportService.register();

        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> LAST_OFFLINE_LOOKUP.remove(handler.player.getUUID())
        );

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

        MinecraftServer server = context.getSource().getServer();
        ServerPlayer onlineTarget = server.getPlayerList().getPlayerByName(targetName);

        if (onlineTarget != null) {
            return ReportService.submit(
                    reporter,
                    onlineTarget.getUUID(),
                    onlineTarget.getName().getString(),
                    reason
            ) != null ? 1 : 0;
        }

        Instant now = Instant.now();
        Instant lastLookup = LAST_OFFLINE_LOOKUP.get(reporter.getUUID());

        if (lastLookup != null
                && lastLookup.plus(OFFLINE_LOOKUP_THROTTLE).isAfter(now)) {
            reporter.sendSystemMessage(
                    Component.literal("[Steward] Please wait a moment before trying again.")
            );

            return 0;
        }

        LAST_OFFLINE_LOOKUP.put(reporter.getUUID(), now);

        Optional<NameAndId> profile = server.services().nameToIdCache().get(targetName);

        if (profile.isEmpty()) {
            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] No player named " + targetName + " could be found."
                    )
            );

            return 0;
        }

        return ReportService.submit(
                reporter,
                profile.get().id(),
                profile.get().name(),
                reason
        ) != null ? 1 : 0;
    }
}
