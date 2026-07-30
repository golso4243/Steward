package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.module.freeze.service.FreezeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public final class ActiveFreezeScreen {

    public static final int RECORDS_PER_PAGE = 28;

    private static final int[] RECORD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private ActiveFreezeScreen() {
        // Utility class
    }

    public static void open(ServerPlayer viewer) {
        open(viewer, 0);
    }

    public static void open(
            ServerPlayer viewer,
            int requestedPage
    ) {
        List<FreezeRecord> records =
                new ArrayList<>(
                        FreezeService.getActiveRecords()
                                .values()
                );

        records.sort(
                Comparator.comparing(
                        FreezeRecord::frozenAt
                ).reversed()
        );

        int totalPages = Math.max(
                1,
                (int) Math.ceil(
                        records.size()
                                / (double) RECORDS_PER_PAGE
                )
        );

        int page = Math.max(
                0,
                Math.min(
                        requestedPage,
                        totalPages - 1
                )
        );

        SimpleContainer container =
                new SimpleContainer(
                        ActiveFreezeMenu.MENU_SIZE
                );

        Map<Integer, UUID> recordSlots =
                new LinkedHashMap<>();

        populate(
                viewer,
                container,
                records,
                recordSlots,
                page,
                totalPages
        );

        Component title = Component.literal(
                "Active Freezes • "
                        + (page + 1)
                        + "/"
                        + totalPages
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ActiveFreezeMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        page,
                                        totalPages,
                                        recordSlots
                                ),
                        title
                )
        );
    }

    private static void populate(
            ServerPlayer viewer,
            SimpleContainer container,
            List<FreezeRecord> records,
            Map<Integer, UUID> recordSlots,
            int page,
            int totalPages
    ) {
        FreezeMenuStyle.addBorder(
                container,
                ActiveFreezeMenu.ROWS
        );

        int startIndex =
                page * RECORDS_PER_PAGE;

        int endIndex = Math.min(
                startIndex + RECORDS_PER_PAGE,
                records.size()
        );

        for (int recordIndex = startIndex;
             recordIndex < endIndex;
             recordIndex++) {

            int pageIndex =
                    recordIndex - startIndex;

            int slot =
                    RECORD_SLOTS[pageIndex];

            FreezeRecord record =
                    records.get(recordIndex);

            boolean online =
                    viewer.level()
                            .getServer()
                            .getPlayerList()
                            .getPlayer(record.targetUuid())
                            != null;

            String status =
                    online ? "Online" : "Offline";

            long durationSeconds =
                    Math.max(
                            0,
                            Duration.between(
                                    record.frozenAt(),
                                    Instant.now()
                            ).getSeconds()
                    );

            FreezeMenuStyle.setButton(
                    container,
                    slot,
                    online
                            ? Items.PLAYER_HEAD
                            : Items.SKELETON_SKULL,
                    record.targetName()
                            + " • "
                            + status
                            + " • "
                            + formatDuration(durationSeconds)
            );

            recordSlots.put(
                    slot,
                    record.targetUuid()
            );
        }

        if (records.isEmpty()) {
            FreezeMenuStyle.setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Active Freezes"
            );
        }

        if (page > 0) {
            FreezeMenuStyle.setButton(
                    container,
                    ActiveFreezeMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (page + 1)
                        + " of "
                        + totalPages
        );

        if (page + 1 < totalPages) {
            FreezeMenuStyle.setButton(
                    container,
                    ActiveFreezeMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Control Panel"
        );

        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static String formatDuration(
            long totalSeconds
    ) {
        long hours = totalSeconds / 3600;
        long minutes =
                (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        if (hours > 0) {
            return hours
                    + "h "
                    + minutes
                    + "m";
        }

        if (minutes > 0) {
            return minutes
                    + "m "
                    + seconds
                    + "s";
        }

        return seconds + "s";
    }
}