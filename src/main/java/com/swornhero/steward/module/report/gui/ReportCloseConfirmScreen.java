package com.swornhero.steward.module.report.gui;

import com.swornhero.steward.core.gui.ActionMenu;
import com.swornhero.steward.core.gui.MenuItems;
import com.swornhero.steward.core.gui.StewardMenu;
import com.swornhero.steward.core.permission.StewardPermissions;
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

public final class ReportCloseConfirmScreen {

    private static final int SUMMARY_SLOT = 13;
    private static final int CONFIRM_SLOT = 29;
    private static final int EDIT_NOTE_SLOT = 31;
    private static final int CANCEL_SLOT = 33;

    private ReportCloseConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID reportId,
            ReportStatus closedStatus,
            String note,
            Consumer<ServerPlayer> detail
    ) {
        if (!StewardPermissions.require(viewer, StewardPermissions.REPORT_MANAGE)) {
            return;
        }

        ReportRecord record = ReportService.findById(reportId);

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal("[Steward] That report is no longer open.")
            );

            detail.accept(viewer);
            return;
        }

        String action = closedStatus == ReportStatus.RESOLVED ? "Resolve" : "Dismiss";
        String displayId = ReportService.formatReportId(reportId);

        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();

        MenuItems.setButton(
                container,
                SUMMARY_SLOT,
                Items.WRITABLE_BOOK,
                action + " " + displayId + "?",
                "Reported Player: " + record.targetName(),
                "Reason: " + record.reason(),
                "Resolution: " + note
        );

        MenuItems.setButton(
                container,
                CONFIRM_SLOT,
                Items.EMERALD,
                "Confirm " + action
        );

        MenuItems.setButton(
                container,
                EDIT_NOTE_SLOT,
                Items.WRITABLE_BOOK,
                "Edit Resolution Note"
        );

        MenuItems.setButton(
                container,
                CANCEL_SLOT,
                Items.BARRIER,
                "Cancel"
        );

        actions.put(CONFIRM_SLOT, player -> {
            if (closedStatus == ReportStatus.RESOLVED) {
                ReportService.resolve(player, reportId, note);
            } else {
                ReportService.dismiss(player, reportId, note);
            }

            detail.accept(player);
        });

        actions.put(
                EDIT_NOTE_SLOT,
                player -> ReportDetailScreen.promptForNote(player, reportId, closedStatus, detail)
        );

        actions.put(CANCEL_SLOT, detail);
        actions.put(StewardMenu.CLOSE_SLOT, ServerPlayer::closeContainer);

        ActionMenu.open(
                viewer,
                action + " Report • " + displayId,
                container,
                actions,
                StewardPermissions.REPORT_MANAGE
        );
    }
}
