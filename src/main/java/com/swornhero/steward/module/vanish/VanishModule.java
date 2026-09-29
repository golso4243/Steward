package com.swornhero.steward.module.vanish;

import com.swornhero.steward.module.vanish.service.VanishConnectionService;
import com.swornhero.steward.module.vanish.service.VanishService;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

public final class VanishModule {
    private VanishModule() {
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> VanishService.load(server)
        );
        ServerTickEvents.END_SERVER_TICK.register(
                VanishService::onEndServerTick
        );
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    if (VanishService.isVanished(newPlayer)) {
                        VanishService.apply(newPlayer);
                    }
                }
        );
        VanishConnectionService.register();
    }
}
