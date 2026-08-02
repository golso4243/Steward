package com.swornhero.steward.module.punishment;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.punishment.service.PunishmentService;

public final class PunishmentModule {

    private PunishmentModule() {
        // Utility class
    }

    public static void register() {
        PunishmentService.register();

        Steward.LOGGER.info(
                "Punishment module registered."
        );
    }
}