package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningCategory;
import com.swornhero.steward.module.warning.model.WarningLevel;
import com.swornhero.steward.module.warning.model.WarningReason;
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

public final class WarningReasonScreen {

    private WarningReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel,
            WarningCategory warningCategory
    ) {
        if (warningLevel == null) {
            WarningLevelScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        if (warningCategory == null) {
            WarningCategoryScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    warningLevel
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
                        WarningReasonMenu.MENU_SIZE
                );

        populate(container);

        Component title =
                Component.literal(
                        "Warning Reason • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningReasonMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        warningLevel,
                                        warningCategory
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container
    ) {
        addBorder(container);

        setButton(
                container,
                WarningReason.FIRST_OFFENSE.slot(),
                Items.FEATHER,
                WarningReason.FIRST_OFFENSE.displayName()
        );

        setButton(
                container,
                WarningReason.REPEATED_OFFENSE.slot(),
                Items.CLOCK,
                WarningReason.REPEATED_OFFENSE.displayName()
        );

        setButton(
                container,
                WarningReason.CONTINUED_AFTER_WARNING.slot(),
                Items.REDSTONE_TORCH,
                WarningReason.CONTINUED_AFTER_WARNING.displayName()
        );

        setButton(
                container,
                WarningReason.IGNORED_STAFF_DIRECTION.slot(),
                Items.BELL,
                WarningReason.IGNORED_STAFF_DIRECTION.displayName()
        );

        setButton(
                container,
                WarningReason.CONFIRMED_PLAYER_REPORT.slot(),
                Items.WRITABLE_BOOK,
                WarningReason.CONFIRMED_PLAYER_REPORT.displayName()
        );

        setButton(
                container,
                WarningReason.DIRECT_STAFF_OBSERVATION.slot(),
                Items.SPYGLASS,
                WarningReason.DIRECT_STAFF_OBSERVATION.displayName()
        );

        setButton(
                container,
                WarningReason.EVIDENCE_REVIEWED.slot(),
                Items.MAP,
                WarningReason.EVIDENCE_REVIEWED.displayName()
        );

        setButton(
                container,
                WarningReason.OTHER.slot(),
                Items.NAME_TAG,
                WarningReason.OTHER.displayName()
        );

        setButton(
                container,
                WarningReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Categories"
        );

        setButton(
                container,
                WarningReasonMenu.CLOSE_SLOT,
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
             slot < WarningReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningReasonMenu.ROWS - 1
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

            container.setItem(slot, pane);
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

        container.setItem(slot, stack);
    }
}