package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.gui.StaffControlScreen;
import com.swornhero.steward.module.staffmode.model.StaffToolAction;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class StaffModeToolInteractionService {

    private static final int PLAYER_BROWSER_SLOT = 0;
    private static final int TELEPORT_SLOT = 1;
    private static final int FREEZE_SLOT = 2;
    private static final int PUNISHMENTS_SLOT = 3;
    private static final int INSPECTION_SLOT = 4;
    private static final int VANISH_SLOT = 5;
    private static final int STAFF_CHAT_SLOT = 6;
    private static final int STAFF_CONTROL_SLOT = 7;
    private static final int EXIT_STAFF_MODE_SLOT = 8;

    private StaffModeToolInteractionService() {
        // Utility class
    }

    public static void register() {
        ItemEvents.USE.register(
                (level, player, hand) -> {
                    if (!(player instanceof ServerPlayer serverPlayer)) {
                        return null;
                    }

                    if (!StaffModeService.isActive(
                            serverPlayer.getUUID()
                    )) {
                        return null;
                    }

                    int selectedSlot =
                            serverPlayer
                                    .getInventory()
                                    .getSelectedSlot();

                    if (selectedSlot == PLAYER_BROWSER_SLOT) {
                        StaffToolSelectionService.clear(
                                serverPlayer.getUUID()
                        );

                        PlayerBrowserScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == TELEPORT_SLOT) {
                        StaffToolSelectionService.setPendingAction(
                                serverPlayer.getUUID(),
                                StaffToolAction.TELEPORT
                        );

                        PlayerBrowserScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == FREEZE_SLOT) {
                        StaffToolSelectionService.setPendingAction(
                                serverPlayer.getUUID(),
                                StaffToolAction.FREEZE
                        );

                        PlayerBrowserScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == PUNISHMENTS_SLOT) {
                        StaffToolSelectionService.setPendingAction(
                                serverPlayer.getUUID(),
                                StaffToolAction.PUNISHMENTS
                        );

                        PlayerBrowserScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == INSPECTION_SLOT) {
                        StaffToolSelectionService.setPendingAction(
                                serverPlayer.getUUID(),
                                StaffToolAction.INSPECTION
                        );

                        PlayerBrowserScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == STAFF_CONTROL_SLOT) {
                        StaffControlScreen.open(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    if (selectedSlot == EXIT_STAFF_MODE_SLOT) {
                        exitStaffMode(
                                serverPlayer
                        );

                        return InteractionResult.SUCCESS;
                    }

                    // Prevent Staff Mode tools from falling through to vanilla behavior.
                    // This also stops unfinished tools, such as the ender pearl,
                    // from being consumed or thrown.
                    return InteractionResult.FAIL;
                }
        );
    }

    private static void exitStaffMode(
            ServerPlayer player
    ) {
        boolean disabled =
                StaffModeService.disable(
                        player
                );

        if (disabled) {
            StaffToolSelectionService.clear(
                    player.getUUID()
            );

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
    }
}
