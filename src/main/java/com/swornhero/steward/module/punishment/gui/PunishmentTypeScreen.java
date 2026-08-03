package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.LinkedHashSet;
import java.util.UUID;

public final class PunishmentTypeScreen {

    private PunishmentTypeScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_MANAGE
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
                        PunishmentTypeMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString()
        );

        Component title =
                Component.literal(
                        "Punish • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentTypeMenu(
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
                PunishmentTypeMenu.MUTE_SLOT,
                Items.NAME_TAG,
                "Mute Player"
        );

        setButton(
                container,
                PunishmentTypeMenu.KICK_SLOT,
                Items.LEATHER_BOOTS,
                "Kick Player"
        );

        setButton(
                container,
                PunishmentTypeMenu.TEMPORARY_BAN_SLOT,
                Items.CLOCK,
                "Temporary Ban"
        );

        setButton(
                container,
                PunishmentTypeMenu.PERMANENT_BAN_SLOT,
                Items.BARRIER,
                "Permanent Ban"
        );

        setButton(
                container,
                PunishmentTypeMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Player Profile"
        );

        setButton(
                container,
                PunishmentTypeMenu.CLOSE_SLOT,
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
             slot < PunishmentTypeMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentTypeMenu.ROWS - 1
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

        LinkedHashSet<DataComponentType<?>>
                hiddenComponents =
                new LinkedHashSet<>();

        hiddenComponents.add(
                DataComponents.ATTRIBUTE_MODIFIERS
        );

        stack.set(
                DataComponents.TOOLTIP_DISPLAY,
                new TooltipDisplay(
                        false,
                        hiddenComponents
                )
        );

        container.setItem(
                slot,
                stack
        );
    }
}