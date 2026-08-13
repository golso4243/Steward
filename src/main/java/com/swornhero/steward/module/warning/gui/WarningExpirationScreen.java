package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningCategory;
import com.swornhero.steward.module.warning.model.WarningExpiration;
import com.swornhero.steward.module.warning.model.WarningLevel;
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

public final class WarningExpirationScreen {

    private WarningExpirationScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel,
            WarningCategory warningCategory,
            String warningReason
    ) {
        open(
                viewer,
                targetUuid,
                browserPage,
                warningLevel,
                warningCategory,
                warningReason,
                null,
                null
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel,
            WarningCategory warningCategory,
            String warningReason,
            String staffNotes,
            String evidenceReference
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

        if (warningReason == null
                || warningReason.isBlank()) {

            WarningReasonScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    warningLevel,
                    warningCategory
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
                        WarningExpirationMenu.MENU_SIZE
                );

        populate(
                container,
                warningLevel
        );

        Component title =
                Component.literal(
                        "Warning Expiration • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningExpirationMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        warningLevel,
                                        warningCategory,
                                        warningReason,
                                        staffNotes,
                                        evidenceReference
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            WarningLevel warningLevel
    ) {
        addBorder(container);

        setButton(
                container,
                WarningExpiration.DEFAULT.slot(),
                Items.CLOCK,
                WarningExpiration.DEFAULT
                        .resolvedDisplayName(
                                warningLevel
                        )
        );

        setButton(
                container,
                WarningExpiration.SEVEN_DAYS.slot(),
                Items.COPPER_INGOT,
                WarningExpiration.SEVEN_DAYS
                        .displayName()
        );

        setButton(
                container,
                WarningExpiration.THIRTY_DAYS.slot(),
                Items.IRON_INGOT,
                WarningExpiration.THIRTY_DAYS
                        .displayName()
        );

        setButton(
                container,
                WarningExpiration.NINETY_DAYS.slot(),
                Items.GOLD_INGOT,
                WarningExpiration.NINETY_DAYS
                        .displayName()
        );

        setButton(
                container,
                WarningExpiration.NEVER.slot(),
                Items.NETHER_STAR,
                WarningExpiration.NEVER
                        .displayName()
        );

        setButton(
                container,
                WarningExpirationMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Reasons"
        );

        setButton(
                container,
                WarningExpirationMenu.CLOSE_SLOT,
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
             slot < WarningExpirationMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningExpirationMenu.ROWS - 1
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
