package com.swornhero.steward.core.history;

import com.swornhero.steward.core.permission.StewardPermissions;
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

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public final class GlobalModerationHistoryScreen {

    public static final int RECORDS_PER_PAGE = 28;

    private static final int[] RECORD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "MMM d, yyyy h:mm a"
            ).withZone(
                    ZoneId.systemDefault()
            );

    private GlobalModerationHistoryScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer
    ) {
        open(
                viewer,
                GlobalHistoryView.ALL_ACTIVITY,
                0
        );
    }

    public static void open(
            ServerPlayer viewer,
            int requestedHistoryPage
    ) {
        open(
                viewer,
                GlobalHistoryView.ALL_ACTIVITY,
                requestedHistoryPage
        );
    }

    public static void open(
            ServerPlayer viewer,
            GlobalHistoryView view
    ) {
        open(
                viewer,
                view,
                0
        );
    }

    public static void open(
            ServerPlayer viewer,
            GlobalHistoryView requestedView,
            int requestedHistoryPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        )) {
            return;
        }

        GlobalHistoryView view =
                requestedView != null
                        ? requestedView
                        : GlobalHistoryView.ALL_ACTIVITY;

        List<ModerationHistoryItem> records =
                view.showsAllTypes()
                        ? ModerationHistoryService.getAll()
                        : ModerationHistoryService.getAllByType(
                        view.actionType()
                );

        int totalPages =
                Math.max(
                        1,
                        (int) Math.ceil(
                                records.size()
                                        / (double) RECORDS_PER_PAGE
                        )
                );

        int historyPage =
                Math.max(
                        0,
                        Math.min(
                                requestedHistoryPage,
                                totalPages - 1
                        )
                );

        SimpleContainer container =
                new SimpleContainer(
                        GlobalModerationHistoryMenu.MENU_SIZE
                );

        Map<Integer, ModerationHistoryItem> recordSlots =
                new LinkedHashMap<>();

        populate(
                container,
                records,
                recordSlots,
                historyPage,
                totalPages
        );

        Component title =
                Component.literal(
                        view.title()
                                + " • "
                                + (historyPage + 1)
                                + "/"
                                + totalPages
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new GlobalModerationHistoryMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        view,
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
            List<ModerationHistoryItem> records,
            Map<Integer, ModerationHistoryItem> recordSlots,
            int historyPage,
            int totalPages
    ) {
        addBorder(container);

        int startIndex =
                historyPage * RECORDS_PER_PAGE;

        int endIndex =
                Math.min(
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

            ModerationHistoryItem item =
                    records.get(recordIndex);

            String date =
                    DATE_FORMAT.format(
                            item.occurredAt()
                    );

            String label =
                    item.targetName()
                            + " • "
                            + item.type().displayName()
                            + " • "
                            + item.summary()
                            + " • "
                            + date;

            setButton(
                    container,
                    slot,
                    itemIcon(item.type()),
                    label
            );

            recordSlots.put(
                    slot,
                    item
            );
        }

        if (records.isEmpty()) {
            setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Moderation History"
            );
        }

        if (historyPage > 0) {
            setButton(
                    container,
                    GlobalModerationHistoryMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                GlobalModerationHistoryMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (historyPage + 1)
                        + " of "
                        + totalPages
        );

        if (historyPage + 1 < totalPages) {
            setButton(
                    container,
                    GlobalModerationHistoryMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                GlobalModerationHistoryMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History"
        );

        setButton(
                container,
                GlobalModerationHistoryMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static Item itemIcon(
            ModerationActionType type
    ) {
        return switch (type) {
            case WARNING ->
                    Items.PAPER;

            case FREEZE ->
                    Items.PACKED_ICE;

            case MUTE ->
                    Items.NAME_TAG;

            case KICK ->
                    Items.LEATHER_BOOTS;

            case TEMPORARY_BAN ->
                    Items.CLOCK;

            case PERMANENT_BAN ->
                    Items.BARRIER;
        };
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
             slot < GlobalModerationHistoryMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == GlobalModerationHistoryMenu.ROWS - 1
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

            container.setItem(
                    slot,
                    pane
            );
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

        container.setItem(
                slot,
                stack
        );
    }
}