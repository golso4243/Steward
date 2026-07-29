package com.swornhero.steward.module.freeze.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

public final class FreezeHistoryDetailMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int BACK_SLOT = 48;
    public static final int PROFILE_SLOT = 49;
    public static final int CLOSE_SLOT = 50;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;
    private final int historyPage;
    private final FreezeHistoryEntry entry;

    public FreezeHistoryDetailMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            FreezeHistoryEntry entry
    ) {
        super(MenuType.GENERIC_9x6, containerId);

        checkContainerSize(menuContainer, MENU_SIZE);

        this.menuContainer = menuContainer;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;
        this.historyPage = historyPage;
        this.entry = entry;

        this.menuContainer.startOpen(playerInventory.player);

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public FreezeHistoryDetailMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                0,
                null
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
                StewardPermissions.FREEZE_HISTORY
        )) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        switch (slotId) {
            case BACK_SLOT -> {
                FreezeHistoryScreen.open(
                        viewer,
                        targetUuid,
                        browserPage,
                        historyPage
                );
            }

            case PROFILE_SLOT -> {
                PlayerProfileScreen.open(
                        viewer,
                        targetUuid,
                        browserPage
                );
            }

            case CLOSE_SLOT -> viewer.closeContainer();

            default -> {
                // Detail items are informational only.
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