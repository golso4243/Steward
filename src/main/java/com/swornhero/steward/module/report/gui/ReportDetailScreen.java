package com.swornhero.steward.module.report.gui;

import com.swornhero.steward.core.gui.ActionMenu;
import com.swornhero.steward.core.gui.MenuItems;
import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.gui.StewardMenu;
import com.swornhero.steward.core.input.TextPromptService;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.notes.gui.PlayerNotesScreen;
import com.swornhero.steward.module.report.model.ReportRecord;
import com.swornhero.steward.module.report.model.ReportStatus;
import com.swornhero.steward.module.report.service.ReportService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class ReportDetailScreen {

    public static final String INPUT_CHANNEL = "report";

    private static final int SUMMARY_SLOT = 13;
    private static final int REPORTER_SLOT = 20;
    private static final int REASON_SLOT = 22;
    private static final int TARGET_SLOT = 24;
    private static final int CLAIM_INFO_SLOT = 30;
    private static final int CLOSURE_INFO_SLOT = 32;

    private static final int CLAIM_SLOT = 37;
    private static final int UNCLAIM_SLOT = 38;
    private static final int RESOLVE_SLOT = 39;
    private static final int DISMISS_SLOT = 40;
    private static final int TARGET_PROFILE_SLOT = 42;
    private static final int TARGET_NOTES_SLOT = 43;

    private ReportDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID reportId,
            Consumer<ServerPlayer> back
    ) {
        if (!StewardPermissions.require(viewer, StewardPermissions.REPORT_VIEW)) {
            return;
        }

        ReportRecord record = ReportService.findById(reportId);

        if (record == null) {
            viewer.sendSystemMessage(
                    Component.literal("[Steward] That report could not be found.")
            );

            back.accept(viewer);
            return;
        }

        String displayId = ReportService.formatReportId(reportId);
        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();
        Consumer<ServerPlayer> reopen = player -> open(player, reportId, back);

        MenuItems.setButton(
                container,
                SUMMARY_SLOT,
                ReportQueueScreen.statusIcon(record.status()),
                displayId + " • " + record.status().displayName(),
                "Filed: " + MenuItems.formatDate(record.createdAt())
        );

        MenuItems.setButton(
                container,
                REPORTER_SLOT,
                Items.PLAYER_HEAD,
                "Reporter: " + record.reporterName()
        );

        MenuItems.setButton(
                container,
                REASON_SLOT,
                Items.WRITABLE_BOOK,
                "Reason",
                record.reason()
        );

        MenuItems.setButton(
                container,
                TARGET_SLOT,
                Items.PLAYER_HEAD,
                "Reported Player: " + record.targetName()
        );

        MenuItems.setButton(
                container,
                CLAIM_INFO_SLOT,
                Items.NAME_TAG,
                record.claimedByName() != null
                        ? "Claimed by " + record.claimedByName()
                        : "Not Claimed",
                record.claimedAt() != null
                        ? "Claimed: " + MenuItems.formatDate(record.claimedAt())
                        : null
        );

        if (!record.isActive()) {
            MenuItems.setButton(
                    container,
                    CLOSURE_INFO_SLOT,
                    Items.BOOK,
                    record.status().displayName() + " by " + record.closedByName(),
                    "Closed: " + MenuItems.formatDate(record.closedAt()),
                    "Resolution: " + record.resolutionNote()
            );
        }

        addLifecycleActions(viewer, record, container, actions, reopen);

        ServerPlayer onlineTarget = viewer.level()
                .getServer()
                .getPlayerList()
                .getPlayer(record.targetUuid());

        if (onlineTarget != null) {
            MenuItems.setButton(
                    container,
                    TARGET_PROFILE_SLOT,
                    Items.COMPASS,
                    "Open " + record.targetName() + "'s Profile"
            );

            actions.put(
                    TARGET_PROFILE_SLOT,
                    player -> PlayerProfileScreen.open(player, record.targetUuid(), 0)
            );
        }

        if (StewardPermissions.has(viewer, StewardPermissions.NOTES_VIEW)) {
            MenuItems.setButton(
                    container,
                    TARGET_NOTES_SLOT,
                    Items.WRITTEN_BOOK,
                    "Staff Notes for " + record.targetName()
            );

            actions.put(
                    TARGET_NOTES_SLOT,
                    player -> PlayerNotesScreen.open(
                            player,
                            record.targetUuid(),
                            record.targetName(),
                            0,
                            reopen,
                            "Back to Report"
                    )
            );
        }

        MenuItems.setButton(container, StewardMenu.BACK_SLOT, Items.OAK_DOOR, "Back");
        MenuItems.setButton(container, StewardMenu.CLOSE_SLOT, Items.BARRIER, "Close");
        actions.put(StewardMenu.BACK_SLOT, back);
        actions.put(StewardMenu.CLOSE_SLOT, ServerPlayer::closeContainer);

        ActionMenu.open(
                viewer,
                "Report • " + displayId,
                container,
                actions,
                StewardPermissions.REPORT_VIEW
        );
    }

    private static void addLifecycleActions(
            ServerPlayer viewer,
            ReportRecord record,
            SimpleContainer container,
            Map<Integer, Consumer<ServerPlayer>> actions,
            Consumer<ServerPlayer> reopen
    ) {
        if (!record.isActive()
                || !StewardPermissions.has(viewer, StewardPermissions.REPORT_MANAGE)
                || record.targetUuid().equals(viewer.getUUID())) {
            return;
        }

        UUID reportId = record.reportId();
        boolean mayActOnClaim = record.isClaimedBy(viewer.getUUID())
                || StewardPermissions.has(viewer, StewardPermissions.REPORT_OVERRIDE_CLAIM);

        if (record.status() == ReportStatus.OPEN) {
            MenuItems.setButton(
                    container,
                    CLAIM_SLOT,
                    Items.NAME_TAG,
                    "Claim Report",
                    "Take ownership of this report."
            );

            actions.put(CLAIM_SLOT, player -> {
                ReportService.claim(player, reportId);
                reopen.accept(player);
            });
        }

        if (record.status() == ReportStatus.CLAIMED && mayActOnClaim) {
            MenuItems.setButton(
                    container,
                    UNCLAIM_SLOT,
                    Items.PAPER,
                    "Release Claim",
                    "Return this report to the open queue."
            );

            actions.put(UNCLAIM_SLOT, player -> {
                ReportService.unclaim(player, reportId);
                reopen.accept(player);
            });

            MenuItems.setButton(
                    container,
                    RESOLVE_SLOT,
                    Items.EMERALD,
                    "Resolve Report",
                    "Action was taken. Requires a resolution note."
            );

            actions.put(
                    RESOLVE_SLOT,
                    player -> promptForNote(player, reportId, ReportStatus.RESOLVED, reopen)
            );
        }

        if (record.status() == ReportStatus.OPEN || mayActOnClaim) {
            MenuItems.setButton(
                    container,
                    DISMISS_SLOT,
                    Items.REDSTONE,
                    "Dismiss Report",
                    "No action needed. Requires a resolution note."
            );

            actions.put(
                    DISMISS_SLOT,
                    player -> promptForNote(player, reportId, ReportStatus.DISMISSED, reopen)
            );
        }
    }

    static void promptForNote(
            ServerPlayer staff,
            UUID reportId,
            ReportStatus closedStatus,
            Consumer<ServerPlayer> detail
    ) {
        TextPromptService.begin(
                staff,
                INPUT_CHANNEL,
                "resolution note",
                ReportRecord.MAX_RESOLUTION_LENGTH,
                (player, note) -> ReportCloseConfirmScreen.open(
                        player,
                        reportId,
                        closedStatus,
                        note,
                        detail
                ),
                detail
        );
    }
}
