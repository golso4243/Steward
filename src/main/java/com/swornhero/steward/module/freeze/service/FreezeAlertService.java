package com.swornhero.steward.module.freeze.service;

import com.swornhero.steward.core.permission.StewardPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class FreezeAlertService {

    private FreezeAlertService() {
        // Utility class
    }

    public static void notifyStaff(
            MinecraftServer server,
            Component message
    ) {
        for (ServerPlayer onlinePlayer
                : server.getPlayerList().getPlayers()) {

            if (!StewardPermissions.has(
                    onlinePlayer,
                    StewardPermissions.FREEZE_ALERTS
            )) {
                continue;
            }

            onlinePlayer.sendSystemMessage(message);
        }
    }

    public static void notifyStaff(
            MinecraftServer server,
            String message
    ) {
        notifyStaff(
                server,
                Component.literal(message)
        );
    }
}