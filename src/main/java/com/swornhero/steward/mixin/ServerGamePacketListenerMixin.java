package com.swornhero.steward.mixin;

import com.swornhero.steward.freeze.FreezeCommandService;
import com.swornhero.steward.freeze.FreezeProtectionService;
import com.swornhero.steward.freeze.FreezeService;
import com.swornhero.steward.gui.*;
import net.minecraft.network.protocol.game.ServerboundChatCommandPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "handlePlayerAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenPlayerActions(
            ServerboundPlayerActionPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        ServerboundPlayerActionPacket.Action action =
                packet.getAction();

        boolean blockedAction =
                action
                        == ServerboundPlayerActionPacket.Action.DROP_ITEM
                        || action
                        == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                        || action
                        == ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND;

        if (!blockedAction) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(
                player
        );
    }

    @Inject(
            method = "handleContainerClick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenContainerClicks(
            ServerboundContainerClickPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        if (steward$isStewardMenuOpen()) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(player);
    }

    @Inject(
            method = "handleSetCreativeModeSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenCreativeSlotChanges(
            ServerboundSetCreativeModeSlotPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(
                player
        );
    }

    @Inject(
            method = "handlePlaceRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenRecipePlacement(
            ServerboundPlaceRecipePacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(
                player
        );
    }

    @Inject(
            method = "handleChatCommand",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenCommands(
            ServerboundChatCommandPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        String command = packet.command();

        if (FreezeCommandService.isAllowed(command)) {
            return;
        }

        callbackInfo.cancel();

        FreezeCommandService.notifyBlocked(player);
    }

    @Unique
    private boolean steward$isStewardMenuOpen() {
        return player.containerMenu instanceof StaffControlMenu
                || player.containerMenu instanceof PlayerBrowserMenu
                || player.containerMenu instanceof PlayerProfileMenu
                || player.containerMenu instanceof FreezeReasonMenu
                || player.containerMenu instanceof FreezeHistoryMenu
                || player.containerMenu instanceof FreezeHistoryDetailMenu
                || player.containerMenu instanceof ActiveFreezeMenu
                || player.containerMenu instanceof ActiveFreezeDetailMenu
                || player.containerMenu instanceof UnfreezeConfirmMenu
                || player.containerMenu instanceof RelocateConfirmMenu;
    }
}