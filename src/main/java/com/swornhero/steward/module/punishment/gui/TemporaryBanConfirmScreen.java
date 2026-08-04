package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.BanReason;
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

public final class TemporaryBanConfirmScreen {

    private TemporaryBanConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            PunishmentDuration duration,
            BanReason banReason
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_TEMPORARY_BAN
        )) {
            return;
        }

        if (duration == null
                || duration.isPermanent()) {

            TemporaryBanDurationScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        if (banReason == null) {
            TemporaryBanReasonScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    duration
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
                            "That player is no longer online."
                    )
            );

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        TemporaryBanConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString(),
                duration,
                banReason
        );

        Component title =
                Component.literal(
                        "Confirm Temporary Ban • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new TemporaryBanConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        duration,
                                        banReason
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            String targetName,
            PunishmentDuration duration,
            BanReason banReason
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
                20,
                Items.IRON_BARS,
                "Punishment: Temporary Ban"
        );

        setButton(
                container,
                21,
                Items.PAPER,
                "Reason: "
                        + banReason.displayName()
        );

        setButton(
                container,
                23,
                Items.CLOCK,
                "Duration: "
                        + duration.displayName()
        );

        setButton(
                container,
                31,
                Items.WRITABLE_BOOK,
                "The player will be disconnected and unable to rejoin."
        );

        setButton(
                container,
                TemporaryBanConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Temporary Ban"
        );

        setButton(
                container,
                TemporaryBanConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Ban Reasons"
        );

        setButton(
                container,
                TemporaryBanConfirmMenu.CANCEL_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "red_concrete"
                        )
                ),
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
             slot < TemporaryBanConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == TemporaryBanConfirmMenu.ROWS - 1
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