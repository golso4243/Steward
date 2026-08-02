package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Items;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FreezeHistoryScreen {
    public static final int RECORDS_PER_PAGE = 28;

    private static final int[] RECORD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                    .withZone(ZoneId.systemDefault());

    private FreezeHistoryScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        open(
                viewer,
                targetUuid,
                browserPage,
                0
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int requestedHistoryPage
    ) {
        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        String targetName =
                target != null
                        ? target.getName().getString()
                        : targetUuid.toString();

        List<FreezeHistoryEntry> records =
                FreezeHistoryService.getForPlayer(targetUuid);

        int totalPages = Math.max(
                1,
                (int) Math.ceil(
                        records.size()
                                / (double) RECORDS_PER_PAGE
                )
        );

        int historyPage = Math.max(
                0,
                Math.min(
                        requestedHistoryPage,
                        totalPages - 1
                )
        );

        SimpleContainer container =
                new SimpleContainer(
                        FreezeHistoryMenu.MENU_SIZE
                );

        Map<Integer, FreezeHistoryEntry> recordSlots =
                new LinkedHashMap<>();

        populate(
                container,
                records,
                recordSlots,
                historyPage,
                totalPages
        );

        Component title = Component.literal(
                "Freeze History • "
                        + targetName
                        + " "
                        + (historyPage + 1)
                        + "/"
                        + totalPages
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new FreezeHistoryMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        totalPages,
                                        recordSlots
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            List<FreezeHistoryEntry> records,
            Map<Integer, FreezeHistoryEntry> recordSlots,
            int historyPage,
            int totalPages
    ) {
        FreezeMenuStyle.addBorder(
                container,
                FreezeHistoryMenu.ROWS
        );

        int startIndex =
                historyPage * RECORDS_PER_PAGE;

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

            FreezeHistoryEntry entry =
                    records.get(recordIndex);

            String date =
                    DATE_FORMAT.format(
                            entry.frozenAt()
                    );

            FreezeMenuStyle.setButton(
                    container,
                    slot,
                    Items.PACKED_ICE,
                    entry.reason() + " • " + date
            );

            recordSlots.put(
                    slot,
                    entry
            );
        }

        if (records.isEmpty()) {
            FreezeMenuStyle.setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Freeze History"
            );
        }

        if (historyPage > 0) {
            FreezeMenuStyle.setButton(
                    container,
                    FreezeHistoryMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (historyPage + 1)
                        + " of "
                        + totalPages
        );

        if (historyPage + 1 < totalPages) {
            FreezeMenuStyle.setButton(
                    container,
                    FreezeHistoryMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History"
        );

        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }
}