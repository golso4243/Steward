package com.swornhero.steward.module.staffmode.service;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class StaffModeService {

    private static final Set<UUID> ACTIVE_STAFF =
            new HashSet<>();

    private StaffModeService() {
        // Utility class
    }

    public static boolean isActive(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        return ACTIVE_STAFF.contains(staffUuid);
    }

    public static boolean enable(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        return ACTIVE_STAFF.add(staffUuid);
    }

    public static boolean disable(UUID staffUuid) {
        if (staffUuid == null) {
            return false;
        }

        return ACTIVE_STAFF.remove(staffUuid);
    }
}