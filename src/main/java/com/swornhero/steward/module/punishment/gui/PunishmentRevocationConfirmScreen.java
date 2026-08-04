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

public final class PunishmentRevocationConfirmScreen {

    private PunishmentRevocationConfirmScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID punishmentId,
            int activePunishmentPage,
            PunishmentRevocationReason revocationReason
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

        if (revocationReason == null) {
            PunishmentRevocationReasonScreen.open(
                    viewer,
                    punishmentId,
                    activePunishmentPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        PunishmentRevocationConfirmMenu.MENU_SIZE
                );

        populate(
                container,
                record,
                revocationReason
        );

        Component title =
                Component.literal(
                        "Confirm Revocation • "
                                + PunishmentService.formatPunishmentId(
                                record.punishmentId()
                        )
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentRevocationConfirmMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        punishmentId,
                                        activePunishmentPage,
                                        revocationReason
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            PunishmentRecord record,
            PunishmentRevocationReason revocationReason
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
                "Punishment: "
                        + record.type().displayName()
        );

        setButton(
                container,
                21,
                Items.NAME_TAG,
                "ID: "
                        + PunishmentService.formatPunishmentId(
                        record.punishmentId()
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
                "This will end the active punishment immediately."
        );

        setButton(
                container,
                PunishmentRevocationConfirmMenu.CONFIRM_SLOT,
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
                PunishmentRevocationConfirmMenu.BACK_SLOT,
                Items.ARROW,
                "Back to Revocation Reasons"
        );

        setButton(
                container,
                PunishmentRevocationConfirmMenu.CANCEL_SLOT,
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
             slot
                     < PunishmentRevocationConfirmMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentRevocationConfirmMenu.ROWS - 1
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