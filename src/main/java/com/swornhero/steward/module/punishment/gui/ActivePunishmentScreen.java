package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
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

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ActivePunishmentScreen {

    public static final int RECORDS_PER_PAGE = 28;

    private static final int[] RECORD_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private ActivePunishmentScreen() {
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
                PunishmentService.activePunishments();

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
                        ActivePunishmentMenu.MENU_SIZE
                );

        Map<Integer, UUID> recordSlots =
                new LinkedHashMap<>();

        populate(
                container,
                records,
                recordSlots,
                page,
                totalPages
        );

        Component title =
                Component.literal(
                        "Active Punishments • "
                                + (page + 1)
                                + "/"
                                + totalPages
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ActivePunishmentMenu(
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
            SimpleContainer container,
            List<PunishmentRecord> records,
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

            PunishmentRecord record =
                    records.get(recordIndex);

            setButton(
                    container,
                    slot,
                    itemFor(record.type()),
                    record.targetName()
                            + " • "
                            + record.type().displayName()
                            + " • "
                            + formatRemaining(record)
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
                    "No Active Punishments"
            );
        }

        if (page > 0) {
            setButton(
                    container,
                    ActivePunishmentMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                ActivePunishmentMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page "
                        + (page + 1)
                        + " of "
                        + totalPages
        );

        if (page + 1 < totalPages) {
            setButton(
                    container,
                    ActivePunishmentMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                ActivePunishmentMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Punishments"
        );

        setButton(
                container,
                ActivePunishmentMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static Item itemFor(
            PunishmentType type
    ) {
        return switch (type) {
            case MUTE ->
                    Items.NAME_TAG;

            case TEMPORARY_BAN ->
                    Items.CLOCK;

            case PERMANENT_BAN ->
                    Items.BARRIER;

            case KICK ->
                    Items.LEATHER_BOOTS;
        };
    }

    private static String formatRemaining(
            PunishmentRecord record
    ) {
        if (record.expiresAt() == null) {
            return "Permanent";
        }

        long totalSeconds =
                Math.max(
                        0,
                        Duration.between(
                                Instant.now(),
                                record.expiresAt()
                        ).getSeconds()
                );

        long days =
                totalSeconds / 86_400;

        long hours =
                (totalSeconds % 86_400) / 3_600;

        long minutes =
                (totalSeconds % 3_600) / 60;

        if (days > 0) {
            return days
                    + "d "
                    + hours
                    + "h remaining";
        }

        if (hours > 0) {
            return hours
                    + "h "
                    + minutes
                    + "m remaining";
        }

        return minutes + "m remaining";
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
             slot < ActivePunishmentMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == ActivePunishmentMenu.ROWS - 1
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