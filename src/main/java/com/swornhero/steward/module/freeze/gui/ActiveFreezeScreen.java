package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.module.freeze.service.FreezeService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        addBorder(container);

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

            setButton(
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
            setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Active Freezes"
            );
        }

        if (page > 0) {
            setButton(
                    container,
                    ActiveFreezeMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                ActiveFreezeMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (page + 1)
                        + " of "
                        + totalPages
        );

        if (page + 1 < totalPages) {
            setButton(
                    container,
                    ActiveFreezeMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                ActiveFreezeMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Control Panel"
        );

        setButton(
                container,
                ActiveFreezeMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static void addBorder(
            SimpleContainer container
    ) {
        Item borderItem =
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "purple_stained_glass_pane"
                        )
                );

        for (int slot = 0;
             slot < ActiveFreezeMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == ActiveFreezeMenu.ROWS - 1
                            || column == 0
                            || column == 8;

            if (!border) {
                continue;
            }

            ItemStack pane =
                    new ItemStack(borderItem);

            pane.set(
                    DataComponents.TOOLTIP_DISPLAY,
                    new TooltipDisplay(
                            true,
                            new LinkedHashSet<>()
                    )
            );

            container.setItem(slot, pane);
        }
    }

    private static void setButton(
            SimpleContainer container,
            int slot,
            Item item,
            String name
    ) {
        ItemStack stack =
                new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        container.setItem(slot, stack);
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