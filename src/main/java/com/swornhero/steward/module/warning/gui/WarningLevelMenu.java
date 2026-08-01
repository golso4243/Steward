package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningLevel;
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

public final class WarningLevelMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int BACK_SLOT = 48;
    public static final int CLOSE_SLOT = 50;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;

    public WarningLevelMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage
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
        this.browserPage = browserPage;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public WarningLevelMenu(
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

        if (slotId == BACK_SLOT) {
            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        if (slotId == CLOSE_SLOT) {
            viewer.closeContainer();
            return;
        }

        WarningLevel level =
                WarningLevel.fromSlot(slotId);

        if (level == null) {
            return;
        }

        selectLevel(
                viewer,
                level
        );
    }

    private void selectLevel(
            ServerPlayer viewer,
            WarningLevel level
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.WARNING_ISSUE
        )) {
            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
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

        viewer.sendSystemMessage(
                Component.literal(
                        level.displayName()
                                + " selected for "
                                + target.getName().getString()
                                + "."
                )
        );

        PlayerProfileScreen.open(
                viewer,
                targetUuid,
                browserPage
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