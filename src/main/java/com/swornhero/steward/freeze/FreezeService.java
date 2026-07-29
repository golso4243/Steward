package com.swornhero.steward.freeze;

import com.swornhero.steward.config.FreezePolicyService;
import com.swornhero.steward.module.freeze.model.FreezePosition;
import com.swornhero.steward.module.freeze.model.FreezeRecord;
import com.swornhero.steward.permission.StaffHierarchyService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Relative;
import com.swornhero.steward.Steward;
import net.minecraft.core.BlockPos;
import java.util.function.Consumer;

import java.util.HashSet;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class FreezeService {
    private static final Map<UUID, FreezeRecord> FROZEN_PLAYERS =
            new HashMap<>();

    private static final Set<UUID> FALLBACK_WARNED_PLAYERS =
            new HashSet<>();

    private FreezeService() {
        // Utility class
    }

    public static boolean restoreFrozenPlayer(
            MinecraftServer server,
            ServerPlayer player,
            FreezeRecord record
    ) {

        FreezePosition savedPosition =
                record.currentPosition();

        ServerLevel targetLevel =
                server.getLevel(
                        savedPosition.dimension()
                );

        boolean useFallback =
                targetLevel == null
                        || !isUsableFreezePosition(
                        targetLevel,
                        savedPosition
                );

        FreezePosition destination =
                useFallback
                        ? createFallbackPosition(player)
                        : savedPosition;

        ServerLevel destinationLevel =
                server.getLevel(
                        destination.dimension()
                );

        if (destinationLevel == null) {
            /*
             * The fallback uses the Overworld, so this should only
             * happen during a severe server/world initialization issue.
             */
            Steward.LOGGER.error(
                    "Unable to restore frozen player {} because "
                            + "neither the saved dimension nor the "
                            + "fallback dimension is available.",
                    record.targetName()
            );

            return false;
        }

        player.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        player.fallDistance = 0.0F;

        boolean wrongDimension =
                !player.level()
                        .dimension()
                        .equals(
                                destination.dimension()
                        );

        if (wrongDimension) {
            player.teleportTo(
                    destinationLevel,
                    destination.x(),
                    destination.y(),
                    destination.z(),
                    Set.<Relative>of(),
                    destination.yaw(),
                    destination.pitch(),
                    false
            );
        } else {
            player.teleportTo(
                    destination.x(),
                    destination.y(),
                    destination.z()
            );

            player.setYRot(destination.yaw());
            player.setXRot(destination.pitch());
        }

        /*
         * Correct any client-side inventory prediction that occurred
         * while the player was reconnecting or being restored.
         */
        player.getInventory().setChanged();
        player.inventoryMenu.sendAllDataToRemote();

        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.sendAllDataToRemote();
        }

        if (useFallback) {
            warnAboutFallback(
                    record,
                    savedPosition,
                    destination
            );
        } else {
            FALLBACK_WARNED_PLAYERS.remove(
                    record.targetUuid()
            );
        }

        return useFallback;
    }

    private static boolean isUsableFreezePosition(
            ServerLevel level,
            FreezePosition position
    ) {
        if (!Double.isFinite(position.x())
                || !Double.isFinite(position.y())
                || !Double.isFinite(position.z())) {

            return false;
        }

        BlockPos feetPosition =
                BlockPos.containing(
                        position.x(),
                        position.y(),
                        position.z()
                );

        BlockPos headPosition =
                feetPosition.above();

        boolean feetBlocked =
                !level.getBlockState(feetPosition)
                        .getCollisionShape(
                                level,
                                feetPosition
                        )
                        .isEmpty();

        boolean headBlocked =
                !level.getBlockState(headPosition)
                        .getCollisionShape(
                                level,
                                headPosition
                        )
                        .isEmpty();

        return !feetBlocked && !headBlocked;
    }

    private static FreezePosition createFallbackPosition(
            ServerPlayer player
    ) {
        return new FreezePosition(
                player.level().dimension(),
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYRot(),
                player.getXRot()
        );
    }

    private static void warnAboutFallback(
            FreezeRecord record,
            FreezePosition savedPosition,
            FreezePosition fallbackPosition
    ) {
        if (!FreezePolicyService.get()
                .fallbackConsoleWarnings()) {

            return;
        }
        if (!FALLBACK_WARNED_PLAYERS.add(
                record.targetUuid()
        )) {
            return;
        }

        Steward.LOGGER.warn(
                "Frozen player {} could not be restored to "
                        + "{} at [{}, {}, {}]. "
                        + "Using fallback {} at [{}, {}, {}].",
                record.targetName(),
                savedPosition.dimension()
                        .identifier(),
                savedPosition.x(),
                savedPosition.y(),
                savedPosition.z(),
                fallbackPosition.dimension()
                        .identifier(),
                fallbackPosition.x(),
                fallbackPosition.y(),
                fallbackPosition.z()
        );
    }

    public static void register() {
        restoreActiveFreezes();

        ServerTickEvents.END_SERVER_TICK.register(
                FreezeService::onEndServerTick
        );
    }

    private static void restoreActiveFreezes() {
        FROZEN_PLAYERS.clear();

        Set<UUID> loadedFreezeIds =
                new HashSet<>();

        for (FreezeRecord record
                : ActiveFreezeStorageService.load()) {

            if (record == null) {
                continue;
            }

            if (!loadedFreezeIds.add(
                    record.freezeId()
            )) {
                Steward.LOGGER.error(
                        "Skipped duplicate freeze case ID {} "
                                + "for player {}.",
                        record.freezeId(),
                        record.targetName()
                );

                continue;
            }

            FreezeRecord existing =
                    FROZEN_PLAYERS.putIfAbsent(
                            record.targetUuid(),
                            record
                    );

            if (existing != null) {
                Steward.LOGGER.error(
                        "Skipped duplicate active freeze for player {}. "
                                + "Existing case: {}. Duplicate case: {}.",
                        record.targetName(),
                        existing.freezeId(),
                        record.freezeId()
                );
            }
        }

        Steward.LOGGER.info(
                "Restored {} unique active freeze records.",
                FROZEN_PLAYERS.size()
        );
    }

    public static void saveActiveFreezes() {
        ActiveFreezeStorageService.save(
                FROZEN_PLAYERS.values()
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

    public static Map<UUID, FreezeRecord> getActiveRecords() {
        return Map.copyOf(FROZEN_PLAYERS);
    }

    public static FreezeRecord getRecordByName(
            String playerName
    ) {
        for (FreezeRecord record
                : FROZEN_PLAYERS.values()) {

            if (record.targetName()
                    .equalsIgnoreCase(playerName)) {

                return record;
            }
        }

        return null;
    }

    public static boolean freeze(
            ServerPlayer staff,
            ServerPlayer target,
            String reason
    ) {

        if (!StaffHierarchyService.requireCanAct(
                staff,
                target
        )) {
            return false;
        }

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

        saveActiveFreezes();

        FreezeAuditService.recordFreeze(record);

        maintainFrozenPlayerSafety(
                target
        );

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
        if (!StaffHierarchyService.requireCanAct(
                staff,
                target
        )) {
            return false;
        }

        return completeUnfreeze(
                staff,
                target.getUUID()
        );
    }

    public static void unfreezeAuthorized(
            ServerPlayer staff,
            UUID targetUuid,
            Consumer<Boolean> completion
    ) {
        StaffHierarchyService.checkCanActOnUuid(
                staff,
                targetUuid
        ).whenComplete(
                (result, throwable) -> {
                    MinecraftServer server =
                            staff.level()
                                    .getServer();

                    server.execute(
                            () -> {
                                if (throwable != null) {
                                    Steward.LOGGER.error(
                                            "Failed to resolve hierarchy "
                                                    + "for offline unfreeze.",
                                            throwable
                                    );

                                    staff.sendSystemMessage(
                                            Component.literal(
                                                    "[Steward] Unable to "
                                                            + "verify the target's "
                                                            + "staff hierarchy."
                                            )
                                    );

                                    completion.accept(false);
                                    return;
                                }

                                if (!result.allowed()) {
                                    StaffHierarchyService.sendDenial(
                                            staff,
                                            result
                                    );

                                    completion.accept(false);
                                    return;
                                }

                                boolean unfrozen =
                                        completeUnfreeze(
                                                staff,
                                                targetUuid
                                        );

                                completion.accept(unfrozen);
                            }
                    );
                }
        );
    }

    public static boolean completeUnfreeze(
            ServerPlayer staff,
            UUID targetUuid
    ) {
        FreezeRecord record =
                FROZEN_PLAYERS.remove(targetUuid);

        if (record == null) {
            return false;
        }
        FALLBACK_WARNED_PLAYERS.remove(
                targetUuid
        );

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

        saveActiveFreezes();

        ServerPlayer onlineTarget =
                staff.level()
                        .getServer()
                        .getPlayerList()
                        .getPlayer(targetUuid);

        if (onlineTarget != null) {
            onlineTarget.setDeltaMovement(
                    0.0D,
                    0.0D,
                    0.0D
            );

            onlineTarget.sendSystemMessage(
                    Component.literal(
                            "You are no longer frozen."
                    )
            );

            onlineTarget.sendSystemMessage(
                    Component.literal(
                            "You were unfrozen by "
                                    + staff.getName().getString()
                                    + "."
                    )
            );
        } else if (FreezePolicyService.get()
                .offlineUnfreezeNotices()) {

            PendingNotificationService.queueUnfreezeNotice(
                    record.targetUuid(),
                    record.targetName(),
                    staff.getUUID(),
                    staff.getName().getString()
            );
        }
    return true;
    }

    public static boolean relocateToStaff(
            ServerPlayer staff,
            UUID targetUuid
    ) {
        FreezeRecord record =
                FROZEN_PLAYERS.get(targetUuid);

        if (record == null) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] That player is no longer frozen."
                    )
            );

            return false;
        }

        MinecraftServer server =
                staff.level().getServer();

        ServerPlayer target =
                server.getPlayerList()
                        .getPlayer(targetUuid);

        if (target == null) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] Frozen players must be online "
                                    + "before they can be relocated."
                    )
            );

            return false;
        }

        if (!StaffHierarchyService.requireCanAct(
                staff,
                target
        )) {
            return false;
        }

        FreezePosition previousPosition =
                record.currentPosition();

        FreezePosition newPosition =
                new FreezePosition(
                        staff.level().dimension(),
                        staff.getX(),
                        staff.getY(),
                        staff.getZ(),
                        staff.getYRot(),
                        staff.getXRot()
                );

        ServerLevel destinationLevel =
                server.getLevel(
                        newPosition.dimension()
                );

        if (destinationLevel == null
                || !isUsableFreezePosition(
                destinationLevel,
                newPosition
        )) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] Your current location is not "
                                    + "safe for frozen-player relocation."
                    )
            );

            return false;
        }

        String relocationNote =
                "Player relocated by staff to a safe location "
                        + "while remaining frozen.";

        record.relocate(
                newPosition,
                staff.getUUID(),
                staff.getName().getString(),
                relocationNote
        );

        saveActiveFreezes();

        boolean usedFallback =
                restoreFrozenPlayer(
                        server,
                        target,
                        record
                );

        if (usedFallback) {
            record.restoreCurrentPosition(
                    previousPosition
            );

            record.removeLatestRelocation();
            saveActiveFreezes();

            restoreFrozenPlayer(
                    server,
                    target,
                    record
            );

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] Relocation failed. "
                                    + "The previous freeze anchor "
                                    + "was restored."
                    )
            );

            return false;
        }

        maintainFrozenPlayerSafety(
                target
        );

        FreezeAuditService.recordRelocation(
                record,
                staff,
                previousPosition,
                newPosition,
                relocationNote
        );

        target.sendSystemMessage(
                Component.literal(
                        "A staff member moved you to a safe location. "
                                + "You are still frozen."
                )
        );

        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] "
                                + target.getName().getString()
                                + " was relocated to your position "
                                + "and remains frozen."
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

    public static void maintainFrozenPlayerSafety(
            ServerPlayer player
    ) {
        player.setDeltaMovement(
                0.0D,
                0.0D,
                0.0D
        );

        player.fallDistance = 0.0F;

        if (FreezePolicyService.get()
                .extinguishFrozenPlayers()) {

            player.setRemainingFireTicks(0);
        }

        if (FreezePolicyService.get()
                .restoreAirWhileFrozen()) {

            player.setAirSupply(
                    player.getMaxAirSupply()
            );
        }
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

            restoreFrozenPlayer(
                    server,
                    player,
                    entry.getValue()
            );

            maintainFrozenPlayerSafety(
                    player
            );
        }
    }
}