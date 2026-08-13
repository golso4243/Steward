package com.swornhero.steward.module.inspection.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StaffHierarchyService;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.staffmode.model.StaffToolAction;
import com.swornhero.steward.module.staffmode.service.StaffToolSelectionService;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.UUID;

public final class InventoryInspectionScreen {

    private static final int HOTBAR_START = 0;
    private static final int HOTBAR_SIZE = 9;
    private static final int MAIN_START = 9;
    private static final int MAIN_SIZE = 27;

    private static final int BOOTS_INDEX = 36;
    private static final int LEGGINGS_INDEX = 37;
    private static final int CHESTPLATE_INDEX = 38;
    private static final int HELMET_INDEX = 39;
    private static final int OFFHAND_INDEX = 40;

    private static final int HELMET_SLOT = 2;
    private static final int CHESTPLATE_SLOT = 3;
    private static final int LEGGINGS_SLOT = 4;
    private static final int BOOTS_SLOT = 5;
    private static final int OFFHAND_SLOT = 6;

    private InventoryInspectionScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            InspectionReturnTarget returnTarget
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.INSPECTION_VIEW
        )) {
            StaffToolSelectionService.clear(
                    viewer.getUUID()
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
                            "[Steward] That player is no longer online."
                    )
            );

            returnToBrowser(
                    viewer,
                    browserPage,
                    returnTarget
            );

            return;
        }

        if (!StaffHierarchyService.requireCanAct(
                viewer,
                target,
                StewardPermissions.INSPECTION_BYPASS_HIERARCHY
        )) {
            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        InventoryInspectionMenu.MENU_SIZE
                );

        populate(
                container,
                target
        );

        Component title =
                Component.literal(
                        "Inspect • "
                                + target.getName().getString()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new InventoryInspectionMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        returnTarget
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            ServerPlayer target
    ) {
        Inventory targetInventory =
                target.getInventory();

        copyRange(
                container,
                targetInventory,
                MAIN_START,
                MAIN_SIZE,
                MAIN_START
        );

        copyRange(
                container,
                targetInventory,
                HOTBAR_START,
                HOTBAR_SIZE,
                36
        );

        copyEquipment(
                container,
                targetInventory,
                HELMET_INDEX,
                HELMET_SLOT,
                "Empty Helmet Slot"
        );

        copyEquipment(
                container,
                targetInventory,
                CHESTPLATE_INDEX,
                CHESTPLATE_SLOT,
                "Empty Chestplate Slot"
        );

        copyEquipment(
                container,
                targetInventory,
                LEGGINGS_INDEX,
                LEGGINGS_SLOT,
                "Empty Leggings Slot"
        );

        copyEquipment(
                container,
                targetInventory,
                BOOTS_INDEX,
                BOOTS_SLOT,
                "Empty Boots Slot"
        );

        copyEquipment(
                container,
                targetInventory,
                OFFHAND_INDEX,
                OFFHAND_SLOT,
                "Empty Offhand Slot"
        );

        setButton(
                container,
                InventoryInspectionMenu.REFRESH_SLOT,
                Items.CLOCK,
                "Refresh Snapshot"
        );

        setButton(
                container,
                InventoryInspectionMenu.BACK_SLOT,
                Items.ARROW,
                "Back"
        );

        setButton(
                container,
                InventoryInspectionMenu.TARGET_INFO_SLOT,
                Items.PLAYER_HEAD,
                target.getName().getString()
                        + " • Read Only"
        );

        setButton(
                container,
                InventoryInspectionMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static void copyRange(
            SimpleContainer container,
            Inventory inventory,
            int sourceStart,
            int length,
            int destinationStart
    ) {
        for (int offset = 0;
             offset < length;
             offset++) {

            int sourceSlot =
                    sourceStart + offset;

            if (sourceSlot
                    >= inventory.getContainerSize()) {
                return;
            }

            container.setItem(
                    destinationStart + offset,
                    inventory.getItem(sourceSlot).copy()
            );
        }
    }

    private static void copyEquipment(
            SimpleContainer container,
            Inventory inventory,
            int sourceSlot,
            int destinationSlot,
            String emptyName
    ) {
        ItemStack stack =
                sourceSlot < inventory.getContainerSize()
                        ? inventory.getItem(sourceSlot).copy()
                        : ItemStack.EMPTY;

        if (!stack.isEmpty()) {
            container.setItem(
                    destinationSlot,
                    stack
            );

            return;
        }

        setButton(
                container,
                destinationSlot,
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "light_gray_stained_glass_pane"
                        )
                ),
                emptyName
        );
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

    private static void returnToBrowser(
            ServerPlayer viewer,
            int browserPage,
            InspectionReturnTarget returnTarget
    ) {
        if (returnTarget
                == InspectionReturnTarget.PLAYER_BROWSER) {

            StaffToolSelectionService.setPendingAction(
                    viewer.getUUID(),
                    StaffToolAction.INSPECTION
            );
        } else {
            StaffToolSelectionService.clear(
                    viewer.getUUID()
            );
        }

        PlayerBrowserScreen.open(
                viewer,
                browserPage
        );
    }
}
