package com.swornhero.steward.module.staffmode.gui;

import com.swornhero.steward.module.staffmode.model.StaffToolAction;
import com.swornhero.steward.module.staffmode.service.StaffToolSelectionService;
import com.swornhero.steward.core.permission.StaffHierarchyService;
import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.gui.PlayerProfileScreen;
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
import com.swornhero.steward.core.permission.StewardPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Relative;
import com.swornhero.steward.module.freeze.service.FreezeService;

import java.util.Set;
import java.util.UUID;

public final class TeleportActionsMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 3;
    public static final int MENU_SIZE = ROWS * 9;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;
    private final TeleportReturnTarget returnTarget;

    public TeleportActionsMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage, TeleportReturnTarget returnTarget
    ) {
        super(
                MenuType.GENERIC_9x3,
                containerId
        );

        checkContainerSize(
                menuContainer,
                MENU_SIZE
        );

        this.menuContainer = menuContainer;
        this.targetUuid = targetUuid;
        this.browserPage = browserPage;
        this.returnTarget = returnTarget;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public TeleportActionsMenu(
            int containerId,
            Inventory playerInventory, TeleportReturnTarget returnTarget
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                TeleportReturnTarget.PLAYER_BROWSER
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
        int inventoryStartY = 86;

        for (int row = 0; row < 3; row++) {
            for (int column = 0;
                 column < 9;
                 column++) {

                int inventorySlot =
                        column + row * 9 + 9;

                int x =
                        8 + column * 18;

                int y =
                        inventoryStartY + row * 18;

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

        TeleportAction action =
                TeleportAction.fromSlot(slotId);

        if (action == null) {
            return;
        }

        switch (action) {
            case TELEPORT_TO ->
                    teleportToPlayer(viewer);

            case BRING_HERE ->
                    bringPlayerHere(viewer);

            case BACK -> {
                if (returnTarget
                        == TeleportReturnTarget.PLAYER_PROFILE) {

                    StaffToolSelectionService.clear(
                            viewer.getUUID()
                    );

                    PlayerProfileScreen.open(
                            viewer,
                            targetUuid,
                            browserPage
                    );

                    return;
                }

                StaffToolSelectionService.setPendingAction(
                        viewer.getUUID(),
                        StaffToolAction.TELEPORT
                );

                PlayerBrowserScreen.open(
                        viewer,
                        browserPage
                );
            }

            case CLOSE -> {
                StaffToolSelectionService.clear(
                        viewer.getUUID()
                );

                viewer.closeContainer();
            }
        }
    }

    private void handleOfflineTarget(
            ServerPlayer viewer
    ) {
        viewer.sendSystemMessage(
                Component.literal(
                        "[Steward] That player is no longer online."
                )
        );

        if (returnTarget
                == TeleportReturnTarget.PLAYER_BROWSER) {

            StaffToolSelectionService.setPendingAction(
                    viewer.getUUID(),
                    StaffToolAction.TELEPORT
            );
        } else {
            StaffToolSelectionService.clear(
                    viewer.getUUID()
            );
        }

        PlayerBrowserScreen.open(
                viewer,
                browserPage
        );
    }

    private void teleportToPlayer(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.TELEPORT_USE
        )) {
            return;
        }

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null) {
            handleOfflineTarget(viewer);
            return;
        }

        if (viewer.getUUID().equals(target.getUUID())) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] You cannot teleport to yourself."
                    )
            );

            return;
        }

        viewer.closeContainer();

        viewer.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        viewer.fallDistance = 0.0F;

        viewer.teleportTo(
                target.level(),
                target.getX(),
                target.getY(),
                target.getZ(),
                Set.<Relative>of(),
                target.getYRot(),
                target.getXRot(),
                false
        );

        StaffToolSelectionService.clear(
                viewer.getUUID()
        );

        viewer.sendSystemMessage(
                Component.literal(
                        "[Steward] Teleported to "
                                + target.getName().getString()
                                + "."
                )
        );
    }

    private void bringPlayerHere(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.TELEPORT_OTHERS
        )) {
            return;
        }

        ServerPlayer target =
                viewer.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null) {
            handleOfflineTarget(viewer);
            return;
        }

        if (viewer.getUUID().equals(target.getUUID())) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] You cannot bring yourself to yourself."
                    )
            );

            return;
        }

        if (FreezeService.isFrozen(target)) {
            boolean relocated =
                    FreezeService.relocateToStaff(
                            viewer,
                            targetUuid,
                            StewardPermissions.TELEPORT_BYPASS_HIERARCHY
                    );

            if (!relocated) {
                return;
            }

            StaffToolSelectionService.clear(
                    viewer.getUUID()
            );

            viewer.closeContainer();
            return;
        }

        if (!StaffHierarchyService.requireCanAct(
                viewer,
                target,
                StewardPermissions.TELEPORT_BYPASS_HIERARCHY
        )) {
            return;
        }

        target.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        target.fallDistance = 0.0F;

        target.teleportTo(
                viewer.level(),
                viewer.getX(),
                viewer.getY(),
                viewer.getZ(),
                Set.<Relative>of(),
                viewer.getYRot(),
                viewer.getXRot(),
                false
        );

        StaffToolSelectionService.clear(
                viewer.getUUID()
        );

        viewer.closeContainer();

        viewer.sendSystemMessage(
                Component.literal(
                        "[Steward] Brought "
                                + target.getName().getString()
                                + " to your location."
                )
        );

        target.sendSystemMessage(
                Component.literal(
                        "[Steward] You were teleported to "
                                + viewer.getName().getString()
                                + "."
                )
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