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

public final class WarningRevocationReasonScreen {

    private WarningRevocationReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID warningId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
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

        SimpleContainer container =
                new SimpleContainer(
                        WarningRevocationReasonMenu.MENU_SIZE
                );

        populate(
                container,
                record
        );

        Component title =
                Component.literal(
                        "Revoke • "
                                + WarningService.formatWarningId(
                                record.warningId()
                        )
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningRevocationReasonMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        warningId,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        returnTarget
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            WarningRecord record
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
                WarningRevocationReason.ISSUED_IN_ERROR.slot(),
                Items.BARRIER,
                WarningRevocationReason.ISSUED_IN_ERROR.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.INSUFFICIENT_EVIDENCE.slot(),
                Items.PAPER,
                WarningRevocationReason.INSUFFICIENT_EVIDENCE.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.SUCCESSFUL_APPEAL.slot(),
                Items.WRITABLE_BOOK,
                WarningRevocationReason.SUCCESSFUL_APPEAL.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.STAFF_DISCRETION.slot(),
                Items.NAME_TAG,
                WarningRevocationReason.STAFF_DISCRETION.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.DUPLICATE_WARNING.slot(),
                Items.MAP,
                WarningRevocationReason.DUPLICATE_WARNING.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.REPLACED_BY_ANOTHER_ACTION.slot(),
                Items.ANVIL,
                WarningRevocationReason.REPLACED_BY_ANOTHER_ACTION.displayName()
        );

        setButton(
                container,
                WarningRevocationReason.OTHER.slot(),
                Items.BOOK,
                WarningRevocationReason.OTHER.displayName()
        );

        setButton(
                container,
                40,
                Items.REDSTONE_TORCH,
                "Action: Revoke "
                        + record.level().displayName()
                        + " Warning"
        );

        setButton(
                container,
                WarningRevocationReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Warning Details"
        );

        setButton(
                container,
                WarningRevocationReasonMenu.CLOSE_SLOT,
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
             slot < WarningRevocationReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningRevocationReasonMenu.ROWS - 1
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