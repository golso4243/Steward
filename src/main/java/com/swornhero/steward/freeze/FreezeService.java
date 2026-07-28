package com.swornhero.steward.freeze;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FreezeService {
    private static final Map<UUID, FreezeRecord> FROZEN_PLAYERS =
            new HashMap<>();

    private FreezeService() {
        // Utility class
    }

    public static void register() {
        ServerTickEvents.END_SERVER_TICK.register(
                FreezeService::onEndServerTick
        );
    }

    public static boolean isFrozen(UUID playerUuid) {
        return FROZEN_PLAYERS.containsKey(playerUuid);
    }

    public static boolean isFrozen(ServerPlayer player) {
        return isFrozen(player.getUUID());
    }

    public static FreezeRecord getRecord(UUID playerUuid) {
        return FROZEN_PLAYERS.get(playerUuid);
    }

    public static FreezeRecord getRecord(ServerPlayer player) {
        return getRecord(player.getUUID());
    }

    public static boolean freeze(
            ServerPlayer staff,
            ServerPlayer target,
            String reason
    ) {
        if (isFrozen(target)) {
            return false;
        }

        FreezePosition position = new FreezePosition(
                target.level().dimension(),
                target.getX(),
                target.getY(),
                target.getZ(),
                target.getYRot(),
                target.getXRot()
        );

        FreezeRecord record = new FreezeRecord(
                target.getUUID(),
                target.getName().getString(),
                staff.getUUID(),
                staff.getName().getString(),
                position,
                reason,
                Instant.now()
        );

        FROZEN_PLAYERS.put(
                target.getUUID(),
                record
        );

        FreezeAuditService.recordFreeze(record);

        target.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        target.fallDistance = 0.0F;

        target.sendSystemMessage(
                Component.literal(
                        "You have been frozen by a staff member."
                )
        );

        target.sendSystemMessage(
                Component.literal(
                        "Please remain connected and wait for instructions."
                )
        );

        staff.sendSystemMessage(
                Component.literal(
                        target.getName().getString()
                                + " has been frozen."
                )
        );

        return true;
    }

    public static boolean unfreeze(
            ServerPlayer staff,
            ServerPlayer target
    ) {
        FreezeRecord record =
                FROZEN_PLAYERS.remove(
                        target.getUUID()
                );

        if (record == null) {
            return false;
        }

        record.complete(
                staff.getUUID(),
                staff.getName().getString(),
                Instant.now()
        );

        FreezeAuditService.recordUnfreeze(
                record,
                staff
        );

        FreezeHistoryService.add(record);

        target.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        target.sendSystemMessage(
                Component.literal(
                        "You are no longer frozen."
                )
        );

        staff.sendSystemMessage(
                Component.literal(
                        target.getName().getString()
                                + " has been unfrozen."
                )
        );

        return true;
    }

    public static boolean toggle(
            ServerPlayer staff,
            ServerPlayer target,
            String reason
    ) {
        if (isFrozen(target)) {
            return unfreeze(
                    staff,
                    target
            );
        }

        return freeze(
                staff,
                target,
                reason
        );
    }

    private static void onEndServerTick(
            MinecraftServer server
    ) {
        if (FROZEN_PLAYERS.isEmpty()) {
            return;
        }

        for (Map.Entry<UUID, FreezeRecord> entry
                : FROZEN_PLAYERS.entrySet()) {

            ServerPlayer player =
                    server.getPlayerList()
                            .getPlayer(entry.getKey());

            if (player == null) {
                continue;
            }

            FreezePosition position =
                    entry.getValue().position();

            player.setDeltaMovement(
                    0.0D,
                    0.0D,
                    0.0D
            );

            player.fallDistance = 0.0F;

            ServerLevel frozenLevel =
                    server.getLevel(
                            position.dimension()
                    );

            if (frozenLevel == null) {
                continue;
            }

            boolean wrongDimension =
                    !player.level()
                            .dimension()
                            .equals(
                                    position.dimension()
                            );

            if (wrongDimension) {
                player.teleportTo(
                        frozenLevel,
                        position.x(),
                        position.y(),
                        position.z(),
                        Set.<Relative>of(),
                        position.yaw(),
                        position.pitch(),
                        false
                );

                continue;
            }

            player.teleportTo(
                    position.x(),
                    position.y(),
                    position.z()
            );
        }
    }
}