package com.swornhero.steward.core.history;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.warning.service.WarningService;
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

public final class ModerationHistoryHubScreen {

    private ModerationHistoryHubScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
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
                        ModerationHistoryHubMenu.MENU_SIZE
                );

        populate(
                container,
                targetUuid
        );

        Component title =
                Component.literal(
                        "History • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ModerationHistoryHubMenu(
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
            UUID targetUuid
    ) {
        addBorder(container);

        int warningCount =
                WarningService.lifetimeWarningCount(
                        targetUuid
                );

        int freezeCount =
                FreezeHistoryService.countForPlayer(
                        targetUuid
                );

        int totalCount =
                warningCount + freezeCount;

        setButton(
                container,
                13,
                Items.PLAYER_HEAD,
                "Moderation Records: "
                        + totalCount
        );

        setButton(
                container,
                ModerationHistoryHubMenu.ALL_ACTIVITY_SLOT,
                Items.CLOCK,
                "All Activity • "
                        + totalCount
                        + " Records"
        );

        setButton(
                container,
                ModerationHistoryHubMenu.WARNING_HISTORY_SLOT,
                Items.PAPER,
                "Warning History • "
                        + warningCount
                        + " Records"
        );

        setButton(
                container,
                ModerationHistoryHubMenu.FREEZE_HISTORY_SLOT,
                Items.PACKED_ICE,
                "Freeze History • "
                        + freezeCount
                        + " Records"
        );

        setButton(
                container,
                ModerationHistoryHubMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Profile"
        );

        setButton(
                container,
                ModerationHistoryHubMenu.CLOSE_SLOT,
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
             slot < ModerationHistoryHubMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == ModerationHistoryHubMenu.ROWS - 1
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