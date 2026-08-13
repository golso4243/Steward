package com.swornhero.steward.core.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.swornhero.steward.core.gui.StaffControlScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.core.status.StewardStatusService;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.module.warning.gui.WarningHistoryDetailScreen;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
import com.swornhero.steward.module.warning.service.WarningDraftInputService;
import com.swornhero.steward.module.freeze.gui.ActiveFreezeDetailScreen;
import com.swornhero.steward.module.freeze.gui.FreezeHistoryDetailScreen;
import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.freeze.service.FreezeService;
import com.swornhero.steward.module.punishment.gui.PunishmentHistoryDetailScreen;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.service.PunishmentService;

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
                        .then(
                                Commands.literal("view")
                                        .then(
                                                Commands.literal("warning")
                                                        .requires(
                                                                StewardCommands
                                                                        ::canViewWarningRecord
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "id",
                                                                        StringArgumentType.word()
                                                                ).executes(
                                                                        StewardCommands
                                                                                ::viewWarningRecord
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("freeze")
                                                        .requires(
                                                                StewardCommands
                                                                        ::canViewFreezeRecord
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "id",
                                                                        StringArgumentType.word()
                                                                ).executes(
                                                                        StewardCommands
                                                                                ::viewFreezeRecord
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("punishment")
                                                        .requires(
                                                                StewardCommands
                                                                        ::canViewPunishmentRecord
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "id",
                                                                        StringArgumentType.word()
                                                                ).executes(
                                                                        StewardCommands
                                                                                ::viewPunishmentRecord
                                                                )
                                                        )
                                        )
                        )
                        .then(
                                Commands.literal("warning")
                                        .then(
                                                Commands.literal("acknowledge")
                                                        .then(
                                                                Commands.argument(
                                                                        "id",
                                                                        StringArgumentType.word()
                                                                ).executes(
                                                                        StewardCommands
                                                                                ::acknowledgeWarning
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("input")
                                                        .requires(
                                                                StewardCommands
                                                                        ::canIssueWarning
                                                        )
                                                        .then(
                                                                Commands.argument(
                                                                        "text",
                                                                        StringArgumentType.greedyString()
                                                                ).executes(
                                                                        StewardCommands
                                                                                ::submitWarningInput
                                                                )
                                                        )
                                        )
                                        .then(
                                                Commands.literal("cancel-input")
                                                        .requires(
                                                                StewardCommands
                                                                        ::canIssueWarning
                                                        )
                                                        .executes(
                                                                StewardCommands
                                                                        ::cancelWarningInput
                                                        )
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

    private static boolean canViewPunishmentRecord(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.PUNISHMENT_VIEW
        ) || StewardPermissions.has(
                source,
                StewardPermissions.HISTORY_VIEW
        );
    }

    private static int viewPunishmentRecord(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        CommandSourceStack source =
                context.getSource();

        if (!canViewPunishmentRecord(source)) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] You do not have permission "
                                    + "to view punishment records."
                    )
            );

            return 0;
        }

        ServerPlayer viewer =
                source.getPlayerOrException();

        String displayId =
                StringArgumentType.getString(
                        context,
                        "id"
                );

        PunishmentRecord record =
                PunishmentService.findByDisplayId(
                        displayId
                );

        if (record == null) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] Punishment record "
                                    + displayId
                                    + " was not found."
                    )
            );

            return 0;
        }

        PunishmentHistoryDetailScreen.open(
                viewer,
                record.punishmentId(),
                record.targetUuid(),
                0,
                0,
                HistoryReturnTarget.STAFF_MENU
        );

        return 1;
    }

    private static boolean canViewFreezeRecord(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.FREEZE_VIEW
        ) || StewardPermissions.has(
                source,
                StewardPermissions.HISTORY_VIEW
        );
    }

    private static int viewFreezeRecord(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        CommandSourceStack source =
                context.getSource();

        if (!canViewFreezeRecord(source)) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] You do not have permission "
                                    + "to view freeze records."
                    )
            );

            return 0;
        }

        ServerPlayer viewer =
                source.getPlayerOrException();

        String displayId =
                StringArgumentType.getString(
                        context,
                        "id"
                );

        FreezeRecord activeRecord =
                FreezeService.findByDisplayId(
                        displayId
                );

        if (activeRecord != null) {
            ActiveFreezeDetailScreen.open(
                    viewer,
                    activeRecord.targetUuid(),
                    0
            );

            return 1;
        }

        FreezeHistoryEntry historyEntry =
                FreezeHistoryService.findByDisplayId(
                        displayId
                );

        if (historyEntry != null) {
            FreezeHistoryDetailScreen.open(
                    viewer,
                    historyEntry.targetUuid(),
                    0,
                    0,
                    historyEntry,
                    HistoryReturnTarget.STAFF_MENU
            );

            return 1;
        }

        source.sendFailure(
                Component.literal(
                        "[Steward] Freeze record "
                                + displayId
                                + " was not found."
                )
        );

        return 0;
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

    private static boolean canViewWarningRecord(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.WARNING_VIEW
        ) || StewardPermissions.has(
                source,
                StewardPermissions.HISTORY_VIEW
        );
    }

    private static boolean canIssueWarning(
            CommandSourceStack source
    ) {
        return StewardPermissions.has(
                source,
                StewardPermissions.WARNING_ISSUE
        );
    }

    private static int submitWarningInput(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        ServerPlayer staff =
                context.getSource()
                        .getPlayerOrException();

        String value =
                StringArgumentType.getString(
                        context,
                        "text"
                );

        return WarningDraftInputService.submit(
                staff,
                value
        ) ? 1 : 0;
    }

    private static int cancelWarningInput(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        ServerPlayer staff =
                context.getSource()
                        .getPlayerOrException();

        return WarningDraftInputService.cancel(staff)
                ? 1
                : 0;
    }

    private static int acknowledgeWarning(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();

        String displayId =
                StringArgumentType.getString(
                        context,
                        "id"
                );

        WarningRecord record =
                WarningService.findByDisplayId(displayId);

        if (record == null) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] Warning record "
                                    + displayId
                                    + " was not found."
                    )
            );

            return 0;
        }

        if (!player.getUUID().equals(record.targetUuid())) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] You can only acknowledge your own warnings."
                    )
            );

            return 0;
        }

        if (!record.isActive()) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] Only active warnings can be acknowledged."
                    )
            );

            return 0;
        }

        if (record.acknowledged()) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] That warning is already acknowledged."
                    )
            );

            return 0;
        }

        if (!WarningService.acknowledgeWarning(
                record.warningId(),
                player.getUUID()
        )) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] The warning could not be acknowledged."
                    )
            );

            return 0;
        }

        source.sendSuccess(
                () -> Component.literal(
                        "[Steward] Warning "
                                + WarningService.formatWarningId(
                                record.warningId()
                        )
                                + " acknowledged."
                ),
                false
        );

        return 1;
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

    private static int viewWarningRecord(
            CommandContext<CommandSourceStack> context
    ) throws CommandSyntaxException {
        CommandSourceStack source =
                context.getSource();

        if (!canViewWarningRecord(source)) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] You do not have permission "
                                    + "to view warning records."
                    )
            );

            return 0;
        }

        ServerPlayer viewer =
                source.getPlayerOrException();

        String displayId =
                StringArgumentType.getString(
                        context,
                        "id"
                );

        WarningRecord record =
                WarningService.findByDisplayId(
                        displayId
                );

        if (record == null) {
            source.sendFailure(
                    Component.literal(
                            "[Steward] Warning record "
                                    + displayId
                                    + " was not found."
                    )
            );

            return 0;
        }

        WarningHistoryDetailScreen.open(
                viewer,
                record.targetUuid(),
                0,
                0,
                record,
                HistoryReturnTarget.STAFF_MENU
        );

        return 1;
    }
}
