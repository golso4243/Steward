package com.swornhero.steward.freeze;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permissions;

public final class FreezeConnectionService {

    private FreezeConnectionService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) ->
                        handleDisconnect(handler.player, server)
        );

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        handleJoin(handler.player, server)
        );
    }

    private static void handleDisconnect(
            ServerPlayer player,
            MinecraftServer server
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        FreezeRecord record =
                FreezeService.getRecord(player);

        if (record != null) {
            record.recordDisconnect();
            FreezeAuditService.recordDisconnect(record);
        }

        String playerName = player.getName().getString();

        Component alert = Component.literal(
                "[Steward] "
                        + playerName
                        + " disconnected while frozen."
        );

        notifyStaff(server, alert);
    }

    private static void handleJoin(
            ServerPlayer player,
            MinecraftServer server
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        FreezeRecord record =
                FreezeService.getRecord(player);

        if (record != null) {
            record.recordReconnect();
            FreezeAuditService.recordReconnect(record);
        }

        player.setDeltaMovement(0.0D, 0.0D, 0.0D);
        player.fallDistance = 0.0F;

        player.sendSystemMessage(
                Component.literal(
                        "You are still frozen. "
                                + "Please wait for a staff member."
                )
        );

        Component alert = Component.literal(
                "[Steward] "
                        + player.getName().getString()
                        + " reconnected while still frozen."
        );

        notifyStaff(server, alert);
    }

    private static void notifyStaff(
            MinecraftServer server,
            Component message
    ) {
        for (ServerPlayer onlinePlayer
                : server.getPlayerList().getPlayers()) {

            if (canReceiveStaffAlerts(onlinePlayer)) {
                onlinePlayer.sendSystemMessage(message);
            }
        }
    }

    private static boolean canReceiveStaffAlerts(
            ServerPlayer player
    ) {
        return player.permissions().hasPermission(
                Permissions.COMMANDS_MODERATOR
        );
    }
}