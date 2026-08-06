package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.history.GlobalHistoryView;
import com.swornhero.steward.core.history.GlobalModerationHistoryScreen;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.history.ModerationHistoryScreen;
import com.swornhero.steward.core.history.PlayerHistoryView;
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

import java.util.UUID;

public final class PunishmentHistoryDetailMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int BACK_SLOT = 49;
    public static final int CLOSE_SLOT = 50;

    private final Container menuContainer;

    private final UUID punishmentId;
    private final UUID targetUuid;
    private final int browserPage;
    private final int historyPage;
    private final HistoryReturnTarget returnTarget;

    public PunishmentHistoryDetailMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID punishmentId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
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
        this.punishmentId = punishmentId;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;
        this.historyPage = historyPage;

        this.returnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget
                        .PUNISHMENT_MODULE_HISTORY;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public PunishmentHistoryDetailMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                new UUID(0L, 0L),
                0,
                0,
                HistoryReturnTarget
                        .PUNISHMENT_MODULE_HISTORY
        );
    }

    private void addMenuSlots(
            Container container
    ) {
        for (int row = 0;
             row < ROWS;
             row++) {

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

        for (int row = 0;
             row < 3;
             row++) {

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

        if (!hasRequiredPermission(viewer)) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

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

            returnToSource(viewer);
            return;
        }

        switch (slotId) {
            case BACK_SLOT ->
                    returnToSource(viewer);

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Detail items are informational only.
            }
        }
    }

    private boolean hasRequiredPermission(
            ServerPlayer viewer
    ) {
        if (returnTarget
                == HistoryReturnTarget
                .PUNISHMENT_MODULE_HISTORY) {

            return StewardPermissions.require(
                    viewer,
                    StewardPermissions.PUNISHMENT_VIEW
            );
        }

        return StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        );
    }

    private void returnToSource(
            ServerPlayer viewer
    ) {
        switch (returnTarget) {
            case PUNISHMENT_MODULE_HISTORY ->
                    PunishmentHistoryScreen.open(
                            viewer,
                            historyPage
                    );

            case ALL_ACTIVITY ->
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            PlayerHistoryView.ALL_ACTIVITY,
                            historyPage
                    );

            case PUNISHMENT_HISTORY ->
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            PlayerHistoryView.PUNISHMENT_HISTORY,
                            historyPage
                    );

            case GLOBAL_ALL_ACTIVITY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.ALL_ACTIVITY,
                            historyPage
                    );

            case GLOBAL_PUNISHMENT_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.PUNISHMENT_HISTORY,
                            historyPage
                    );

            case GLOBAL_WARNING_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.WARNING_HISTORY,
                            historyPage
                    );

            case GLOBAL_FREEZE_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.FREEZE_HISTORY,
                            historyPage
                    );

            case WARNING_HISTORY,
                 FREEZE_HISTORY ->
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            PlayerHistoryView.ALL_ACTIVITY,
                            historyPage
                    );
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