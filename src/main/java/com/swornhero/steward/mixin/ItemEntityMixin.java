package com.swornhero.steward.mixin;

import com.swornhero.steward.freeze.FreezeService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

    @Inject(
            method = "playerTouch",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenItemPickup(
            Player player,
            CallbackInfo callbackInfo
    ) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (FreezeService.isFrozen(serverPlayer)) {
            callbackInfo.cancel();
        }
    }
}