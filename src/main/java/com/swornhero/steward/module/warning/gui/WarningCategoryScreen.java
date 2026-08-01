package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningCategory;
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

public final class WarningCategoryScreen {

    private WarningCategoryScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel
    ) {
        if (warningLevel == null) {
            WarningLevelScreen.open(
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
                        WarningCategoryMenu.MENU_SIZE
                );

        populate(container);

        Component title =
                Component.literal(
                        "Warning Category • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningCategoryMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        warningLevel
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
                WarningCategory.CHAT_MISCONDUCT.slot(),
                Items.WRITABLE_BOOK,
                WarningCategory.CHAT_MISCONDUCT.displayName()
        );

        setButton(
                container,
                WarningCategory.SPAM.slot(),
                Items.PAPER,
                WarningCategory.SPAM.displayName()
        );

        setButton(
                container,
                WarningCategory.HARASSMENT.slot(),
                Items.IRON_SWORD,
                WarningCategory.HARASSMENT.displayName()
        );

        setButton(
                container,
                WarningCategory.PLAYER_DISRESPECT.slot(),
                Items.PLAYER_HEAD,
                WarningCategory.PLAYER_DISRESPECT.displayName()
        );

        setButton(
                container,
                WarningCategory.STAFF_DISRESPECT.slot(),
                Items.SHIELD,
                WarningCategory.STAFF_DISRESPECT.displayName()
        );

        setButton(
                container,
                WarningCategory.GRIEFING.slot(),
                Items.TNT,
                WarningCategory.GRIEFING.displayName()
        );

        setButton(
                container,
                WarningCategory.STEALING.slot(),
                Items.CHEST,
                WarningCategory.STEALING.displayName()
        );

        setButton(
                container,
                WarningCategory.TRESPASSING.slot(),
                Items.OAK_DOOR,
                WarningCategory.TRESPASSING.displayName()
        );

        setButton(
                container,
                WarningCategory.EXPLOITING.slot(),
                Items.COMMAND_BLOCK,
                WarningCategory.EXPLOITING.displayName()
        );

        setButton(
                container,
                WarningCategory.INAPPROPRIATE_CONTENT.slot(),
                Items.PAINTING,
                WarningCategory.INAPPROPRIATE_CONTENT.displayName()
        );

        setButton(
                container,
                WarningCategory.ADVERTISING.slot(),
                Items.OAK_SIGN,
                WarningCategory.ADVERTISING.displayName()
        );

        setButton(
                container,
                WarningCategory.FAILURE_TO_FOLLOW_STAFF.slot(),
                Items.BELL,
                WarningCategory.FAILURE_TO_FOLLOW_STAFF.displayName()
        );

        setButton(
                container,
                WarningCategory.OTHER.slot(),
                Items.NAME_TAG,
                WarningCategory.OTHER.displayName()
        );

        setButton(
                container,
                WarningCategoryMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Warning Levels"
        );

        setButton(
                container,
                WarningCategoryMenu.CLOSE_SLOT,
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
             slot < WarningCategoryMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningCategoryMenu.ROWS - 1
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