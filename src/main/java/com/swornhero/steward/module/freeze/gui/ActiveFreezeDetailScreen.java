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
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
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
        FreezeMenuStyle.addBorder(
                container,
                ActiveFreezeDetailMenu.ROWS
        );

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

        /*
         * Freeze summary.
         */
        FreezeMenuStyle.setButton(
                container,
                10,
                Items.PLAYER_HEAD,
                "Player: "
                        + record.targetName()
        );

        FreezeMenuStyle.setButton(
                container,
                11,
                Items.NAME_TAG,
                "Case ID: "
                        + record.freezeId()
                        .toString()
        );

        FreezeMenuStyle.setButton(
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

        FreezeMenuStyle.setButton(
                container,
                13,
                Items.CLOCK,
                "Frozen At: "
                        + DATE_FORMAT.format(
                        record.frozenAt()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                14,
                Items.PACKED_ICE,
                "Frozen By: "
                        + record.frozenByName()
        );

        FreezeMenuStyle.setButton(
                container,
                15,
                Items.WRITABLE_BOOK,
                "Reason: "
                        + record.reason()
        );

        FreezeMenuStyle.setButton(
                container,
                16,
                Items.COMPASS,
                "Duration: "
                        + formatDuration(
                        durationSeconds
                )
        );

        /*
         * Original freeze location.
         */
        FreezeMenuStyle.setButton(
                container,
                20,
                Items.ENDER_EYE,
                "Original Dimension: "
                        + record.position()
                        .dimension()
                        .identifier()
        );

        FreezeMenuStyle.setButton(
                container,
                28,
                Items.COMPASS,
                "Original X: "
                        + formatCoordinate(
                        record.position().x()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                29,
                Items.COMPASS,
                "Original Y: "
                        + formatCoordinate(
                        record.position().y()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                30,
                Items.COMPASS,
                "Original Z: "
                        + formatCoordinate(
                        record.position().z()
                )
        );

        /*
         * Connection activity.
         */
        FreezeMenuStyle.setButton(
                container,
                33,
                Items.REDSTONE_TORCH,
                "Disconnects: "
                        + record.disconnectCount()
        );

        FreezeMenuStyle.setButton(
                container,
                34,
                Items.LEVER,
                "Reconnects: "
                        + record.reconnectCount()
        );

        /*
         * Primary moderation action.
         */
        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeDetailMenu.UNFREEZE_SLOT,
                Items.MAGMA_CREAM,
                "Unfreeze Player"
        );

        /*
         * Navigation and utility actions.
         */
        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeDetailMenu.RELOCATE_SLOT,
                Items.ENDER_PEARL,
                "Bring Player Here"
        );

        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Active Freezes"
        );

        FreezeMenuStyle.setButton(
                container,
                ActiveFreezeDetailMenu.CLOSE_SLOT,
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