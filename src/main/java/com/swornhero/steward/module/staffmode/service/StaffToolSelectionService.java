package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.module.staffmode.model.StaffToolAction;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class StaffToolSelectionService {

    private static final Map<UUID, StaffToolAction> PENDING_ACTIONS =
            new HashMap<>();

    private StaffToolSelectionService() {
        // Utility class
    }

    public static void setPendingAction(
            UUID staffUuid,
            StaffToolAction action
    ) {
        if (staffUuid == null || action == null) {
            return;
        }

        PENDING_ACTIONS.put(
                staffUuid,
                action
        );
    }

    public static StaffToolAction pendingAction(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return null;
        }

        return PENDING_ACTIONS.get(
                staffUuid
        );
    }

    public static StaffToolAction consumePendingAction(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return null;
        }

        return PENDING_ACTIONS.remove(
                staffUuid
        );
    }

    public static void clear(
            UUID staffUuid
    ) {
        if (staffUuid == null) {
            return;
        }

        PENDING_ACTIONS.remove(
                staffUuid
        );
    }
}