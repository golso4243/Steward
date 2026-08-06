package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningLevel;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
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
import java.util.UUID;

public final class WarningHistoryScreen {

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

    private WarningHistoryScreen() {
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
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        )) {
            return;
        }

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        String targetName =
                target != null
                        ? target.getName().getString()
                        : targetUuid.toString();

        List<WarningRecord> records =
                WarningService.warningsFor(
                        targetUuid
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
                        WarningHistoryMenu.MENU_SIZE
                );

        Map<Integer, WarningRecord> recordSlots =
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
                        "Warning History • "
                                + targetName
                                + " "
                                + (historyPage + 1)
                                + "/"
                                + totalPages
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningHistoryMenu(
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
            List<WarningRecord> records,
            Map<Integer, WarningRecord> recordSlots,
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

            WarningRecord record =
                    records.get(recordIndex);

            String date =
                    DATE_FORMAT.format(
                            record.issuedAt()
                    );

            String warningId =
                    WarningService.formatWarningId(
                            record.warningId()
                    );

            String label =
                    warningId
                            + " • "
                            + record.level().displayName()
                            + " • "
                            + record.category().displayName()
                            + " • "
                            + record.status().displayName()
                            + " • "
                            + date;

            setButton(
                    container,
                    slot,
                    warningIcon(record.level()),
                    label
            );

            recordSlots.put(
                    slot,
                    record
            );
        }

        if (records.isEmpty()) {
            setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Warning History"
            );
        }

        if (historyPage > 0) {
            setButton(
                    container,
                    WarningHistoryMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                WarningHistoryMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (historyPage + 1)
                        + " of "
                        + totalPages
        );

        if (historyPage + 1 < totalPages) {
            setButton(
                    container,
                    WarningHistoryMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                WarningHistoryMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History Menu"
        );

        setButton(
                container,
                WarningHistoryMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static Item warningIcon(
            WarningLevel level
    ) {
        if (level == null) {
            return Items.PAPER;
        }

        return switch (level) {
            case VERBAL ->
                    Items.PAPER;

            case FORMAL ->
                    Items.WRITABLE_BOOK;

            case FINAL ->
                    Items.ENCHANTED_BOOK;
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
             slot < WarningHistoryMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningHistoryMenu.ROWS - 1
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