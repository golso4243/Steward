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
import java.util.LinkedHashSet;
import java.util.UUID;

public final class UnfreezeConfirmScreen {

    private UnfreezeConfirmScreen() {
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
                        UnfreezeConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                record
        );

        Component title = Component.literal(
                "Confirm Unfreeze • "
                        + record.targetName()
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new UnfreezeConfirmMenu(
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
            SimpleContainer container,
            FreezeRecord record
    ) {
        addBorder(container);

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
                13,
                Items.PLAYER_HEAD,
                "Unfreeze "
                        + record.targetName()
                        + "?"
        );

        setButton(
                container,
                21,
                Items.WRITABLE_BOOK,
                "Reason: "
                        + record.reason()
        );

        setButton(
                container,
                23,
                Items.PACKED_ICE,
                "Frozen By: "
                        + record.frozenByName()
        );

        setButton(
                container,
                31,
                Items.CLOCK,
                "Frozen For: "
                        + formatDuration(
                        durationSeconds
                )
        );

        setButton(
                container,
                UnfreezeConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Unfreeze"
        );

        setButton(
                container,
                UnfreezeConfirmMenu.CANCEL_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "red_concrete"
                        )
                ),
                "Cancel"
        );

        setButton(
                container,
                UnfreezeConfirmMenu.CLOSE_SLOT,
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
             slot < UnfreezeConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == UnfreezeConfirmMenu.ROWS - 1
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

    private static String formatDuration(
            long totalSeconds
    ) {
        long hours =
                totalSeconds / 3600;

        long minutes =
                (totalSeconds % 3600) / 60;

        long seconds =
                totalSeconds % 60;

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
}