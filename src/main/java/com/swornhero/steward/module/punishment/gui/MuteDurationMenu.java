package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentDuration;
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

public final class MuteDurationMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int ONE_HOUR_SLOT = 19;
    public static final int SIX_HOURS_SLOT = 20;
    public static final int ONE_DAY_SLOT = 21;
    public static final int THREE_DAYS_SLOT = 22;
    public static final int SEVEN_DAYS_SLOT = 23;
    public static final int FOURTEEN_DAYS_SLOT = 24;
    public static final int THIRTY_DAYS_SLOT = 25;

    public static final int BACK_SLOT = 49;
    public static final int CLOSE_SLOT = 50;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;

    public MuteDurationMenu(
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

    public MuteDurationMenu(
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

        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_MUTE
        )) {
            viewer.closeContainer();
            return;
        }

        if (slotId < 0 || slotId >= MENU_SIZE) {
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

        PunishmentDuration duration =
                durationFromSlot(slotId);

        if (duration != null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "Mute reason selection for "
                                    + duration.displayName()
                                    + " will be added next."
                    )
            );

            return;
        }

        switch (slotId) {
            case BACK_SLOT ->
                    PunishmentTypeScreen.open(
                            viewer,
                            targetUuid,
                            browserPage
                    );

            case CLOSE_SLOT ->
                    viewer.closeContainer();

            default -> {
                // Border or empty slot.
            }
        }
    }

    private static PunishmentDuration durationFromSlot(
            int slot
    ) {
        return switch (slot) {
            case ONE_HOUR_SLOT ->
                    PunishmentDuration.ONE_HOUR;

            case SIX_HOURS_SLOT ->
                    PunishmentDuration.SIX_HOURS;

            case ONE_DAY_SLOT ->
                    PunishmentDuration.ONE_DAY;

            case THREE_DAYS_SLOT ->
                    PunishmentDuration.THREE_DAYS;

            case SEVEN_DAYS_SLOT ->
                    PunishmentDuration.SEVEN_DAYS;

            case FOURTEEN_DAYS_SLOT ->
                    PunishmentDuration.FOURTEEN_DAYS;

            case THIRTY_DAYS_SLOT ->
                    PunishmentDuration.THIRTY_DAYS;

            default -> null;
        };
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