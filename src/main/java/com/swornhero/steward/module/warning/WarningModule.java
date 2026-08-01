package com.swornhero.steward.module.warning;

import com.swornhero.steward.module.warning.service.WarningService;

public final class WarningModule {

    private WarningModule() {
        // Utility class
    }

    public static void register() {
        WarningService.register();
    }
}