package com.swornhero.steward.core.gui;

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

import com.swornhero.steward.module.freeze.service.FreezeService;

import java.util.LinkedHashSet;
import java.util.UUID;

public final class PlayerProfileScreen {
    private PlayerProfileScreen() {
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

            PlayerBrowserScreen.open(viewer, browserPage);
            return;
        }

        SimpleContainer container =
                new SimpleContainer(PlayerProfileMenu.MENU_SIZE);

        populate(container, target);

        Component title = Component.literal(
                "Steward • " + target.getName().getString()
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PlayerProfileMenu(
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
            SimpleContainer container,
            ServerPlayer target
    ) {
        addBorder(container);

        setButton(
                container,
                PlayerProfileAction.PLAYER_INFO.slot(),
                Items.PLAYER_HEAD,
                target.getName().getString()
        );

        setButton(
                container,
                PlayerProfileAction.TELEPORT_TO.slot(),
                Items.ENDER_PEARL,
                "Teleport To Player"
        );

        setButton(
                container,
                PlayerProfileAction.BRING_HERE.slot(),
                Items.ENDER_EYE,
                "Bring Player Here"
        );

        boolean frozen = FreezeService.isFrozen(target);

        setButton(
                container,
                PlayerProfileAction.FREEZE.slot(),
                frozen ? Items.MAGMA_CREAM : Items.PACKED_ICE,
                frozen ? "Unfreeze Player" : "Freeze Player"
        );

        setButton(
                container,
                PlayerProfileAction.WARN.slot(),
                Items.PAPER,
                "Issue Warning"
        );

        setButton(
                container,
                PlayerProfileAction.INSPECT.slot(),
                Items.SPYGLASS,
                "Inspect Inventory"
        );

        setButton(
                container,
                PlayerProfileAction.REPORTS.slot(),
                Items.WRITABLE_BOOK,
                "View Reports"
        );

        setButton(
                container,
                PlayerProfileAction.NOTES.slot(),
                Items.WRITTEN_BOOK,
                "Staff Notes"
        );

        setButton(
                container,
                PlayerProfileAction.HISTORY.slot(),
                Items.CLOCK,
                "Moderation History"
        );


        setButton(
                container,
                PlayerProfileAction.PUNISHMENTS.slot(),
                Items.ANVIL,
                "Punishments"
        );

        setButton(
                container,
                PlayerProfileAction.BACK.slot(),
                Items.OAK_DOOR,
                "Back to Players"
        );

        setButton(
                container,
                PlayerProfileAction.CLOSE.slot(),
                Items.BARRIER,
                "Close"
        );
    }

    private static void addBorder(SimpleContainer container) {
        Item borderItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "purple_stained_glass_pane"
                )
        );

        for (int slot = 0;
             slot < PlayerProfileMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row == PlayerProfileMenu.ROWS - 1
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