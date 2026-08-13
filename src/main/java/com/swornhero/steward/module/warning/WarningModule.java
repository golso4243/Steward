package com.swornhero.steward.module.warning;

import com.swornhero.steward.module.warning.service.WarningService;
import com.swornhero.steward.module.warning.service.WarningDraftInputService;

public final class WarningModule {

    private WarningModule() {
        // Utility class
    }

    public static void register() {
        WarningService.register();
        WarningDraftInputService.register();
    }
}
