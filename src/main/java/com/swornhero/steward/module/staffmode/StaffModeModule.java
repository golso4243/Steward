package com.swornhero.steward.module.staffmode;

import com.swornhero.steward.module.staffmode.service.StaffModeRecoveryService;
import com.swornhero.steward.module.staffmode.service.StaffModeService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class StaffModeModule {

    private StaffModeModule() {
        // Utility class
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(
                server ->
                        StaffModeService
                                .restorePersistedSnapshots(
                                        server.registryAccess()
                                )
        );

        StaffModeRecoveryService.register();
    }
}