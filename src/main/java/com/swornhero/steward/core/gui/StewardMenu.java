package com.swornhero.steward.core.gui;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Read-only 9x6 navigation menu. Item movement is never delegated to
 * vanilla; subclasses only receive clicks on Steward's 54 menu slots.
 */
public abstract class StewardMenu extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 47;
    public static final int BACK_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 51;
    public static final int CLOSE_SLOT = 53;

    private final Container menuContainer;

    protected StewardMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer
    ) {
        super(MenuType.GENERIC_9x6, containerId);

        checkContainerSize(menuContainer, MENU_SIZE);

        this.menuContainer = menuContainer;
        this.menuContainer.startOpen(playerInventory.player);

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    protected abstract void onSlotClicked(
            ServerPlayer viewer,
            int slot
    );

    private void addMenuSlots(Container container) {
        for (int row = 0; row < ROWS; row++) {
            for (int column = 0; column < 9; column++) {
                int slotIndex = column + row * 9;

                addSlot(
                        new Slot(
                                container,
                                slotIndex,
                                8 + column * 18,
                                18 + row * 18
                        ) {
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
                addSlot(
                        new Slot(
                                inventory,
                                column + row * 9 + 9,
                                8 + column * 18,
                                inventoryStartY + row * 18
                        )
                );
            }
        }

        for (int column = 0; column < 9; column++) {
            addSlot(
                    new Slot(
                            inventory,
                            column,
                            8 + column * 18,
                            inventoryStartY + 58
                    )
            );
        }
    }

    @Override
    public final void clicked(
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

        onSlotClicked(viewer, slotId);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
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
