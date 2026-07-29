package com.swornhero.steward.gui;

import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.freeze.FreezeService;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class ActiveFreezeDetailScreen {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm:ss a"
                    )
                    .withZone(
                            ZoneId.systemDefault()
                    );

    private ActiveFreezeDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int activeFreezePage
    ) {
        FreezeRecord record =
                FreezeService.getRecord(targetUuid);

        if (record == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That freeze is no longer active."
                    )
            );

            ActiveFreezeScreen.open(
                    viewer,
                    activeFreezePage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        ActiveFreezeDetailMenu.MENU_SIZE
                );

        populate(
                viewer,
                container,
                record
        );

        Component title = Component.literal(
                "Active Freeze • "
                        + record.targetName()
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ActiveFreezeDetailMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        activeFreezePage
                                ),
                        title
                )
        );
    }

    private static void populate(
            ServerPlayer viewer,
            SimpleContainer container,
            FreezeRecord record
    ) {
        addBorder(container);

        boolean online =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(record.targetUuid())
                        != null;

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
                10,
                Items.PLAYER_HEAD,
                "Player: "
                        + record.targetName()
        );

        setButton(
                container,
                11,
                Items.NAME_TAG,
                "Case ID: "
                        + record.freezeId()
                        .toString()
        );

        setButton(
                container,
                12,
                online
                        ? BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_dye"
                        )
                )
                        : BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "gray_dye"
                        )
                ),
                "Status: "
                        + (online
                        ? "Online"
                        : "Offline")
        );

        setButton(
                container,
                14,
                Items.WRITABLE_BOOK,
                "Reason: "
                        + record.reason()
        );

        setButton(
                container,
                16,
                Items.PACKED_ICE,
                "Frozen By: "
                        + record.frozenByName()
        );

        setButton(
                container,
                20,
                Items.CLOCK,
                "Frozen At: "
                        + DATE_FORMAT.format(
                        record.frozenAt()
                )
        );

        setButton(
                container,
                22,
                Items.COMPASS,
                "Duration: "
                        + formatDuration(
                        durationSeconds
                )
        );

        setButton(
                container,
                24,
                Items.ENDER_EYE,
                "Original Dimension: "
                        + record.position()
                        .dimension()
                        .identifier()
        );

        setButton(
                container,
                29,
                Items.COMPASS,
                "Original X: "
                        + formatCoordinate(
                        record.position().x()
                )
        );

        setButton(
                container,
                31,
                Items.COMPASS,
                "Original Y: "
                        + formatCoordinate(
                        record.position().y()
                )
        );

        setButton(
                container,
                33,
                Items.COMPASS,
                "Original Z: "
                        + formatCoordinate(
                        record.position().z()
                )
        );

        setButton(
                container,
                37,
                Items.RECOVERY_COMPASS,
                "Current Dimension: "
                        + record.currentPosition()
                        .dimension()
                        .identifier()
        );

        setButton(
                container,
                38,
                Items.REDSTONE_TORCH,
                "Disconnects: "
                        + record.disconnectCount()
        );

        setButton(
                container,
                39,
                Items.COMPASS,
                "Current X: "
                        + formatCoordinate(
                        record.currentPosition().x()
                )
        );

        setButton(
                container,
                41,
                Items.COMPASS,
                "Current Y: "
                        + formatCoordinate(
                        record.currentPosition().y()
                )
        );

        setButton(
                container,
                42,
                Items.LEVER,
                "Reconnects: "
                        + record.reconnectCount()
        );

        setButton(
                container,
                43,
                Items.COMPASS,
                "Current Z: "
                        + formatCoordinate(
                        record.currentPosition().z()
                )
        );

        setButton(
                container,
                ActiveFreezeDetailMenu.RELOCATE_SLOT,
                Items.ENDER_PEARL,
                "Bring Player Here"
        );

        setButton(
                container,
                ActiveFreezeDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Active Freezes"
        );

        setButton(
                container,
                ActiveFreezeDetailMenu.UNFREEZE_SLOT,
                Items.MAGMA_CREAM,
                "Unfreeze Player"
        );

        setButton(
                container,
                ActiveFreezeDetailMenu.CLOSE_SLOT,
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
             slot < ActiveFreezeDetailMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == ActiveFreezeDetailMenu.ROWS - 1
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
                    + "m "
                    + seconds
                    + "s";
        }

        if (minutes > 0) {
            return minutes
                    + "m "
                    + seconds
                    + "s";
        }

        return seconds + "s";
    }

    private static String formatCoordinate(
            double coordinate
    ) {
        return String.format(
                "%.2f",
                coordinate
        );
    }
}