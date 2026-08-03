package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentRevocationReason;
import com.swornhero.steward.module.punishment.service.PunishmentService;
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

public final class PunishmentRevocationReasonScreen {

    private PunishmentRevocationReasonScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID punishmentId,
            int activePunishmentPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_REVOKE
        )) {
            return;
        }

        PunishmentRecord record =
                PunishmentService.findById(
                        punishmentId
                );

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That punishment is no longer active."
                    )
            );

            ActivePunishmentScreen.open(
                    viewer,
                    activePunishmentPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        PunishmentRevocationReasonMenu.MENU_SIZE
                );

        populate(
                container,
                record
        );

        Component title =
                Component.literal(
                        "Revoke • "
                                + PunishmentService.formatPunishmentId(
                                record.punishmentId()
                        )
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentRevocationReasonMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        punishmentId,
                                        activePunishmentPage
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            PunishmentRecord record
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
                PunishmentRevocationReason.ISSUED_IN_ERROR.slot(),
                Items.BARRIER,
                PunishmentRevocationReason.ISSUED_IN_ERROR.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.INSUFFICIENT_EVIDENCE.slot(),
                Items.PAPER,
                PunishmentRevocationReason.INSUFFICIENT_EVIDENCE.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.SUCCESSFUL_APPEAL.slot(),
                Items.WRITABLE_BOOK,
                PunishmentRevocationReason.SUCCESSFUL_APPEAL.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.STAFF_DISCRETION.slot(),
                Items.NAME_TAG,
                PunishmentRevocationReason.STAFF_DISCRETION.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.REPLACED_BY_ANOTHER_ACTION.slot(),
                Items.ANVIL,
                PunishmentRevocationReason.REPLACED_BY_ANOTHER_ACTION.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.EARLY_RELEASE.slot(),
                Items.IRON_DOOR,
                PunishmentRevocationReason.EARLY_RELEASE.displayName()
        );

        setButton(
                container,
                PunishmentRevocationReason.OTHER.slot(),
                Items.BOOK,
                PunishmentRevocationReason.OTHER.displayName()
        );

        setButton(
                container,
                40,
                Items.REDSTONE_TORCH,
                "Action: Revoke "
                        + record.type().displayName()
        );

        setButton(
                container,
                PunishmentRevocationReasonMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Punishment Details"
        );

        setButton(
                container,
                PunishmentRevocationReasonMenu.CLOSE_SLOT,
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
             slot < PunishmentRevocationReasonMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentRevocationReasonMenu.ROWS - 1
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