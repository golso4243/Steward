package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.core.gui.PlayerBrowserScreen;
import com.swornhero.steward.core.gui.StaffControlScreen;
import net.fabricmc.fabric.api.event.player.ItemEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class StaffModeToolInteractionService {

    private static final int PLAYER_BROWSER_SLOT = 0;
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

                    return null;
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