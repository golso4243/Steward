package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
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

import java.util.UUID;

public final class UnfreezeConfirmMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int CONFIRM_SLOT = 48;
    public static final int CANCEL_SLOT = 50;
    public static final int CLOSE_SLOT = 53;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int returnPage;
    private final UnfreezeReturnTarget returnTarget;

    public UnfreezeConfirmMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int returnPage,
            UnfreezeReturnTarget returnTarget
    ) {
        super(
                MenuType.GENERIC_9x6,
                containerId
        );

        checkContainerSize(
                menuContainer,
                MENU_SIZE
        );

        this.menuContainer = menuContainer;
        this.targetUuid = targetUuid;
        this.returnPage = returnPage;
        this.returnTarget = returnTarget;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public UnfreezeConfirmMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                UnfreezeReturnTarget.ACTIVE_FREEZES
        );
    }

    private void addMenuSlots(
            Container container
    ) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0;
                 column < 9;
                 column++) {

                int slotIndex =
                        column + row * 9;

                int x =
                        8 + column * 18;

                int y =
                        18 + row * 18;

                addSlot(
                        new Slot(
                                container,
                                slotIndex,
                                x,
                                y
                        ) {
                            @Override
                            public boolean mayPickup(
                                    Player player
                            ) {
                                return false;
                            }

                            @Override
                            public boolean mayPlace(
                                    ItemStack stack
                            ) {
                                return false;
                            }
                        }
                );
            }
        }
    }

    private void addPlayerInventorySlots(
            Inventory inventory
    ) {
        int inventoryStartY = 140;

        for (int row = 0; row < 3; row++) {
            for (int column = 0;
                 column < 9;
                 column++) {

                int inventorySlot =
                        column + row * 9 + 9;

                int x =
                        8 + column * 18;

                int y =
                        inventoryStartY
                                + row * 18;

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

        int hotbarY =
                inventoryStartY + 58;

        for (int column = 0;
             column < 9;
             column++) {

            int x =
                    8 + column * 18;

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

        switch (slotId) {
            case CONFIRM_SLOT ->
                    confirmUnfreeze(viewer);

            case CANCEL_SLOT ->
                    returnAfterCancel(viewer);

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Information, border, or empty slot.
            }
        }
    }

    private void returnAfterCancel(
            ServerPlayer viewer
    ) {
        if (returnTarget
                == UnfreezeReturnTarget.PLAYER_PROFILE) {

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    returnPage
            );

            return;
        }

        ActiveFreezeDetailScreen.open(
                viewer,
                targetUuid,
                returnPage
        );
    }

    private void returnAfterSuccess(
            ServerPlayer viewer
    ) {
        if (returnTarget
                == UnfreezeReturnTarget.PLAYER_PROFILE) {

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    returnPage
            );

            return;
        }

        ActiveFreezeScreen.open(
                viewer,
                returnPage
        );
    }

    private void confirmUnfreeze(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.FREEZE_UNFREEZE
        )) {
            return;
        }

        ServerPlayer onlineTarget =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (onlineTarget == null
                && !StewardPermissions.require(
                viewer,
                StewardPermissions
                        .FREEZE_UNFREEZE_OFFLINE
        )) {
            return;
        }

        viewer.sendSystemMessage(
                Component.literal(
                        "[Steward] Checking staff hierarchy..."
                )
        );

        FreezeService.unfreezeAuthorized(
                viewer,
                targetUuid,
                unfrozen -> {
                    if (!unfrozen) {
                        viewer.sendSystemMessage(
                                Component.literal(
                                        "That freeze is no longer active "
                                                + "or the action was denied."
                                )
                        );

                        returnAfterCancel(viewer);
                        return;
                    }

                    returnAfterSuccess(viewer);
                }
        );
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
    public boolean stillValid(
            Player player
    ) {
        return true;
    }

    @Override
    public void removed(
            Player player
    ) {
        super.removed(player);
        menuContainer.stopOpen(player);
    }
}