package com.swornhero.steward.module.vanish.service;

import com.swornhero.steward.core.permission.StewardPermissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class VanishConnectionService {
    private VanishConnectionService() {
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayer player = handler.player;
            // Run after the connection callback so vanilla's initial player-list update
            // cannot overwrite Steward's per-viewer tab-list policy.
            server.execute(() -> {
                if (VanishService.isVanished(player)) {
                    VanishService.apply(player);
                    player.sendSystemMessage(Component.literal("[Steward] Persistent vanish reapplied."));
                    notifyReconnect(player);
                }
                VanishService.refreshViewer(player);
            });
        });
    }

    private static void notifyReconnect(ServerPlayer vanished) {
        if (vanished.getServer() == null) {
            return;
        }
        Component message = Component.literal("[Steward] "
                + vanished.getName().getString() + " reconnected while vanished.");
        for (ServerPlayer viewer : vanished.getServer().getPlayerList().getPlayers()) {
            if (viewer != vanished
                    && StewardPermissions.has(viewer, StewardPermissions.VANISH_NOTIFICATIONS)) {
                viewer.sendSystemMessage(message);
            }
        }
    }
}
