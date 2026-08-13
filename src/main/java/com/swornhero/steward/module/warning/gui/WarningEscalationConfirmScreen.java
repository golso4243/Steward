package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.module.warning.model.WarningRecord;
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

public final class WarningEscalationConfirmScreen {

    private WarningEscalationConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            WarningRecord record,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        if (viewer == null || record == null) {
            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        WarningEscalationConfirmMenu.MENU_SIZE
                );

        addBorder(container);
        setButton(
                container,
                13,
                Items.ANVIL,
                "Escalate "
                        + WarningService.formatWarningId(
                        record.warningId()
                )
                        + " for "
                        + record.targetName()
        );
        setButton(
                container,
                WarningEscalationConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "orange_concrete"
                        )
                ),
                "Confirm Escalation"
        );
        setButton(
                container,
                WarningEscalationConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Warning"
        );
        setButton(
                container,
                WarningEscalationConfirmMenu.CANCEL_SLOT,
                Items.BARRIER,
                "Cancel"
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningEscalationConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        record.warningId(),
                                        record.targetUuid(),
                                        browserPage,
                                        historyPage,
                                        returnTarget
                                ),
                        Component.literal(
                                "Confirm Warning Escalation"
                        )
                )
        );
    }

    private static void addBorder(SimpleContainer container) {
        Item borderItem =
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "orange_stained_glass_pane"
                        )
                );

        for (int slot = 0;
             slot < WarningEscalationConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            if (row != 0
                    && row != WarningEscalationConfirmMenu.ROWS - 1
                    && column != 0
                    && column != 8) {
                continue;
            }

            ItemStack pane = new ItemStack(borderItem);
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
        ItemStack stack = new ItemStack(item);
        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );
        container.setItem(slot, stack);
    }
}
