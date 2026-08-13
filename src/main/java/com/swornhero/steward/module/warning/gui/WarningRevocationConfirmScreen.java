package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.model.WarningRevocationReason;
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

public final class WarningRevocationConfirmScreen {

    private WarningRevocationConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID warningId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget,
            WarningRevocationReason revocationReason
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.WARNING_REVOKE
        )) {
            return;
        }

        WarningRecord record =
                WarningService.findById(
                        warningId
                );

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] That warning is no longer active."
                    )
            );

            viewer.closeContainer();
            return;
        }

        if (revocationReason == null) {
            WarningRevocationReasonScreen.open(
                    viewer,
                    warningId,
                    targetUuid,
                    browserPage,
                    historyPage,
                    returnTarget
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        WarningRevocationConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                record,
                revocationReason
        );

        Component title =
                Component.literal(
                        "Confirm Revocation • "
                                + WarningService.formatWarningId(
                                warningId
                        )
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningRevocationConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        warningId,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        returnTarget,
                                        revocationReason
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            WarningRecord record,
            WarningRevocationReason revocationReason
    ) {
        addBorder(container);

        setButton(
                container,
                13,
                Items.PLAYER_HEAD,
                "Target: " + record.targetName()
        );

        setButton(
                container,
                20,
                Items.REDSTONE_TORCH,
                "Warning: "
                        + record.level().displayName()
        );

        setButton(
                container,
                21,
                Items.NAME_TAG,
                "ID: "
                        + WarningService.formatWarningId(
                        record.warningId()
                )
        );

        setButton(
                container,
                23,
                Items.PAPER,
                "Revocation Reason: "
                        + revocationReason.displayName()
        );

        setButton(
                container,
                31,
                Items.WRITABLE_BOOK,
                "This warning will stop contributing active points."
        );

        setButton(
                container,
                WarningRevocationConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Revocation"
        );

        setButton(
                container,
                WarningRevocationConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Revocation Reasons"
        );

        setButton(
                container,
                WarningRevocationConfirmMenu.CANCEL_SLOT,
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
             slot < WarningRevocationConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningRevocationConfirmMenu.ROWS - 1
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