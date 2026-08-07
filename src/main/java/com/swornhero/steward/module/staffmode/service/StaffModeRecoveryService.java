package com.swornhero.steward.module.staffmode.service;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class StaffModeRecoveryService {

    private StaffModeRecoveryService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        recoverIfNeeded(
                                handler.player
                        )
        );
    }

    private static void recoverIfNeeded(
            ServerPlayer player
    ) {
        if (!StaffModeService.needsRecovery(
                player.getUUID()
        )) {
            return;
        }

        boolean recovered =
                StaffModeService.recover(
                        player
                );

        if (recovered) {
            player.sendSystemMessage(
                    Component.literal(
                            "Steward restored your inventory from an interrupted Staff Mode session."
                    )
            );

            return;
        }

        player.sendSystemMessage(
                Component.literal(
                        "Steward could not safely restore your Staff Mode inventory."
                )
        );
    }
}