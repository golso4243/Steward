package com.swornhero.steward.module.report.gui;

import com.swornhero.steward.core.gui.ActionMenu;
import com.swornhero.steward.core.gui.MenuItems;
import com.swornhero.steward.core.gui.StaffControlScreen;
import com.swornhero.steward.core.gui.StewardMenu;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.report.model.ReportRecord;
import com.swornhero.steward.module.report.model.ReportStatus;
import com.swornhero.steward.module.report.service.ReportService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Paged report list: either the server-wide triage queue or the reports
 * filed against one player.
 */
public final class ReportQueueScreen {

    private static final int FILTER_SLOT = 4;

    private ReportQueueScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            boolean includeClosed,
            int page
    ) {
        open(
                viewer,
                null,
                null,
                includeClosed,
                page,
                StaffControlScreen::open,
                "Back to Control Panel"
        );
    }

    public static void openForTarget(
            ServerPlayer viewer,
            UUID targetUuid,
            String targetName,
            int page,
            Consumer<ServerPlayer> back,
            String backLabel
    ) {
        open(viewer, targetUuid, targetName, true, page, back, backLabel);
    }

    private static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            String targetName,
            boolean includeClosed,
            int requestedPage,
            Consumer<ServerPlayer> back,
            String backLabel
    ) {
        if (!StewardPermissions.require(viewer, StewardPermissions.REPORT_VIEW)) {
            return;
        }

        List<ReportRecord> records;

        if (targetUuid != null) {
            records = ReportService.reportsAgainst(targetUuid).stream()
                    .filter(record -> includeClosed || record.isActive())
                    .toList();
        } else {
            records = includeClosed
                    ? ReportService.allReports()
                    : ReportService.activeReports();
        }

        int totalPages = MenuItems.totalPages(records.size());
        int page = MenuItems.clampPage(requestedPage, totalPages);

        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();

        Consumer<ServerPlayer> reopen = player -> open(
                player,
                targetUuid,
                targetName,
                includeClosed,
                page,
                back,
                backLabel
        );

        int start = page * MenuItems.CONTENT_SLOTS.length;
        int end = Math.min(start + MenuItems.CONTENT_SLOTS.length, records.size());

        for (int index = start; index < end; index++) {
            ReportRecord record = records.get(index);
            int slot = MenuItems.CONTENT_SLOTS[index - start];

            MenuItems.setButton(
                    container,
                    slot,
                    statusIcon(record.status()),
                    ReportService.formatReportId(record.reportId())
                            + " • " + record.targetName()
                            + " • " + record.status().displayName(),
                    "Reporter: " + record.reporterName(),
                    "Reason: " + record.reason(),
                    "Filed: " + MenuItems.formatDate(record.createdAt()),
                    record.claimedByName() != null
                            ? "Claimed by: " + record.claimedByName()
                            : null
            );

            UUID reportId = record.reportId();
            actions.put(slot, player -> ReportDetailScreen.open(player, reportId, reopen));
        }

        if (records.isEmpty()) {
            MenuItems.setButton(
                    container,
                    22,
                    Items.PAPER,
                    includeClosed ? "No Reports" : "No Open Reports"
            );
        }

        MenuItems.setButton(
                container,
                FILTER_SLOT,
                Items.HOPPER,
                includeClosed ? "Showing: All Reports" : "Showing: Open Reports",
                includeClosed
                        ? "Click to show only open and claimed reports."
                        : "Click to include resolved and dismissed reports."
        );

        actions.put(FILTER_SLOT, player -> open(
                player,
                targetUuid,
                targetName,
                !includeClosed,
                0,
                back,
                backLabel
        ));

        MenuItems.setPagination(container, page, totalPages, backLabel);

        actions.put(StewardMenu.PREVIOUS_PAGE_SLOT, player -> {
            if (page > 0) {
                open(player, targetUuid, targetName, includeClosed, page - 1, back, backLabel);
            }
        });

        actions.put(StewardMenu.NEXT_PAGE_SLOT, player -> {
            if (page + 1 < totalPages) {
                open(player, targetUuid, targetName, includeClosed, page + 1, back, backLabel);
            }
        });

        actions.put(StewardMenu.BACK_SLOT, back);
        actions.put(StewardMenu.CLOSE_SLOT, ServerPlayer::closeContainer);

        String title = (targetUuid != null ? "Reports • " + targetName : "Report Queue")
                + " " + (page + 1) + "/" + totalPages;

        ActionMenu.open(
                viewer,
                title,
                container,
                actions,
                StewardPermissions.REPORT_VIEW
        );
    }

    static Item statusIcon(ReportStatus status) {
        return switch (status) {
            case OPEN -> Items.WRITABLE_BOOK;
            case CLAIMED -> Items.NAME_TAG;
            case RESOLVED -> Items.EMERALD;
            case DISMISSED -> Items.PAPER;
        };
    }
}
