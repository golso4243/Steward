package com.swornhero.steward.module.staffmode.model;

import java.util.List;
import java.util.UUID;

public record StaffModeSnapshotEntry(
        UUID staffUuid,
        List<String> inventory,
        int selectedSlot
) {

    public StaffModeSnapshotEntry {
        if (staffUuid == null) {
            throw new IllegalArgumentException(
                    "Staff UUID cannot be null."
            );
        }

        if (inventory == null) {
            throw new IllegalArgumentException(
                    "Inventory data cannot be null."
            );
        }

        inventory = List.copyOf(inventory);
    }
}