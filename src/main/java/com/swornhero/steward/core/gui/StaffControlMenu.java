package com.swornhero.steward.core.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.staffmode.service.StaffModeService;
import com.swornhero.steward.module.freeze.gui.ActiveFreezeScreen;
import com.swornhero.steward.module.punishment.gui.PunishmentHubScreen;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.swornhero.steward.core.history.GlobalModerationHistoryHubScreen;

public final class StaffControlMenu extends AbstractContainerMenu {
    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    private final Container menuContainer;

    public StaffControlMenu(
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

    public StaffControlMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE)
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

        // Main player inventory: 27 slots.
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
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

        // Player hotbar: 9 slots.
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
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Only process Steward's 54 control-panel slots.
        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        StaffControlAction action =
                StaffControlAction.fromSlot(slotId);

        // Borders and empty slots have no assigned action.
        if (action == null) {
            return;
        }

        handleAction(serverPlayer, action);

        /*
         * Do not call super.clicked(...).
         * This keeps all item movement, swapping, dragging,
         * dropping, and duplication behavior blocked.
         */
    }

    private void handleAction(
            ServerPlayer player,
            StaffControlAction action
    ) {
        switch (action) {
            case CLOSE -> player.closeContainer();

            case STAFF_MODE -> toggleStaffMode(player);

            case PLAYERS -> PlayerBrowserScreen.open(player);

            case ACTIVE_FREEZES -> {
                if (!StewardPermissions.require(
                        player,
                        StewardPermissions.FREEZE_MANAGE
                )) {
                    return;
                }

                ActiveFreezeScreen.open(player);
            }

            case HISTORY -> {
                if (!StewardPermissions.require(
                        player,
                        StewardPermissions.HISTORY_VIEW
                )) {
                    return;
                }

                GlobalModerationHistoryHubScreen.open(
                        player
                );
            }

            case PUNISHMENTS -> {
                if (!StewardPermissions.require(
                        player,
                        StewardPermissions.PUNISHMENT_MANAGE
                )) {
                    return;
                }

                PunishmentHubScreen.open(player);
            }

            default -> player.sendSystemMessage(
                    Component.literal(
                            actionDisplayName(action)
                                    + " is not available yet."
                    )
            );
        }
    }

    private void toggleStaffMode(
            ServerPlayer player
    ) {
        if (!StewardPermissions.require(
                player,
                StewardPermissions.STAFF_MODE_USE
        )) {
            return;
        }

        if (StaffModeService.isActive(
                player.getUUID()
        )) {
            boolean disabled =
                    StaffModeService.disable(
                            player
                    );

            if (disabled) {
                player.sendSystemMessage(
                        Component.literal(
                                "Staff Mode disabled."
                        )
                );

                return;
            }

            player.sendSystemMessage(
                    Component.literal(
                            "Staff Mode could not be disabled safely."
                    )
            );

            return;
        }

        boolean enabled =
                StaffModeService.enable(
                        player
                );

        if (enabled) {
            player.sendSystemMessage(
                    Component.literal(
                            "Staff Mode enabled."
                    )
            );

            return;
        }

        player.sendSystemMessage(
                Component.literal(
                        "Staff Mode could not be enabled."
                )
        );
    }

    private String actionDisplayName(StaffControlAction action) {
        return switch (action) {
            case STAFF_MODE -> "Staff Mode";
            case MODE_STYLE -> "Mode Style";
            case VANISH -> "Vanish";
            case PLAYERS -> "Players";
            case TELEPORT -> "Teleport Tools";
            case ACTIVE_FREEZES -> "Active Freezes";
            case REPORTS -> "Reports";
            case NOTES -> "Staff Notes";
            case INSPECTION -> "Inspection";
            case HISTORY -> "History";
            case PUNISHMENTS -> "Punishments";
            case STAFF_CHAT -> "Staff Chat";
            case ALERTS -> "Staff Alerts";
            case SETTINGS -> "Settings";
            case CLOSE -> "Close";
        };
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