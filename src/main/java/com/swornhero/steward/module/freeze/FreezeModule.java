package com.swornhero.steward.module.freeze;

import com.swornhero.steward.module.freeze.config.FreezePolicyService;
import com.swornhero.steward.module.freeze.service.FreezeConnectionService;
import com.swornhero.steward.module.freeze.service.FreezeDamageService;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.freeze.service.FreezeProtectionService;
import com.swornhero.steward.module.freeze.service.FreezeService;
import com.swornhero.steward.module.freeze.service.PendingNotificationService;

public final class FreezeModule {

    private FreezeModule() {
        // Utility class
    }

    public static void register() {
        FreezePolicyService.register();

        FreezeHistoryService.register();
        PendingNotificationService.register();

        FreezeService.register();
        FreezeDamageService.register();
        FreezeProtectionService.register();
        FreezeConnectionService.register();
    }
}