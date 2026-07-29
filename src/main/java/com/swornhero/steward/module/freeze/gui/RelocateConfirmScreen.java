package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.module.freeze.service.FreezeService;
import com.swornhero.steward.module.freeze.model.FreezePosition;
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

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.UUID;

public final class RelocateConfirmScreen {

    private RelocateConfirmScreen() {
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

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] Frozen players must be online "
                                    + "before they can be relocated."
                    )
            );

            ActiveFreezeDetailScreen.open(
                    viewer,
                    targetUuid,
                    activeFreezePage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        RelocateConfirmMenu.MENU_SIZE
                );

        populate(
                viewer,
                container,
                record
        );

        Component title =
                Component.literal(
                        "Confirm Relocation • "
                                + record.targetName()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new RelocateConfirmMenu(
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

        setButton(
                container,
                13,
                Items.ENDER_PEARL,
                "Bring "
                        + record.targetName()
                        + " Here?"
        );

        setButton(
                container,
                20,
                Items.COMPASS,
                "Original Freeze Location: "
                        + formatPosition(
                        record.position()
                )
        );

        setButton(
                container,
                22,
                Items.BELL,
                "Warning: Changes Investigation Context"
        );

        setButton(
                container,
                24,
                Items.COMPASS,
                "Current Freeze Anchor: "
                        + formatPosition(
                        record.currentPosition()
                )
        );

        setButton(
                container,
                30,
                Items.WRITABLE_BOOK,
                "New Destination: "
                        + formatPlayerPosition(viewer)
        );

        setButton(
                container,
                31,
                Items.BOOK,
                "The original freeze location "
                        + "will be preserved."
        );

        setButton(
                container,
                32,
                Items.REDSTONE_TORCH,
                "The staff member, time, old anchor, "
                        + "and new anchor will be recorded."
        );

        setButton(
                container,
                RelocateConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Relocation"
        );

        setButton(
                container,
                RelocateConfirmMenu.CANCEL_SLOT,
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
                RelocateConfirmMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static String formatPosition(
            FreezePosition position
    ) {
        return position.dimension()
                .identifier()
                + " ["
                + formatCoordinate(position.x())
                + ", "
                + formatCoordinate(position.y())
                + ", "
                + formatCoordinate(position.z())
                + "]";
    }

    private static String formatPlayerPosition(
            ServerPlayer player
    ) {
        return player.level()
                .dimension()
                .identifier()
                + " ["
                + formatCoordinate(player.getX())
                + ", "
                + formatCoordinate(player.getY())
                + ", "
                + formatCoordinate(player.getZ())
                + "]";
    }

    private static String formatCoordinate(
            double coordinate
    ) {
        return String.format(
                Locale.ROOT,
                "%.2f",
                coordinate
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
             slot < RelocateConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == RelocateConfirmMenu.ROWS - 1
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