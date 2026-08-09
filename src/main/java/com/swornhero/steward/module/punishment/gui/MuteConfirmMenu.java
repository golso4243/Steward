package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.chat.ClickableRecordId;
import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.MuteReason;
import com.swornhero.steward.module.punishment.model.PunishmentDuration;
import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentType;
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

public final class MuteConfirmMenu
        extends AbstractContainerMenu {

    public static final int ROWS = 6;
    public static final int MENU_SIZE = ROWS * 9;

    public static final int CONFIRM_SLOT = 22;
    public static final int BACK_SLOT = 48;
    public static final int CANCEL_SLOT = 50;

    private final Container menuContainer;
    private final UUID targetUuid;
    private final int browserPage;
    private final PunishmentDuration duration;
    private final MuteReason muteReason;

    private boolean submitted;

    public MuteConfirmMenu(
            int containerId,
            Inventory playerInventory,
            Container menuContainer,
            UUID targetUuid,
            int browserPage,
            PunishmentDuration duration,
            MuteReason muteReason
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
        this.duration = duration;
        this.muteReason = muteReason;
        this.submitted = false;

        this.menuContainer.startOpen(
                playerInventory.player
        );

        addMenuSlots(menuContainer);
        addPlayerInventorySlots(playerInventory);
    }

    public MuteConfirmMenu(
            int containerId,
            Inventory playerInventory
    ) {
        this(
                containerId,
                playerInventory,
                new SimpleContainer(MENU_SIZE),
                new UUID(0L, 0L),
                0,
                PunishmentDuration.ONE_HOUR,
                MuteReason.OTHER
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
            MuteReasonScreen.open(
                    viewer,
                    targetUuid,
                    browserPage,
                    duration
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

        confirmMute(viewer);
    }

    private void confirmMute(
            ServerPlayer viewer
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_MUTE
        )) {
            PunishmentTypeScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        if (targetUuid.equals(viewer.getUUID())) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] You cannot mute yourself."
                    )
            );

            submitted = false;
            return;
        }

        if (duration == null
                || duration.isPermanent()
                || muteReason == null) {

            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] The selected mute is invalid."
                    )
            );

            submitted = false;
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
                            "[Steward] That player is no longer online."
                    )
            );

            PlayerBrowserScreen.open(
                    viewer,
                    browserPage
            );

            return;
        }

        if (!PunishmentService.activePunishmentsFor(
                targetUuid,
                PunishmentType.MUTE
        ).isEmpty()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "[Steward] "
                                    + target.getName().getString()
                                    + " already has an active mute."
                    )
            );

            PlayerProfileScreen.open(
                    viewer,
                    targetUuid,
                    browserPage
            );

            return;
        }

        submitted = true;

        try {
            PunishmentRecord record =
                    PunishmentService.createPunishment(
                            PunishmentType.MUTE,
                            target.getUUID(),
                            target.getName().getString(),
                            viewer.getUUID(),
                            viewer.getName().getString(),
                            muteReason.displayName(),
                            null,
                            null,
                            true,
                            duration
                    );

            String punishmentId =
                    PunishmentService.formatPunishmentId(
                            record.punishmentId()
                    );

            viewer.sendSystemMessage(
                    Component.empty()
                            .append(
                                    ClickableRecordId.create(
                                            punishmentId,
                                            "/steward view punishment "
                                                    + punishmentId,
                                            "Click to view punishment details"
                                    )
                            )
                            .append(
                                    Component.literal(
                                            " issued to "
                                                    + target.getName()
                                                    .getString()
                                                    + ". Mute duration: "
                                                    + duration.displayName()
                                                    + "."
                                    )
                            )
            );

            target.sendSystemMessage(
                    Component.literal(
                            "[Steward] You have been muted for "
                                    + duration.displayName()
                                    + ". Reason: "
                                    + muteReason.displayName()
                                    + ". Punishment ID: "
                                    + punishmentId
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
                            "[Steward] The mute could not be issued."
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