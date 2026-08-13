package com.swornhero.steward.module.staffmode.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
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

public final class TeleportActionsScreen {

    private TeleportActionsScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        open(
                viewer,
                targetUuid,
                browserPage,
                TeleportReturnTarget.PLAYER_BROWSER
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            TeleportReturnTarget returnTarget
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
                        TeleportActionsMenu.MENU_SIZE
                );

        populate(container);

        Component title = Component.literal(
                "Steward • Teleport • "
                        + target.getName().getString()
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new TeleportActionsMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        returnTarget
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
                TeleportAction.TELEPORT_TO.slot(),
                Items.ENDER_PEARL,
                "Teleport to Player"
        );

        setButton(
                container,
                TeleportAction.BRING_HERE.slot(),
                Items.ENDER_EYE,
                "Bring Player Here"
        );

        setButton(
                container,
                TeleportAction.BACK.slot(),
                Items.OAK_DOOR,
                "Back to Players"
        );

        setButton(
                container,
                TeleportAction.CLOSE.slot(),
                Items.BARRIER,
                "Close"
        );
    }

    private static void addBorder(
            SimpleContainer container
    ) {
        Item borderItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "purple_stained_glass_pane"
                )
        );

        for (int slot = 0;
             slot < TeleportActionsMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row == TeleportActionsMenu.ROWS - 1
                            || column == 0
                            || column == 8;

            if (border) {
                ItemStack pane = new ItemStack(borderItem);

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