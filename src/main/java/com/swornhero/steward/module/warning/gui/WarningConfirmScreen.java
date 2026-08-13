package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.module.warning.model.WarningCategory;
import com.swornhero.steward.module.warning.model.WarningExpiration;
import com.swornhero.steward.module.warning.model.WarningLevel;
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
import java.util.UUID;

public final class WarningConfirmScreen {

    private WarningConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel,
            WarningCategory warningCategory,
            String warningReason,
            WarningExpiration warningExpiration
    ) {
        open(
                viewer,
                new WarningDraft(
                        targetUuid,
                        browserPage,
                        warningLevel,
                        warningCategory,
                        warningReason,
                        warningExpiration,
                        null,
                        null
                )
        );
    }

    public static void open(
            ServerPlayer viewer,
            WarningDraft draft
    ) {
        UUID targetUuid = draft.targetUuid();
        int browserPage = draft.browserPage();
        WarningLevel warningLevel = draft.level();
        WarningCategory warningCategory = draft.category();
        String warningReason = draft.reason();
        WarningExpiration warningExpiration = draft.expiration();

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

        if (warningExpiration == null) {
            WarningExpirationScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    warningLevel,
                    warningCategory,
                    warningReason
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
                        WarningConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                target.getName().getString(),
                warningLevel,
                warningCategory,
                warningReason,
                warningExpiration,
                draft.staffNotes(),
                draft.evidenceReference()
        );

        Component title =
                Component.literal(
                        "Confirm Warning • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        warningLevel,
                                        warningCategory,
                                        warningReason,
                                        warningExpiration,
                                        draft.staffNotes(),
                                        draft.evidenceReference()
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            String targetName,
            WarningLevel warningLevel,
            WarningCategory warningCategory,
            String warningReason,
            WarningExpiration warningExpiration,
            String staffNotes,
            String evidenceReference
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
                29,
                Items.WRITTEN_BOOK,
                staffNotes == null
                        ? "Staff Notes: None"
                        : "Staff Notes: " + staffNotes
        );

        setButton(
                container,
                33,
                Items.SPYGLASS,
                evidenceReference == null
                        ? "Evidence: None"
                        : "Evidence: " + evidenceReference
        );

        setButton(
                container,
                19,
                Items.PAPER,
                "Level: "
                        + warningLevel.displayName()
        );

        setButton(
                container,
                20,
                Items.WRITABLE_BOOK,
                "Category: "
                        + warningCategory.displayName()
        );

        setButton(
                container,
                21,
                Items.NAME_TAG,
                "Reason: "
                        + warningReason
        );

        setButton(
                container,
                23,
                Items.CLOCK,
                "Expiration: "
                        + warningExpiration
                        .resolvedDisplayName(
                                warningLevel
                        )
        );

        setButton(
                container,
                WarningConfirmMenu.CONFIRM_SLOT,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "lime_concrete"
                        )
                ),
                "Confirm Warning"
        );

        setButton(
                container,
                WarningConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Notes and Evidence"
        );

        setButton(
                container,
                WarningConfirmMenu.CANCEL_SLOT,
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
             slot < WarningConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningConfirmMenu.ROWS - 1
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
