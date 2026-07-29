package com.swornhero.steward.gui;

import com.swornhero.steward.freeze.FreezeHistoryEntry;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class FreezeHistoryDetailScreen {
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm:ss a"
                    )
                    .withZone(ZoneId.systemDefault());

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
        SimpleContainer container =
                new SimpleContainer(
                        FreezeHistoryDetailMenu.MENU_SIZE
                );

        populate(container, entry);

        Component title = Component.literal(
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
                                        entry
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            FreezeHistoryEntry entry
    ) {
        addBorder(container);

        long durationSeconds =
                Duration.between(
                        entry.frozenAt(),
                        entry.unfrozenAt()
                ).getSeconds();

        setButton(
                container,
                10,
                Items.PLAYER_HEAD,
                "Player: " + entry.targetName()
        );

        setButton(
                container,
                11,
                Items.NAME_TAG,
                "Case ID: "
                        + entry.freezeId()
                        .toString()
        );

        setButton(
                container,
                12,
                Items.WRITABLE_BOOK,
                "Reason: " + entry.reason()
        );

        setButton(
                container,
                14,
                Items.PACKED_ICE,
                "Frozen By: " + entry.frozenByName()
        );

        setButton(
                container,
                16,
                Items.MAGMA_CREAM,
                "Unfrozen By: " + entry.unfrozenByName()
        );

        setButton(
                container,
                20,
                Items.CLOCK,
                "Started: "
                        + DATE_FORMAT.format(entry.frozenAt())
        );

        setButton(
                container,
                22,
                Items.RECOVERY_COMPASS,
                "Ended: "
                        + DATE_FORMAT.format(entry.unfrozenAt())
        );

        setButton(
                container,
                24,
                Items.COMPASS,
                "Duration: "
                        + formatDuration(durationSeconds)
        );

        setButton(
                container,
                30,
                Items.ENDER_EYE,
                "Dimension: " + entry.dimension()
        );

        setButton(
                container,
                31,
                Items.COMPASS,
                "X: " + formatCoordinate(entry.x())
        );

        setButton(
                container,
                32,
                Items.COMPASS,
                "Y: " + formatCoordinate(entry.y())
        );

        setButton(
                container,
                33,
                Items.COMPASS,
                "Z: " + formatCoordinate(entry.z())
        );

        setButton(
                container,
                38,
                Items.REDSTONE_TORCH,
                "Disconnects: "
                        + entry.disconnectCount()
        );

        setButton(
                container,
                42,
                Items.LEVER,
                "Reconnects: "
                        + entry.reconnectCount()
        );

        setButton(
                container,
                FreezeHistoryDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History"
        );

        setButton(
                container,
                FreezeHistoryDetailMenu.PROFILE_SLOT,
                Items.PLAYER_HEAD,
                "Player Profile"
        );

        setButton(
                container,
                FreezeHistoryDetailMenu.CLOSE_SLOT,
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
             slot < FreezeHistoryDetailMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == FreezeHistoryDetailMenu.ROWS - 1
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
        ItemStack stack = new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        container.setItem(slot, stack);
    }

    private static String formatDuration(
            long durationSeconds
    ) {
        long hours = durationSeconds / 3600;
        long minutes = (durationSeconds % 3600) / 60;
        long seconds = durationSeconds % 60;

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
        return String.format("%.2f", coordinate);
    }
}