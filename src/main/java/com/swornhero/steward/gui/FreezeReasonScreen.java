package com.swornhero.steward.gui;

import com.swornhero.steward.module.freeze.model.FreezeReason;
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
import java.util.UUID;

public final class FreezeReasonScreen {
    private FreezeReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That player is no longer online."
                    )
            );

            PlayerBrowserScreen.open(
                    viewer,
                    browserPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        FreezeReasonMenu.MENU_SIZE
                );

        populate(container);

        Component title = Component.literal(
                "Freeze • "
                        + target.getName().getString()
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new FreezeReasonMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container
    ) {
        addBorder(container);

        setButton(
                container,
                FreezeReason.SUSPECTED_CHEATING.slot(),
                Items.DIAMOND_ORE,
                FreezeReason.SUSPECTED_CHEATING.displayName()
        );

        setButton(
                container,
                FreezeReason.GRIEFING_INVESTIGATION.slot(),
                Items.TNT,
                FreezeReason.GRIEFING_INVESTIGATION.displayName()
        );

        setButton(
                container,
                FreezeReason.THEFT_INVESTIGATION.slot(),
                Items.CHEST,
                FreezeReason.THEFT_INVESTIGATION.displayName()
        );

        setButton(
                container,
                FreezeReason.PLAYER_REPORT.slot(),
                Items.WRITABLE_BOOK,
                FreezeReason.PLAYER_REPORT.displayName()
        );

        setButton(
                container,
                FreezeReason.INVENTORY_INSPECTION.slot(),
                Items.SPYGLASS,
                FreezeReason.INVENTORY_INSPECTION.displayName()
        );

        setButton(
                container,
                FreezeReason.STAFF_DISCUSSION.slot(),
                Items.ECHO_SHARD,
                FreezeReason.STAFF_DISCUSSION.displayName()
        );

        setButton(
                container,
                FreezeReason.OTHER.slot(),
                Items.PAPER,
                FreezeReason.OTHER.displayName()
        );

        setButton(
                container,
                FreezeReasonMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back"
        );

        setButton(
                container,
                FreezeReasonMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Cancel"
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
             slot < FreezeReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == FreezeReasonMenu.ROWS - 1
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
}