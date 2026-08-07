package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.module.staffmode.model.StaffModeSnapshot;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class StaffModeService {

    private static final Set<UUID> ACTIVE_STAFF =
            new HashSet<>();

    private static final Map<UUID, StaffModeSnapshot> SNAPSHOTS =
            new HashMap<>();

    private StaffModeService() {
        // Utility class
    }

    public static boolean hasSnapshot(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return false;
        }

        return SNAPSHOTS.containsKey(staffUuid);
    }

    public static boolean restoreSnapshot(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        UUID staffUuid =
                player.getUUID();

        StaffModeSnapshot snapshot =
                SNAPSHOTS.get(staffUuid);

        if (snapshot == null) {
            return false;
        }

        return StaffModeSnapshotService.restore(
                player,
                snapshot
        );
    }

    public static StaffModeSnapshot snapshotFor(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return null;
        }

        return SNAPSHOTS.get(staffUuid);
    }

    public static boolean captureSnapshot(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        UUID staffUuid =
                player.getUUID();

        if (SNAPSHOTS.containsKey(staffUuid)) {
            return false;
        }

        StaffModeSnapshot snapshot =
                StaffModeSnapshotService.capture(
                        player
                );

        SNAPSHOTS.put(
                staffUuid,
                snapshot
        );

        return true;
    }

    public static boolean enable(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        UUID staffUuid =
                player.getUUID();

        if (isActive(staffUuid)) {
            return false;
        }

        if (!captureSnapshot(player)) {
            return false;
        }

        return ACTIVE_STAFF.add(staffUuid);
    }

    public static boolean isActive(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        return ACTIVE_STAFF.contains(staffUuid);
    }

    public static boolean disable(
            ServerPlayer player
    ) {
        if (player == null) {
            return false;
        }

        UUID staffUuid =
                player.getUUID();

        if (!isActive(staffUuid)) {
            return false;
        }

        if (!restoreSnapshot(player)) {
            return false;
        }

        boolean disabled =
                ACTIVE_STAFF.remove(staffUuid);

        if (!disabled) {
            return false;
        }

        SNAPSHOTS.remove(staffUuid);

        return true;
    }
}