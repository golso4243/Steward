package com.swornhero.steward.module.vanish;

import com.swornhero.steward.module.vanish.service.VanishConnectionService;
import com.swornhero.steward.module.vanish.service.VanishService;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

public final class VanishModule {
    private VanishModule() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> VanishService.load(server)
        );
        VanishConnectionService.register();
    }
}
