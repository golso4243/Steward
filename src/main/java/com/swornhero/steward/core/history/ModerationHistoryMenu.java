package com.swornhero.steward.core.history;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.freeze.gui.FreezeHistoryDetailScreen;
import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.warning.gui.WarningHistoryDetailScreen;
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
import com.swornhero.steward.module.punishment.gui.PunishmentHistoryDetailScreen;

import java.util.Map;
import java.util.UUID;

public final class ModerationHistoryMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int PREVIOUS_PAGE_SLOT = 45;
    public static final int PAGE_INFO_SLOT = 47;
    public static final int BACK_SLOT = 49;
    public static final int NEXT_PAGE_SLOT = 51;
    public static final int CLOSE_SLOT = 53;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;
    private final PlayerHistoryView view;
    private final int historyPage;
    private final int totalPages;

    private final Map<Integer, ModerationHistoryItem>
            recordSlots;

    public ModerationHistoryMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage,
            PlayerHistoryView view,
            int historyPage,
            int totalPages,
            Map<Integer, ModerationHistoryItem> recordSlots
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

        this.view =
                view != null
                        ? view
                        : PlayerHistoryView.ALL_ACTIVITY;

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

    public ModerationHistoryMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                PlayerHistoryView.ALL_ACTIVITY,
                0,
                1,
                Map.of()
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

        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        )) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
            return;
        }

        ModerationHistoryItem item =
                recordSlots.get(slotId);

        if (item != null) {
            openRecord(
                    viewer,
                    item
            );

            return;
        }

        switch (slotId) {
            case PREVIOUS_PAGE_SLOT -> {
                if (historyPage > 0) {
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            view,
                            historyPage - 1
                    );
                }
            }

            case NEXT_PAGE_SLOT -> {
                if (historyPage + 1 < totalPages) {
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            view,
                            historyPage + 1
                    );
                }
            }

            case BACK_SLOT ->
                    ModerationHistoryHubScreen.open(
                            viewer,
                            targetUuid,
                            browserPage
                    );

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Border, page indicator, or empty slot.
            }
        }
    }

    private void openRecord(
            ServerPlayer viewer,
            ModerationHistoryItem item
    ) {
        switch (item.type()) {
            case WARNING ->
                    openWarningRecord(
                            viewer,
                            item.recordId()
                    );

            case FREEZE ->
                    openFreezeRecord(
                            viewer,
                            item.recordId()
                    );

            case MUTE,
                 KICK,
                 TEMPORARY_BAN,
                 PERMANENT_BAN ->
                    openPunishmentRecord(
                            viewer,
                            item.recordId()
                    );        }
    }

    private void openWarningRecord(
            ServerPlayer viewer,
            UUID warningId
    ) {
        WarningRecord record =
                WarningService.findById(
                        warningId
                );

        if (record == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That warning record could not be found."
                    )
            );

            reopenCurrentPage(viewer);
            return;
        }

        viewer.sendSystemMessage(
                Component.literal(
                        "Selected "
                                + WarningService.formatWarningId(
                                record.warningId()
                        )
                )
        );

        WarningHistoryDetailScreen.open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                record,
                view.returnTarget()
        );
    }

    private void openFreezeRecord(
            ServerPlayer viewer,
            UUID freezeId
    ) {
        FreezeHistoryEntry matchingEntry =
                FreezeHistoryService
                        .getForPlayer(
                                targetUuid
                        )
                        .stream()
                        .filter(
                                entry ->
                                        freezeId.equals(
                                                entry.freezeId()
                                        )
                        )
                        .findFirst()
                        .orElse(null);

        if (matchingEntry == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That freeze record could not be found."
                    )
            );

            reopenCurrentPage(viewer);
            return;
        }

        FreezeHistoryDetailScreen.open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                matchingEntry,
                view.returnTarget()
        );
    }

    private void openPunishmentRecord(
            ServerPlayer viewer,
            UUID punishmentId
    ) {
        PunishmentHistoryDetailScreen.open(
                viewer,
                punishmentId,
                targetUuid,
                browserPage,
                historyPage,
                view.returnTarget()
        );
    }

    private void reopenCurrentPage(
            ServerPlayer viewer
    ) {
        ModerationHistoryScreen.open(
                viewer,
                targetUuid,
                browserPage,
                view,
                historyPage
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