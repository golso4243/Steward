package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.service.PunishmentService;
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

public final class PunishmentHubScreen {

    private PunishmentHubScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_VIEW
        )) {
            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        PunishmentHubMenu.MENU_SIZE
                );

        populate(container);

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentHubMenu(
                                        containerId,
                                        inventory,
                                        container
                                ),
                        Component.literal(
                                "Steward • Punishments"
                        )
                )
        );
    }

    private static void populate(
            SimpleContainer container
    ) {
        addBorder(container);

        int activeCount =
                PunishmentService.activePunishments()
                        .size();

        int historyCount =
                PunishmentService.allPunishments()
                        .size();

        setButton(
                container,
                13,
                Items.ANVIL,
                "Punishment Management"
        );

        setButton(
                container,
                PunishmentHubMenu.ACTIVE_PUNISHMENTS_SLOT,
                Items.REDSTONE_TORCH,
                "Active Punishments • "
                        + activeCount
        );

        setButton(
                container,
                PunishmentHubMenu.PUNISHMENT_HISTORY_SLOT,
                Items.WRITTEN_BOOK,
                "Punishment History • "
                        + historyCount
                        + " Records"
        );

        setButton(
                container,
                PunishmentHubMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Control Panel"
        );

        setButton(
                container,
                PunishmentHubMenu.CLOSE_SLOT,
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
             slot < PunishmentHubMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentHubMenu.ROWS - 1
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