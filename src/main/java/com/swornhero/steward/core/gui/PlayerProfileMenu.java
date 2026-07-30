package com.swornhero.steward.core.gui;

import com.swornhero.steward.module.freeze.gui.UnfreezeConfirmScreen;
import com.swornhero.steward.module.freeze.gui.FreezeHistoryScreen;
import com.swornhero.steward.module.freeze.gui.FreezeReasonScreen;
import com.swornhero.steward.module.freeze.gui.UnfreezeReturnTarget;
import com.swornhero.steward.module.freeze.service.FreezeService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import com.swornhero.steward.core.permission.StewardPermissions;

import java.util.UUID;

public final class PlayerProfileMenu extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;

    public PlayerProfileMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage
    ) {
        super(MenuType.GENERIC_9x6, containerId);

        checkContainerSize(menuContainer, MENU_SIZE);

        this.menuContainer = menuContainer;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;

        this.menuContainer.startOpen(playerInventory.player);

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public PlayerProfileMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0
        );
    }

    private void addMenuSlots(Container container) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9;
                int x = 8 + column * 18;
                int y = 18 + row * 18;

                addSlot(
                        new Slot(container, slotIndex, x, y) {
                            @Override
                            public boolean mayPickup(Player player) {
                                return false;
                            }

                            @Override
                            public boolean mayPlace(ItemStack stack) {
                                return false;
                            }
                        }
                );
            }
        }
    }

    private void addPlayerInventorySlots(Inventory inventory) {
        int inventoryStartY = 140;

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot =
                        column + row * 9 + 9;

                int x = 8 + column * 18;
                int y = inventoryStartY + row * 18;

                addSlot(
                        new Slot(
                                inventory,
                                inventorySlot,
                                x,
                                y
                        )
                );
            }
        }

        int hotbarY = inventoryStartY + 58;

        for (int column = 0; column < 9; column++) {
            int x = 8 + column * 18;

            addSlot(
                    new Slot(
                            inventory,
                            column,
                            x,
                            hotbarY
                    )
            );
        }
    }

    @Override
    public void clicked(
            int slotId,
            int button,
            ContainerInput input,
            Player player
    ) {
        if (!(player instanceof ServerPlayer viewer)) {
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        PlayerProfileAction action =
                PlayerProfileAction.fromSlot(slotId);

        if (action == null) {
            return;
        }

        handleAction(viewer, action);

        /*
         * Never call super.clicked(...).
         * This is a navigation interface, not an inventory.
         */
    }

    private void handleAction(
            ServerPlayer viewer,
            PlayerProfileAction action
    ) {
        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (action == PlayerProfileAction.BACK) {
            PlayerBrowserScreen.open(
                    viewer,
                    browserPage
            );

            return;
        }

        if (action == PlayerProfileAction.CLOSE) {
            viewer.closeContainer();
            return;
        }

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

        switch (action) {
            case PLAYER_INFO -> viewer.sendSystemMessage(
                    Component.literal(
                            "Viewing profile for "
                                    + target.getName().getString()
                    )
            );

            case TELEPORT_TO -> viewer.sendSystemMessage(
                    Component.literal(
                            "Teleport to "
                                    + target.getName().getString()
                                    + " will be added later."
                    )
            );

            case BRING_HERE -> viewer.sendSystemMessage(
                    Component.literal(
                            "Bring "
                                    + target.getName().getString()
                                    + " here will be added later."
                    )
            );

            case FREEZE -> {
                if (FreezeService.isFrozen(target)) {
                    if (!StewardPermissions.require(
                            viewer,
                            StewardPermissions.FREEZE_UNFREEZE
                    )) {
                        return;
                    }

                    UnfreezeConfirmScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            UnfreezeReturnTarget.PLAYER_PROFILE
                    );

                    return;
                }

                if (!StewardPermissions.require(
                        viewer,
                        StewardPermissions.FREEZE_USE
                )) {
                    return;
                }

                FreezeReasonScreen.open(
                        viewer,
                        targetUuid,
                        browserPage
                );
            }

            case INSPECT -> viewer.sendSystemMessage(
                    Component.literal(
                            "Inventory inspection selected for "
                                    + target.getName().getString()
                    )
            );

            case REPORTS -> viewer.sendSystemMessage(
                    Component.literal(
                            "Reports selected for "
                                    + target.getName().getString()
                    )
            );

            case NOTES -> viewer.sendSystemMessage(
                    Component.literal(
                            "Staff notes selected for "
                                    + target.getName().getString()
                    )
            );

            case HISTORY -> {
                if (!StewardPermissions.require(
                        viewer,
                        StewardPermissions.FREEZE_HISTORY
                )) {
                    return;
                }

                FreezeHistoryScreen.open(
                        viewer,
                        targetUuid,
                        browserPage
                );
            }

            case PUNISHMENTS -> viewer.sendSystemMessage(
                    Component.literal(
                            "Punishments selected for "
                                    + target.getName().getString()
                    )
            );

            default -> {
                // BACK and CLOSE are handled before this switch.
            }
        }
    }

    @Override
    public ItemStack quickMoveStack(
            Player player,
            int index
    ) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canTakeItemForPickAll(
            ItemStack stack,
            Slot slot
    ) {
        return false;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        menuContainer.stopOpen(player);
    }
}