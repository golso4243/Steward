package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.staffmode.model.StaffModeSnapshot;
import net.minecraft.server.level.ServerPlayer;

import com.swornhero.steward.module.staffmode.model.StaffModeSnapshotEntry;
import com.swornhero.steward.module.staffmode.storage.StaffModeSnapshotCodec;
import com.swornhero.steward.module.staffmode.storage.StaffModeStorageService;
import net.minecraft.core.HolderLookup;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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

    private static boolean persistSnapshots(
            HolderLookup.Provider registries
    ) {
        if (registries == null) {
            return false;
        }

        List<StaffModeSnapshotEntry> entries =
                new ArrayList<>();

        for (StaffModeSnapshot snapshot
                : SNAPSHOTS.values()) {

            entries.add(
                    StaffModeSnapshotCodec.encode(
                            snapshot,
                            registries
                    )
            );
        }

        entries.sort(
                Comparator.comparing(
                        entry ->
                                entry.staffUuid()
                                        .toString()
                )
        );

        return StaffModeStorageService.save(
                entries
        );
    }

    private static boolean hasPersistedSnapshot(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return false;
        }

        for (StaffModeSnapshotEntry entry
                : StaffModeStorageService.load()) {

            if (staffUuid.equals(
                    entry.staffUuid()
            )) {
                return true;
            }
        }

        return false;
    }

    public static void restorePersistedSnapshots(
            HolderLookup.Provider registries
    ) {
        if (registries == null) {
            return;
        }

        SNAPSHOTS.clear();
        ACTIVE_STAFF.clear();

        for (StaffModeSnapshotEntry entry
                : StaffModeStorageService.load()) {

            if (entry == null) {
                continue;
            }

            StaffModeSnapshot snapshot =
                    StaffModeSnapshotCodec.decode(
                            entry,
                            registries
                    );

            SNAPSHOTS.put(
                    snapshot.staffUuid(),
                    snapshot
            );
        }
    }

    public static boolean needsRecovery(
            UUID staffUuid
    ) {
        return hasSnapshot(staffUuid)
                && !isActive(staffUuid);
    }

    public static boolean recover(
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

        if (!StaffModeSnapshotService.restore(
                player,
                snapshot
        )) {
            return false;
        }

        ACTIVE_STAFF.remove(
                staffUuid
        );

        SNAPSHOTS.remove(
                staffUuid
        );

        if (!persistSnapshots(
                player.registryAccess()
        )) {
            SNAPSHOTS.put(
                    staffUuid,
                    snapshot
            );

            return false;
        }

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

        if (hasSnapshot(staffUuid)) {
            return false;
        }

        if (!captureSnapshot(player)) {
            return false;
        }

        if (!persistSnapshots(
                player.registryAccess()
        )) {
            SNAPSHOTS.remove(staffUuid);

            return false;
        }

        /*
         * Critical safety check:
         * never clear the inventory unless the snapshot
         * can actually be read back from disk.
         */
        if (!hasPersistedSnapshot(staffUuid)) {
            SNAPSHOTS.remove(staffUuid);

            Steward.LOGGER.error(
                    "Staff Mode enable aborted for {}: persisted snapshot verification failed. File: {}",
                    staffUuid,
                    StaffModeStorageService
                            .snapshotFile()
                            .toAbsolutePath()
            );

            return false;
        }

        if (!ACTIVE_STAFF.add(staffUuid)) {
            return false;
        }

        StaffModeSnapshotService.prepareStaffInventory(
                player
        );

        Steward.LOGGER.info(
                "Staff Mode enabled for {}. Recovery snapshot: {}",
                staffUuid,
                StaffModeStorageService
                        .snapshotFile()
                        .toAbsolutePath()
        );

        return true;
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
            Steward.LOGGER.error(
                    "Staff Mode disable failed for {}: not active.",
                    staffUuid
            );

            return false;
        }

        StaffModeSnapshot snapshot =
                SNAPSHOTS.get(staffUuid);

        if (snapshot == null) {
            Steward.LOGGER.error(
                    "Staff Mode disable failed for {}: in-memory snapshot missing.",
                    staffUuid
            );

            return false;
        }

        Steward.LOGGER.info(
                "Staff Mode disable for {}: snapshot slots={}, inventory slots={}, persisted={}",
                staffUuid,
                snapshot.inventory().size(),
                player.getInventory().getContainerSize(),
                hasPersistedSnapshot(staffUuid)
        );

        if (!StaffModeSnapshotService.restore(
                player,
                snapshot
        )) {
            Steward.LOGGER.error(
                    "Staff Mode disable failed for {}: inventory restoration failed.",
                    staffUuid
            );

            return false;
        }

        if (!ACTIVE_STAFF.remove(staffUuid)) {
            Steward.LOGGER.error(
                    "Staff Mode disable failed for {}: active-state removal failed.",
                    staffUuid
            );

            return false;
        }

        SNAPSHOTS.remove(staffUuid);

        if (!persistSnapshots(
                player.registryAccess()
        )) {
            SNAPSHOTS.put(
                    staffUuid,
                    snapshot
            );

            Steward.LOGGER.error(
                    "Staff Mode disable failed for {}: recovery-file cleanup failed.",
                    staffUuid
            );

            return false;
        }

        return true;
    }
}