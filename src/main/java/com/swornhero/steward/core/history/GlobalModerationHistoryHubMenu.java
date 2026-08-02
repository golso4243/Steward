package com.swornhero.steward.core.history;

import com.swornhero.steward.core.gui.StaffControlScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
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

public final class GlobalModerationHistoryHubMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int ALL_ACTIVITY_SLOT = 20;
    public static final int WARNING_HISTORY_SLOT = 22;
    public static final int FREEZE_HISTORY_SLOT = 24;

    public static final int BACK_SLOT = 48;
    public static final int CLOSE_SLOT = 50;

    private final Container menuContainer;

    public GlobalModerationHistoryHubMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer
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

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public GlobalModerationHistoryHubMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE)
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

        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        )) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        switch (slotId) {
            case ALL_ACTIVITY_SLOT ->
                    GlobalModerationHistoryScreen.open(
                            viewer
                    );

            case WARNING_HISTORY_SLOT ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.WARNING_HISTORY
                    );

            case FREEZE_HISTORY_SLOT ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.FREEZE_HISTORY
                    );

            case BACK_SLOT ->
                    StaffControlScreen.open(viewer);

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Border or empty slot.
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