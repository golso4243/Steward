package com.swornhero.steward.module.freeze.service;

import com.swornhero.steward.config.FreezePolicyService;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.server.level.ServerPlayer;

public final class FreezeDamageService {

    private FreezeDamageService() {
        // Utility class
    }

    public static void register() {
        ServerLivingEntityEvents.ALLOW_DAMAGE.register(
                (entity, damageSource, amount) -> {
                    if (!(entity instanceof ServerPlayer player)) {
                        return true;
                    }

                    if (!FreezeService.isFrozen(player)) {
                        return true;
                    }

                    return !FreezePolicyService.get()
                            .preventDamageWhileFrozen();
                }
        );

        ServerLivingEntityEvents.ALLOW_DEATH.register(
                (entity, damageSource, damageAmount) -> {
                    if (!(entity instanceof ServerPlayer player)) {
                        return true;
                    }

                    if (!FreezeService.isFrozen(player)) {
                        return true;
                    }

                    /*
                     * This is a final safety layer. Normally, all
                     * damage is already stopped by ALLOW_DAMAGE.
                     */
                    if (FreezePolicyService.get()
                            .preventDamageWhileFrozen()) {

                        if (player.getHealth() <= 0.0F) {
                            player.setHealth(1.0F);
                        }

                        return false;
                    }

                    return true;
                }
        );
    }
}