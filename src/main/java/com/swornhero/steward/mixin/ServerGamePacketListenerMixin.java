package com.swornhero.steward.mixin;

import com.swornhero.steward.freeze.FreezeProtectionService;
import com.swornhero.steward.freeze.FreezeService;
import com.swornhero.steward.gui.PlayerBrowserMenu;
import com.swornhero.steward.gui.PlayerProfileMenu;
import com.swornhero.steward.gui.StaffControlMenu;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    /**
     * Blocks Q and Ctrl + Q item dropping while the player is frozen.
     */
    @Inject(
            method = "handlePlayerAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$blockFrozenItemDrops(
            ServerboundPlayerActionPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!FreezeService.isFrozen(player)) {
            return;
        }

        ServerboundPlayerActionPacket.Action action =
                packet.getAction();

        boolean droppingItem =
                action
                        == ServerboundPlayerActionPacket.Action.DROP_ITEM
                        || action
                        == ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS;

        if (!droppingItem) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(player);
    }

    /**
     * Blocks inventory and container interaction while frozen.
     * Steward's own menus are allowed through because they already prevent
     * item movement and are needed so staff can navigate the interface and
     * unfreeze themselves during testing.
     */
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

        if (isStewardMenuOpen()) {
            return;
        }

        callbackInfo.cancel();

        FreezeProtectionService.denyAndResynchronize(player);
    }

    /**
     * Returns true when the player currently has one of Steward's protected
     * navigation menus open.
     */
    @Unique
    private boolean isStewardMenuOpen() {
        return player.containerMenu instanceof StaffControlMenu
                || player.containerMenu instanceof PlayerBrowserMenu
                || player.containerMenu instanceof PlayerProfileMenu;
    }
}