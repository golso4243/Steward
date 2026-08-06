package com.swornhero.steward.core.history;

import com.swornhero.steward.core.permission.StewardPermissions;
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

public final class GlobalModerationHistoryHubScreen {

    private GlobalModerationHistoryHubScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        )) {
            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        GlobalModerationHistoryHubMenu.MENU_SIZE
                );

        populate(container);

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new GlobalModerationHistoryHubMenu(
                                        containerId,
                                        inventory,
                                        container
                                ),
                        Component.literal(
                                "Steward • Moderation History"
                        )
                )
        );
    }

    private static void populate(
            SimpleContainer container
    ) {
        addBorder(container);

        int totalCount =
                ModerationHistoryService.countAll();

        int warningCount =
                ModerationHistoryService.countAllByType(
                        ModerationActionType.WARNING
                );

        int freezeCount =
                ModerationHistoryService.countAllByType(
                        ModerationActionType.FREEZE
                );

        int punishmentCount =
                ModerationHistoryService.countAllPunishments();

        setButton(
                container,
                13,
                Items.CLOCK,
                "Server-Wide Records • "
                        + totalCount
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.ALL_ACTIVITY_SLOT,
                Items.COMPASS,
                "All Activity • "
                        + totalCount
                        + " Records"
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.WARNING_HISTORY_SLOT,
                Items.PAPER,
                "Warning History • "
                        + warningCount
                        + " Records"
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.FREEZE_HISTORY_SLOT,
                Items.PACKED_ICE,
                "Freeze History • "
                        + freezeCount
                        + " Records"
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.PUNISHMENT_HISTORY_SLOT,
                Items.IRON_SWORD,
                "Punishment History • "
                        + punishmentCount
                        + " Records"
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Control Panel"
        );

        setButton(
                container,
                GlobalModerationHistoryHubMenu.CLOSE_SLOT,
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
             slot < GlobalModerationHistoryHubMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == GlobalModerationHistoryHubMenu.ROWS - 1
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