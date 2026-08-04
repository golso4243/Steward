package com.swornhero.steward.module.punishment;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.punishment.service.PunishmentService;
import com.swornhero.steward.module.punishment.service.MuteEnforcementService;
import com.swornhero.steward.module.punishment.service.BanEnforcementService;

public final class PunishmentModule {

    private PunishmentModule() {
        // Utility class
    }

    public static void register() {
        PunishmentService.register();
        MuteEnforcementService.register();
        BanEnforcementService.register();

        Steward.LOGGER.info(
                "Punishment module registered."
        );
    }
}