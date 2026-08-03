package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.MuteReason;
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

public final class MuteConfirmScreen {

    private MuteConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            PunishmentDuration duration,
            MuteReason muteReason
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_MUTE
        )) {
            return;
        }

        if (duration == null
                || duration.isPermanent()) {

            MuteDurationScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        if (muteReason == null) {
            MuteReasonScreen.open(
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

            PlayerBrowserScreen.open(
                    viewer,
                    browserPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        MuteConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString(),
                duration,
                muteReason
        );

        Component title =
                Component.literal(
                        "Confirm Mute • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new MuteConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        duration,
                                        muteReason
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            String targetName,
            PunishmentDuration duration,
            MuteReason muteReason
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
                Items.NAME_TAG,
                "Punishment: Mute"
        );

        setButton(
                container,
                21,
                Items.PAPER,
                "Reason: "
                        + muteReason.displayName()
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
                "The player will be unable to send chat messages."
        );

        setButton(
                container,
                MuteConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Mute"
        );

        setButton(
                container,
                MuteConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Mute Reasons"
        );

        setButton(
                container,
                MuteConfirmMenu.CANCEL_SLOT,
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
             slot < MuteConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == MuteConfirmMenu.ROWS - 1
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