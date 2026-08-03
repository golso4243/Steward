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

public final class MuteReasonScreen {

    private MuteReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            PunishmentDuration duration
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
                        MuteReasonMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString(),
                duration
        );

        Component title =
                Component.literal(
                        "Mute Reason • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new MuteReasonMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        duration
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            String targetName,
            PunishmentDuration duration
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
                MuteReason.SPAM_OR_FLOODING.slot(),
                Items.WRITABLE_BOOK,
                MuteReason.SPAM_OR_FLOODING.displayName()
        );

        setButton(
                container,
                MuteReason.HARASSMENT_OR_TOXICITY.slot(),
                Items.BLAZE_POWDER,
                MuteReason.HARASSMENT_OR_TOXICITY.displayName()
        );

        setButton(
                container,
                MuteReason.INAPPROPRIATE_LANGUAGE.slot(),
                Items.PAPER,
                MuteReason.INAPPROPRIATE_LANGUAGE.displayName()
        );

        setButton(
                container,
                MuteReason.ADVERTISING.slot(),
                Items.OAK_SIGN,
                MuteReason.ADVERTISING.displayName()
        );

        setButton(
                container,
                MuteReason.IMPERSONATION.slot(),
                Items.PLAYER_HEAD,
                MuteReason.IMPERSONATION.displayName()
        );

        setButton(
                container,
                MuteReason.SHARING_INAPPROPRIATE_CONTENT.slot(),
                Items.MAP,
                MuteReason.SHARING_INAPPROPRIATE_CONTENT.displayName()
        );

        setButton(
                container,
                MuteReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.slot(),
                Items.BELL,
                MuteReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.displayName()
        );

        setButton(
                container,
                MuteReason.OTHER.slot(),
                Items.NAME_TAG,
                MuteReason.OTHER.displayName()
        );

        setButton(
                container,
                40,
                Items.CLOCK,
                "Duration: " + duration.displayName()
        );

        setButton(
                container,
                MuteReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Mute Durations"
        );

        setButton(
                container,
                MuteReasonMenu.CLOSE_SLOT,
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
             slot < MuteReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == MuteReasonMenu.ROWS - 1
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