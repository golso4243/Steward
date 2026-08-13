package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.permission.StaffHierarchyService;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.model.WarningRevocationReason;
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

public final class WarningRevocationConfirmMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int CONFIRM_SLOT = 22;
    public static final int BACK_SLOT = 48;
    public static final int CANCEL_SLOT = 50;

    private final Container menuContainer;
    private final UUID warningId;
    private final UUID targetUuid;
    private final int browserPage;
    private final int historyPage;
    private final HistoryReturnTarget returnTarget;
    private final WarningRevocationReason revocationReason;

    private boolean submitted;

    public WarningRevocationConfirmMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID warningId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget,
            WarningRevocationReason revocationReason
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
        this.warningId = warningId;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;
        this.historyPage = historyPage;

        this.returnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget.WARNING_HISTORY;

        this.revocationReason = revocationReason;
        this.submitted = false;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public WarningRevocationConfirmMenu(
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
                HistoryReturnTarget.WARNING_HISTORY,
                WarningRevocationReason.OTHER
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
            WarningRevocationReasonScreen.open(
                    viewer,
                    warningId,
                    targetUuid,
                    browserPage,
                    historyPage,
                    returnTarget
            );

            return;
        }

        if (slotId == CANCEL_SLOT) {
            viewer.closeContainer();
            return;
        }

        if (slotId != CONFIRM_SLOT || submitted) {
            return;
        }

        confirmRevocation(viewer);
    }

    private void confirmRevocation(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.WARNING_REVOKE
        )) {
            openCurrentDetails(viewer);
            return;
        }

        WarningRecord record =
                WarningService.findById(
                        warningId
                );

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] That warning is no longer active."
                    )
            );

            openCurrentDetails(viewer);
            return;
        }

        if (revocationReason == null) {
            WarningRevocationReasonScreen.open(
                    viewer,
                    warningId,
                    targetUuid,
                    browserPage,
                    historyPage,
                    returnTarget
            );

            return;
        }

        submitted = true;

        MinecraftServer server =
                viewer.level().getServer();

        StaffHierarchyService.checkCanActOnUuid(
                viewer,
                record.targetUuid(),
                StewardPermissions.WARNING_BYPASS_HIERARCHY
        ).whenComplete(
                (hierarchyResult, exception) ->
                        server.execute(
                                () -> {
                                    if (exception != null) {
                                        submitted = false;

                                        Steward.LOGGER.error(
                                                "Could not resolve hierarchy while "
                                                        + "revoking warning {}.",
                                                warningId,
                                                exception
                                        );

                                        viewer.sendSystemMessage(
                                                Component.literal(
                                                        "[Steward] The staff hierarchy "
                                                                + "could not be resolved."
                                                )
                                        );

                                        openCurrentDetails(viewer);
                                        return;
                                    }

                                    if (!hierarchyResult.allowed()) {
                                        submitted = false;

                                        StaffHierarchyService.sendDenial(
                                                viewer,
                                                hierarchyResult
                                        );

                                        openCurrentDetails(viewer);
                                        return;
                                    }

                                    completeRevocation(
                                            viewer,
                                            record
                                    );
                                }
                        )
        );
    }

    private void completeRevocation(
            ServerPlayer viewer,
            WarningRecord record
    ) {
        try {
            boolean revoked =
                    WarningService.revokeWarning(
                            warningId,
                            viewer.getUUID(),
                            viewer.getName().getString(),
                            revocationReason.displayName()
                    );

            if (!revoked) {
                submitted = false;

                viewer.sendSystemMessage(
                        Component.literal(
                                "[Steward] That warning could not be revoked."
                        )
                );

                openCurrentDetails(viewer);
                return;
            }

            String warningDisplayId =
                    WarningService.formatWarningId(
                            warningId
                    );

            viewer.sendSystemMessage(
                    Component.literal(
                            warningDisplayId
                                    + " was revoked for "
                                    + record.targetName()
                                    + " by "
                                    + viewer.getName().getString()
                                    + ". Reason: "
                                    + revocationReason.displayName()
                                    + "."
                    )
            );

            ServerPlayer target =
                    viewer.level()
                            .getServer()
                            .getPlayerList()
                            .getPlayer(
                                    record.targetUuid()
                            );

            if (target != null) {
                target.sendSystemMessage(
                        Component.literal(
                                "[Steward] Your warning was revoked. Reason: "
                                        + revocationReason.displayName()
                                        + ". Warning ID: "
                                        + warningDisplayId
                                        + "."
                        )
                );
            }

            openCurrentDetails(viewer);
        } catch (RuntimeException exception) {
            submitted = false;

            Steward.LOGGER.error(
                    "[Steward] Could not revoke warning {}.",
                    warningId,
                    exception
            );

            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] The warning could not be revoked."
                    )
            );

            openCurrentDetails(viewer);
        }
    }

    private void openCurrentDetails(
            ServerPlayer viewer
    ) {
        WarningRecord currentRecord =
                WarningService.findById(
                        warningId
                );

        if (currentRecord == null) {
            viewer.closeContainer();
            return;
        }

        WarningHistoryDetailScreen.open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                currentRecord,
                returnTarget
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