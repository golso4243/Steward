package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.BanReason;
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

public final class PermanentBanReasonScreen {

    private PermanentBanReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_PERMANENT_BAN
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

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        PermanentBanReasonMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString()
        );

        Component title =
                Component.literal(
                        "Permanent Ban Reason • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PermanentBanReasonMenu(
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
                BanReason.REPEATED_RULE_VIOLATIONS.slot(),
                Items.WRITABLE_BOOK,
                BanReason.REPEATED_RULE_VIOLATIONS.displayName()
        );

        setButton(
                container,
                BanReason.HARASSMENT_OR_ABUSE.slot(),
                Items.BLAZE_POWDER,
                BanReason.HARASSMENT_OR_ABUSE.displayName()
        );

        setButton(
                container,
                BanReason.GRIEFING_OR_THEFT.slot(),
                Items.IRON_PICKAXE,
                BanReason.GRIEFING_OR_THEFT.displayName()
        );

        setButton(
                container,
                BanReason.CHEATING_OR_EXPLOITATION.slot(),
                Items.COMPARATOR,
                BanReason.CHEATING_OR_EXPLOITATION.displayName()
        );

        setButton(
                container,
                BanReason.THREATS_OR_SEVERE_MISCONDUCT.slot(),
                Items.TNT,
                BanReason.THREATS_OR_SEVERE_MISCONDUCT.displayName()
        );

        setButton(
                container,
                BanReason.EVADING_MODERATION.slot(),
                Items.ENDER_PEARL,
                BanReason.EVADING_MODERATION.displayName()
        );

        setButton(
                container,
                BanReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.slot(),
                Items.BELL,
                BanReason.FAILURE_TO_FOLLOW_STAFF_DIRECTION.displayName()
        );

        setButton(
                container,
                BanReason.OTHER.slot(),
                Items.NAME_TAG,
                BanReason.OTHER.displayName()
        );

        setButton(
                container,
                40,
                Items.BARRIER,
                "Duration: Permanent"
        );

        setButton(
                container,
                PermanentBanReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Punishment Types"
        );

        setButton(
                container,
                PermanentBanReasonMenu.CLOSE_SLOT,
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
             slot < PermanentBanReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PermanentBanReasonMenu.ROWS - 1
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