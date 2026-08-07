package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.chat.ClickableRecordId;
import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningCategory;
import com.swornhero.steward.module.warning.model.WarningExpiration;
import com.swornhero.steward.module.warning.model.WarningLevel;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
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

public final class WarningConfirmMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int CONFIRM_SLOT = 22;
    public static final int BACK_SLOT = 48;
    public static final int CANCEL_SLOT = 50;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;
    private final WarningLevel warningLevel;
    private final WarningCategory warningCategory;
    private final String warningReason;
    private final WarningExpiration warningExpiration;

    private boolean submitted;

    public WarningConfirmMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage,
            WarningLevel warningLevel,
            WarningCategory warningCategory,
            String warningReason,
            WarningExpiration warningExpiration
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
        this.warningLevel = warningLevel;
        this.warningCategory = warningCategory;
        this.warningReason = warningReason;
        this.warningExpiration = warningExpiration;
        this.submitted = false;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public WarningConfirmMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                WarningLevel.VERBAL,
                WarningCategory.OTHER,
                "Other: Other documented reason",
                WarningExpiration.DEFAULT
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
            WarningExpirationScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    warningLevel,
                    warningCategory,
                    warningReason
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

        issueWarning(viewer);
    }

    private void issueWarning(
            ServerPlayer viewer
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

        submitted = true;

        try {
            WarningRecord record =
                    WarningService.issueWarning(
                            target.getUUID(),
                            target.getName().getString(),
                            warningLevel,
                            warningCategory,
                            warningReason,
                            viewer.getUUID(),
                            viewer.getName().getString(),
                            null,
                            null,
                            true,
                            warningExpiration
                    );

            String warningId =
                    WarningService.formatWarningId(
                            record.warningId()
                    );

            viewer.sendSystemMessage(
                    Component.empty()
                            .append(
                                    ClickableRecordId.create(
                                            warningId,
                                            "/steward view warning "
                                                    + warningId,
                                            "Click to view warning details"
                                    )
                            )
                            .append(
                                    Component.literal(
                                            " issued to "
                                                    + target.getName()
                                                    .getString()
                                                    + "."
                                    )
                            )
            );

            target.sendSystemMessage(
                    Component.literal(
                            "You received a "
                                    + warningLevel.displayName()
                                    + ". Reason: "
                                    + warningReason
                                    + ". Warning ID: "
                                    + warningId
                                    + "."
                    )
            );

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );
        } catch (RuntimeException exception) {
            submitted = false;

            viewer.sendSystemMessage(
                    Component.literal(
                            "The warning could not be issued."
                    )
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