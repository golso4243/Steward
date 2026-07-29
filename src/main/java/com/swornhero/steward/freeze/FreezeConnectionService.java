package com.swornhero.steward.freeze;

import com.swornhero.steward.config.FreezePolicyService;
import com.swornhero.steward.permission.StewardPermissions;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class FreezeConnectionService {

    private FreezeConnectionService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) ->
                        handleDisconnect(
                                handler.player,
                                server
                        )
        );

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        handleJoin(
                                handler.player,
                                server
                        )
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
            FreezeService.saveActiveFreezes();
            FreezeAuditService.recordDisconnect(record);
        }

        if (!FreezePolicyService.get()
                .disconnectAlerts()) {
            return;
        }

        Component alert =
                Component.literal(
                        "[Steward] "
                                + player.getName().getString()
                                + " disconnected while frozen."
                );

        notifyStaff(
                server,
                alert
        );
    }

    private static void handleJoin(
            ServerPlayer player,
            MinecraftServer server
    ) {
        PendingNotificationService.deliverPendingNotice(
                player
        );

        if (!FreezeService.isFrozen(player)) {
            return;
        }

        FreezeRecord record =
                FreezeService.getRecord(player);

        if (record == null) {
            return;
        }

        record.recordReconnect();
        FreezeService.saveActiveFreezes();
        FreezeAuditService.recordReconnect(record);

        boolean usedFallback =
                FreezeService.restoreFrozenPlayer(
                        server,
                        player,
                        record
                );

        player.sendSystemMessage(
                Component.literal(
                        "You are still frozen. "
                                + "Please wait for a staff member."
                )
        );

        if (FreezePolicyService.get()
                .showReasonOnReconnect()) {

            player.sendSystemMessage(
                    Component.literal(
                            "Freeze reason: "
                                    + record.reason()
                    )
            );
        }

        if (!FreezePolicyService.get()
                .reconnectAlerts()) {
            return;
        }

        Component alert;

        if (usedFallback
                && FreezePolicyService.get()
                .fallbackStaffAlerts()) {

            alert =
                    Component.literal(
                            "[Steward] "
                                    + player.getName().getString()
                                    + " reconnected while frozen, "
                                    + "but their saved location was "
                                    + "unavailable or unsafe. Their "
                                    + "current safe location was used "
                                    + "as the fallback."
                    );
        } else {
            alert =
                    Component.literal(
                            "[Steward] "
                                    + player.getName().getString()
                                    + " reconnected while still frozen."
                    );
        }

        notifyStaff(
                server,
                alert
        );
    }

    private static void notifyStaff(
            MinecraftServer server,
            Component message
    ) {
        for (ServerPlayer onlinePlayer
                : server.getPlayerList().getPlayers()) {

            if (canReceiveStaffAlerts(onlinePlayer)) {
                onlinePlayer.sendSystemMessage(
                        message
                );
            }
        }
    }

    private static boolean canReceiveStaffAlerts(
            ServerPlayer player
    ) {
        return StewardPermissions.has(
                player,
                StewardPermissions.FREEZE_ALERTS
        );
    }
}