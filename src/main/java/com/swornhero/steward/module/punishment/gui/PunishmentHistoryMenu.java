package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.service.PunishmentService;
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

import java.util.Map;
import java.util.UUID;

public final class PunishmentHistoryMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 47;
    public static final int BACK_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 51;
    public static final int CLOSE_SLOT = 53;

    private final Container menuContainer;

    private final int historyPage;
    private final int totalPages;

    private final Map<Integer, UUID> recordSlots;

    public PunishmentHistoryMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            int historyPage,
            int totalPages,
            Map<Integer, UUID> recordSlots
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
        this.historyPage = historyPage;
        this.totalPages = totalPages;
        this.recordSlots =
                Map.copyOf(recordSlots);

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public PunishmentHistoryMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                0,
                1,
                Map.of()
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
                StewardPermissions.PUNISHMENT_VIEW
        )) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        UUID punishmentId =
                recordSlots.get(slotId);

        if (punishmentId != null) {
            PunishmentRecord record =
                    PunishmentService.findById(
                            punishmentId
                    );

            if (record == null) {
                viewer.sendSystemMessage(
                        Component.literal(
                                "That punishment record is no longer available."
                        )
                );

                PunishmentHistoryScreen.open(
                        viewer,
                        historyPage
                );

                return;
            }

            viewer.sendSystemMessage(
                    Component.literal(
                            PunishmentService.formatPunishmentId(
                                    punishmentId
                            )
                                    + " history details will be added next."
                    )
            );

            return;
        }

        switch (slotId) {
            case PREVIOUS_PAGE_SLOT -> {
                if (historyPage > 0) {
                    PunishmentHistoryScreen.open(
                            viewer,
                            historyPage - 1
                    );
                }
            }

            case NEXT_PAGE_SLOT -> {
                if (historyPage + 1 < totalPages) {
                    PunishmentHistoryScreen.open(
                            viewer,
                            historyPage + 1
                    );
                }
            }

            case BACK_SLOT ->
                    PunishmentHubScreen.open(viewer);

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Border, page indicator, or empty slot.
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