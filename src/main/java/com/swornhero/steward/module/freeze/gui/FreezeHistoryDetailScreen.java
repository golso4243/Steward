package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.core.history.GlobalHistoryView;
import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.history.GlobalModerationHistoryScreen;
import com.swornhero.steward.core.history.ModerationHistoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Items;

import java.time.Duration;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class FreezeHistoryDetailScreen {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm:ss a"
                    )
                    .withZone(
                            ZoneId.systemDefault()
                    );

    private FreezeHistoryDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            FreezeHistoryEntry entry
    ) {
        open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                entry,
                HistoryReturnTarget.FREEZE_HISTORY
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            FreezeHistoryEntry entry,
            HistoryReturnTarget returnTarget
    ) {
        if (entry == null) {
            returnToSource(
                    viewer,
                    targetUuid,
                    browserPage,
                    historyPage,
                    returnTarget
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        FreezeHistoryDetailMenu.MENU_SIZE
                );

        populate(
                container,
                entry
        );

        Component title =
                Component.literal(
                        "Freeze Details • "
                                + entry.targetName()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new FreezeHistoryDetailMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        entry,
                                        returnTarget
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            FreezeHistoryEntry entry
    ) {
        FreezeMenuStyle.addBorder(
                container,
                FreezeHistoryDetailMenu.ROWS
        );

        long durationSeconds =
                Math.max(
                        0,
                        Duration.between(
                                entry.frozenAt(),
                                entry.unfrozenAt()
                        ).getSeconds()
                );

        /*
         * Completed freeze summary.
         */
        FreezeMenuStyle.setButton(
                container,
                10,
                Items.PLAYER_HEAD,
                "Player: "
                        + entry.targetName()
        );

        FreezeMenuStyle.setButton(
                container,
                11,
                Items.NAME_TAG,
                "Case ID: "
                        + entry.freezeId()
                        .toString()
        );

        FreezeMenuStyle.setButton(
                container,
                12,
                Items.GUNPOWDER,
                "Status: Completed"
        );

        FreezeMenuStyle.setButton(
                container,
                14,
                Items.PACKED_ICE,
                "Frozen By: "
                        + entry.frozenByName()
        );

        FreezeMenuStyle.setButton(
                container,
                15,
                Items.MAGMA_CREAM,
                "Unfrozen By: "
                        + entry.unfrozenByName()
        );

        FreezeMenuStyle.setButton(
                container,
                16,
                Items.WRITABLE_BOOK,
                "Reason: "
                        + entry.reason()
        );

        /*
         * Freeze timing.
         */
        FreezeMenuStyle.setButton(
                container,
                23,
                Items.CLOCK,
                "Started: "
                        + DATE_FORMAT.format(
                        entry.frozenAt()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                24,
                Items.RECOVERY_COMPASS,
                "Ended: "
                        + DATE_FORMAT.format(
                        entry.unfrozenAt()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                25,
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
                29,
                Items.ENDER_EYE,
                "Original Dimension: "
                        + entry.originalPosition()
                        .dimension()
        );

        FreezeMenuStyle.setButton(
                container,
                37,
                Items.COMPASS,
                "Original X: "
                        + formatCoordinate(
                        entry.originalPosition().x()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                38,
                Items.COMPASS,
                "Original Y: "
                        + formatCoordinate(
                        entry.originalPosition().y()
                )
        );

        FreezeMenuStyle.setButton(
                container,
                39,
                Items.COMPASS,
                "Original Z: "
                        + formatCoordinate(
                        entry.originalPosition().z()
                )
        );

        /*
         * Freeze activity.
         */
        FreezeMenuStyle.setButton(
                container,
                41,
                Items.REDSTONE_TORCH,
                "Disconnects: "
                        + entry.disconnectCount()
        );

        FreezeMenuStyle.setButton(
                container,
                42,
                Items.LEVER,
                "Reconnects: "
                        + entry.reconnectCount()
        );

        FreezeMenuStyle.setButton(
                container,
                43,
                Items.ENDER_PEARL,
                "Relocations: "
                        + entry.relocations().size()
        );

        /*
         * Navigation.
         */
        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History"
        );

        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryDetailMenu.PROFILE_SLOT,
                Items.PLAYER_HEAD,
                "Player Profile"
        );

        FreezeMenuStyle.setButton(
                container,
                FreezeHistoryDetailMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static String formatDuration(
            long durationSeconds
    ) {
        long hours =
                durationSeconds / 3600;

        long minutes =
                (durationSeconds % 3600) / 60;

        long seconds =
                durationSeconds % 60;

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

    private static void returnToSource(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        switch (returnTarget) {
            case ALL_ACTIVITY ->
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            historyPage
                    );

            case GLOBAL_ALL_ACTIVITY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            historyPage
                    );

            case GLOBAL_WARNING_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.WARNING_HISTORY,
                            historyPage
                    );

            case FREEZE_HISTORY, WARNING_HISTORY ->
                    FreezeHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            historyPage
                    );
        }
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