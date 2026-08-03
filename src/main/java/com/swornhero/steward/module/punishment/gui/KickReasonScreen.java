package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.KickReason;
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

public final class KickReasonScreen {

    private KickReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_KICK
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
                        KickReasonMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString()
        );

        Component title =
                Component.literal(
                        "Kick Reason • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new KickReasonMenu(
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
                KickReason.DISRUPTIVE_BEHAVIOR.slot(),
                Items.BELL,
                KickReason.DISRUPTIVE_BEHAVIOR.displayName()
        );

        setButton(
                container,
                KickReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.slot(),
                Items.COMPASS,
                KickReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.displayName()
        );

        setButton(
                container,
                KickReason.SPAM_OR_CHAT_ABUSE.slot(),
                Items.WRITABLE_BOOK,
                KickReason.SPAM_OR_CHAT_ABUSE.displayName()
        );

        setButton(
                container,
                KickReason.INAPPROPRIATE_CONDUCT.slot(),
                Items.PAPER,
                KickReason.INAPPROPRIATE_CONDUCT.displayName()
        );

        setButton(
                container,
                KickReason.AFK_OR_INACTIVE.slot(),
                Items.CLOCK,
                KickReason.AFK_OR_INACTIVE.displayName()
        );

        setButton(
                container,
                KickReason.SERVER_RULE_VIOLATION.slot(),
                Items.BOOK,
                KickReason.SERVER_RULE_VIOLATION.displayName()
        );

        setButton(
                container,
                KickReason.TECHNICAL_OR_CONNECTION_ISSUE.slot(),
                Items.REDSTONE,
                KickReason.TECHNICAL_OR_CONNECTION_ISSUE.displayName()
        );

        setButton(
                container,
                KickReason.OTHER.slot(),
                Items.NAME_TAG,
                KickReason.OTHER.displayName()
        );

        setButton(
                container,
                KickReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Punishment Types"
        );

        setButton(
                container,
                KickReasonMenu.CLOSE_SLOT,
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
             slot < KickReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == KickReasonMenu.ROWS - 1
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