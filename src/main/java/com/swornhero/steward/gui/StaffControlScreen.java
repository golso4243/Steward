package com.swornhero.steward.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.component.TooltipDisplay;

public final class StaffControlScreen {
    private static final Component TITLE =
            Component.literal("Steward • Control Panel");

    private StaffControlScreen() {
        // Utility class
    }

    public static void open(ServerPlayer player) {
        SimpleContainer container =
                new SimpleContainer(StaffControlMenu.MENU_SIZE);

        populate(container);

        player.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) ->
                                new StaffControlMenu(
                                        containerId,
                                        inventory,
                                        container
                                ),
                        TITLE
                )
        );
    }

    private static void populate(SimpleContainer container) {
        addBorder(container);

        setButton(
                container,
                StaffControlAction.STAFF_MODE.slot(),
                Items.ENCHANTED_BOOK,
                "Staff Mode"
        );

        setButton(
                container,
                StaffControlAction.MODE_STYLE.slot(),
                Items.AMETHYST_SHARD,
                "Mode Style"
        );

        setButton(
                container,
                StaffControlAction.VANISH.slot(),
                Items.POTION,
                "Vanish"
        );

        setButton(
                container,
                StaffControlAction.PLAYERS.slot(),
                Items.PLAYER_HEAD,
                "Players"
        );

        setButton(
                container,
                StaffControlAction.TELEPORT.slot(),
                Items.ENDER_PEARL,
                "Teleport Tools"
        );

        setButton(
                container,
                StaffControlAction.FREEZE.slot(),
                Items.PACKED_ICE,
                "Freeze"
        );

        setButton(
                container,
                StaffControlAction.REPORTS.slot(),
                Items.WRITABLE_BOOK,
                "Reports"
        );

        setButton(
                container,
                StaffControlAction.NOTES.slot(),
                Items.WRITTEN_BOOK,
                "Staff Notes"
        );

        setButton(
                container,
                StaffControlAction.INSPECTION.slot(),
                Items.SPYGLASS,
                "Inspection"
        );

        setButton(
                container,
                StaffControlAction.HISTORY.slot(),
                Items.CLOCK,
                "History"
        );

        setButton(
                container,
                StaffControlAction.PUNISHMENTS.slot(),
                Items.ANVIL,
                "Punishments"
        );

        setButton(
                container,
                StaffControlAction.STAFF_CHAT.slot(),
                Items.ECHO_SHARD,
                "Staff Chat"
        );

        setButton(
                container,
                StaffControlAction.ALERTS.slot(),
                Items.BELL,
                "Staff Alerts"
        );

        setButton(
                container,
                StaffControlAction.SETTINGS.slot(),
                Items.COMPARATOR,
                "Settings"
        );

        setButton(
                container,
                StaffControlAction.CLOSE.slot(),
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

        for (int slot = 0; slot < StaffControlMenu.MENU_SIZE; slot++) {
            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row == StaffControlMenu.ROWS - 1
                            || column == 0
                            || column == 8;

            if (border) {
                ItemStack pane = new ItemStack(borderItem);

                pane.set(
                        DataComponents.TOOLTIP_DISPLAY,
                        new TooltipDisplay(
                                true,
                                new java.util.LinkedHashSet<>()
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