package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentDuration;
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

public final class MuteDurationScreen {

    private MuteDurationScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_MUTE
        )) {
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
                        MuteDurationMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString()
        );

        Component title =
                Component.literal(
                        "Mute Duration • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new MuteDurationMenu(
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
            String targetName
    ) {
        addBorder(container);

        setButton(
                container,
                13,
                Items.PLAYER_HEAD,
                "Target: " + targetName
        );

        setButton(
                container,
                MuteDurationMenu.ONE_HOUR_SLOT,
                Items.CLOCK,
                PunishmentDuration.ONE_HOUR.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.SIX_HOURS_SLOT,
                Items.CLOCK,
                PunishmentDuration.SIX_HOURS.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.ONE_DAY_SLOT,
                Items.CLOCK,
                PunishmentDuration.ONE_DAY.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.THREE_DAYS_SLOT,
                Items.CLOCK,
                PunishmentDuration.THREE_DAYS.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.SEVEN_DAYS_SLOT,
                Items.CLOCK,
                PunishmentDuration.SEVEN_DAYS.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.FOURTEEN_DAYS_SLOT,
                Items.CLOCK,
                PunishmentDuration.FOURTEEN_DAYS.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.THIRTY_DAYS_SLOT,
                Items.CLOCK,
                PunishmentDuration.THIRTY_DAYS.displayName()
        );

        setButton(
                container,
                MuteDurationMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Punishment Types"
        );

        setButton(
                container,
                MuteDurationMenu.CLOSE_SLOT,
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
             slot < MuteDurationMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == MuteDurationMenu.ROWS - 1
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