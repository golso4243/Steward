package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningLevel;
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

public final class WarningLevelScreen {

    private WarningLevelScreen() {
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
                        WarningLevelMenu.MENU_SIZE
                );

        populate(container);

        Component title =
                Component.literal(
                        "Warning Level • "
                                + target.getName()
                                .getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningLevelMenu(
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
                WarningLevel.VERBAL.slot(),
                Items.FEATHER,
                WarningLevel.VERBAL.displayName()
        );

        setButton(
                container,
                WarningLevel.FORMAL.slot(),
                Items.PAPER,
                WarningLevel.FORMAL.displayName()
        );

        setButton(
                container,
                WarningLevel.FINAL.slot(),
                Items.REDSTONE_TORCH,
                WarningLevel.FINAL.displayName()
        );

        setButton(
                container,
                WarningLevelMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back"
        );

        setButton(
                container,
                WarningLevelMenu.CLOSE_SLOT,
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
             slot < WarningLevelMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningLevelMenu.ROWS - 1
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
}