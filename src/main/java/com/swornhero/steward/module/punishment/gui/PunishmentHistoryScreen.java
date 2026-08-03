package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentStatus;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import com.swornhero.steward.module.punishment.service.PunishmentService;
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

public final class PunishmentHistoryScreen {

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
                    )
                    .withZone(
                            ZoneId.systemDefault()
                    );

    private PunishmentHistoryScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer
    ) {
        open(viewer, 0);
    }

    public static void open(
            ServerPlayer viewer,
            int requestedPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_VIEW
        )) {
            return;
        }

        List<PunishmentRecord> records =
                PunishmentService.allPunishments();

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
                                requestedPage,
                                totalPages - 1
                        )
                );

        SimpleContainer container =
                new SimpleContainer(
                        PunishmentHistoryMenu.MENU_SIZE
                );

        Map<Integer, UUID> recordSlots =
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
                        "Punishment History • "
                                + (historyPage + 1)
                                + "/"
                                + totalPages
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentHistoryMenu(
                                        containerId,
                                        inventory,
                                        container,
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
            List<PunishmentRecord> records,
            Map<Integer, UUID> recordSlots,
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

            PunishmentRecord record =
                    records.get(recordIndex);

            String label =
                    PunishmentService.formatPunishmentId(
                            record.punishmentId()
                    )
                            + " • "
                            + record.targetName()
                            + " • "
                            + record.type().displayName()
                            + " • "
                            + record.status().displayName()
                            + " • "
                            + DATE_FORMAT.format(
                            record.issuedAt()
                    );

            setButton(
                    container,
                    slot,
                    historyIcon(
                            record.type(),
                            record.status()
                    ),
                    label
            );

            recordSlots.put(
                    slot,
                    record.punishmentId()
            );
        }

        if (records.isEmpty()) {
            setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Punishment History"
            );
        }

        if (historyPage > 0) {
            setButton(
                    container,
                    PunishmentHistoryMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                PunishmentHistoryMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (historyPage + 1)
                        + " of "
                        + totalPages
        );

        if (historyPage + 1 < totalPages) {
            setButton(
                    container,
                    PunishmentHistoryMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                PunishmentHistoryMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Punishments"
        );

        setButton(
                container,
                PunishmentHistoryMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static Item historyIcon(
            PunishmentType type,
            PunishmentStatus status
    ) {
        if (status == PunishmentStatus.REVOKED) {
            return Items.MAGMA_CREAM;
        }

        if (status == PunishmentStatus.EXPIRED) {
            return BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "gray_wool"
                    )
            );
        }

        if (status == PunishmentStatus.COMPLETED) {
            return BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "lime_wool"
                    )
            );
        }

        return switch (type) {
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
             slot < PunishmentHistoryMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentHistoryMenu.ROWS - 1
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