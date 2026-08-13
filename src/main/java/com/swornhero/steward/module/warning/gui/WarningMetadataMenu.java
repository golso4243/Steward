package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.warning.model.WarningDraft;
import com.swornhero.steward.module.warning.model.WarningDraftInputType;
import com.swornhero.steward.module.warning.service.WarningDraftInputService;
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

public final class WarningMetadataMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int STAFF_NOTES_SLOT = 20;
    public static final int EVIDENCE_SLOT = 22;
    public static final int CONTINUE_SLOT = 24;
    public static final int CLEAR_STAFF_NOTES_SLOT = 29;
    public static final int CLEAR_EVIDENCE_SLOT = 33;
    public static final int BACK_SLOT = 48;
    public static final int CANCEL_SLOT = 50;

    private final Container menuContainer;
    private final WarningDraft draft;

    public WarningMetadataMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            WarningDraft draft
    ) {
        super(MenuType.GENERIC_9x6, containerId);
        checkContainerSize(menuContainer, MENU_SIZE);

        this.menuContainer = menuContainer;
        this.draft = draft;
        this.menuContainer.startOpen(playerInventory.player);

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public WarningMetadataMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new WarningDraft(
                        new UUID(0L, 0L),
                        0,
                        com.swornhero.steward.module.warning.model.WarningLevel.VERBAL,
                        com.swornhero.steward.module.warning.model.WarningCategory.OTHER,
                        "Other documented reason",
                        com.swornhero.steward.module.warning.model.WarningExpiration.DEFAULT,
                        null,
                        null
                )
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

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                int inventorySlot = column + row * 9 + 9;
                addSlot(
                        new Slot(
                                inventory,
                                inventorySlot,
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

        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.WARNING_ISSUE
        )) {
            viewer.closeContainer();
            return;
        }

        switch (slotId) {
            case STAFF_NOTES_SLOT ->
                    WarningDraftInputService.begin(
                            viewer,
                            draft,
                            WarningDraftInputType.STAFF_NOTES
                    );

            case EVIDENCE_SLOT ->
                    WarningDraftInputService.begin(
                            viewer,
                            draft,
                            WarningDraftInputType.EVIDENCE_REFERENCE
                    );

            case CLEAR_STAFF_NOTES_SLOT ->
                    WarningMetadataScreen.open(
                            viewer,
                            draft.withStaffNotes(null)
                    );

            case CLEAR_EVIDENCE_SLOT ->
                    WarningMetadataScreen.open(
                            viewer,
                            draft.withEvidenceReference(null)
                    );

            case CONTINUE_SLOT ->
                    WarningConfirmScreen.open(viewer, draft);

            case BACK_SLOT ->
                    WarningExpirationScreen.open(
                            viewer,
                            draft.targetUuid(),
                            draft.browserPage(),
                            draft.level(),
                            draft.category(),
                            draft.reason(),
                            draft.staffNotes(),
                            draft.evidenceReference()
                    );

            case CANCEL_SLOT -> {
                WarningDraftInputService.clear(viewer.getUUID());
                viewer.closeContainer();
            }

            default -> {
                // Informational or border slot.
            }
        }
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
