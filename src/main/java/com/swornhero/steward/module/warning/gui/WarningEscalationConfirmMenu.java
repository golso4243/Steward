package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.permission.StaffHierarchyService;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
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

public final class WarningEscalationConfirmMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 3;
    public static final int MENU_SIZE = ROWS * 9;
    public static final int CONFIRM_SLOT = 11;
    public static final int BACK_SLOT = 18;
    public static final int CANCEL_SLOT = 26;

    private final Container menuContainer;
    private final UUID warningId;
    private final UUID targetUuid;
    private final int browserPage;
    private final int historyPage;
    private final HistoryReturnTarget returnTarget;
    private boolean submitted;

    public WarningEscalationConfirmMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID warningId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        super(MenuType.GENERIC_9x3, containerId);
        checkContainerSize(menuContainer, MENU_SIZE);

        this.menuContainer = menuContainer;
        this.warningId = warningId;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;
        this.historyPage = historyPage;
        this.returnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget.WARNING_HISTORY;
        this.submitted = false;

        this.menuContainer.startOpen(playerInventory.player);
        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public WarningEscalationConfirmMenu(
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
                HistoryReturnTarget.WARNING_HISTORY
        );
    }

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
        int inventoryStartY = 86;

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
    public void clicked(
            int slotId,
            int button,
            ContainerInput input,
            Player player
    ) {
        if (!(player instanceof ServerPlayer viewer)
                || slotId < 0
                || slotId >= MENU_SIZE) {
            return;
        }

        if (slotId == BACK_SLOT) {
            openCurrentDetails(viewer);
            return;
        }

        if (slotId == CANCEL_SLOT) {
            viewer.closeContainer();
            return;
        }

        if (slotId != CONFIRM_SLOT || submitted) {
            return;
        }

        confirmEscalation(viewer);
    }

    private void confirmEscalation(ServerPlayer viewer) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.WARNING_MANAGE
        )) {
            openCurrentDetails(viewer);
            return;
        }

        WarningRecord record = WarningService.findById(warningId);

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] That warning is no longer active."
                    )
            );
            openCurrentDetails(viewer);
            return;
        }

        submitted = true;
        MinecraftServer server = viewer.level().getServer();

        StaffHierarchyService.checkCanActOnUuid(
                viewer,
                record.targetUuid(),
                StewardPermissions.WARNING_BYPASS_HIERARCHY
        ).whenComplete(
                (result, exception) ->
                        server.execute(
                                () -> completeHierarchyCheck(
                                        viewer,
                                        result,
                                        exception
                                )
                        )
        );
    }

    private void completeHierarchyCheck(
            ServerPlayer viewer,
            com.swornhero.steward.core.permission.HierarchyResult result,
            Throwable exception
    ) {
        if (exception != null) {
            submitted = false;
            Steward.LOGGER.error(
                    "Could not resolve hierarchy while escalating warning {}.",
                    warningId,
                    exception
            );
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] The staff hierarchy could not be resolved."
                    )
            );
            openCurrentDetails(viewer);
            return;
        }

        if (result == null || !result.allowed()) {
            submitted = false;

            if (result != null) {
                StaffHierarchyService.sendDenial(viewer, result);
            } else {
                viewer.sendSystemMessage(
                        Component.literal(
                                "[Steward] The staff hierarchy could not be resolved."
                        )
                );
            }

            openCurrentDetails(viewer);
            return;
        }

        WarningRecord current = WarningService.findById(warningId);

        if (current == null || !current.isActive()) {
            submitted = false;
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] That warning is no longer active."
                    )
            );
            openCurrentDetails(viewer);
            return;
        }

        if (!WarningService.escalateWarning(
                warningId,
                viewer.getUUID(),
                viewer.getName().getString()
        )) {
            submitted = false;
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] The warning could not be escalated."
                    )
            );
            openCurrentDetails(viewer);
            return;
        }

        viewer.sendSystemMessage(
                Component.literal(
                        "[Steward] "
                                + WarningService.formatWarningId(warningId)
                                + " for "
                                + current.targetName()
                                + " was marked escalated."
                )
        );

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(current.targetUuid());

        if (target != null) {
            target.sendSystemMessage(
                    Component.literal(
                            "[Steward] Your warning "
                                    + WarningService.formatWarningId(warningId)
                                    + " was escalated for further staff action."
                    )
            );
        }

        openCurrentDetails(viewer);
    }

    private void openCurrentDetails(ServerPlayer viewer) {
        WarningRecord current = WarningService.findById(warningId);

        if (current == null) {
            viewer.closeContainer();
            return;
        }

        WarningHistoryDetailScreen.open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                current,
                returnTarget
        );
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
