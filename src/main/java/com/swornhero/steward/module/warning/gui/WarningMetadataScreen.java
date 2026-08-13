package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningDraft;
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

public final class WarningMetadataScreen {

    private WarningMetadataScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            WarningDraft draft
    ) {
        if (viewer == null || draft == null) {
            return;
        }

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(draft.targetUuid());

        if (target == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] That player is no longer online."
                    )
            );

            PlayerBrowserScreen.open(
                    viewer,
                    draft.browserPage()
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(WarningMetadataMenu.MENU_SIZE);

        populate(container, draft);

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningMetadataMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        draft
                                ),
                        Component.literal(
                                "Warning Details • "
                                        + target.getName().getString()
                        )
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            WarningDraft draft
    ) {
        addBorder(container);

        setButton(
                container,
                WarningMetadataMenu.STAFF_NOTES_SLOT,
                Items.WRITABLE_BOOK,
                draft.staffNotes() == null
                        ? "Add Staff Notes"
                        : "Staff Notes: " + draft.staffNotes()
        );

        setButton(
                container,
                WarningMetadataMenu.EVIDENCE_SLOT,
                Items.SPYGLASS,
                draft.evidenceReference() == null
                        ? "Add Evidence Reference"
                        : "Evidence: " + draft.evidenceReference()
        );

        setButton(
                container,
                WarningMetadataMenu.CONTINUE_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Review Warning"
        );

        if (draft.staffNotes() != null) {
            setButton(
                    container,
                    WarningMetadataMenu.CLEAR_STAFF_NOTES_SLOT,
                    Items.BARRIER,
                    "Clear Staff Notes"
            );
        }

        if (draft.evidenceReference() != null) {
            setButton(
                    container,
                    WarningMetadataMenu.CLEAR_EVIDENCE_SLOT,
                    Items.BARRIER,
                    "Clear Evidence Reference"
            );
        }

        setButton(
                container,
                WarningMetadataMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Expiration"
        );

        setButton(
                container,
                WarningMetadataMenu.CANCEL_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "red_concrete"
                        )
                ),
                "Cancel"
        );
    }

    private static void addBorder(SimpleContainer container) {
        Item borderItem =
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "purple_stained_glass_pane"
                        )
                );

        for (int slot = 0;
             slot < WarningMetadataMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            if (row != 0
                    && row != WarningMetadataMenu.ROWS - 1
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
