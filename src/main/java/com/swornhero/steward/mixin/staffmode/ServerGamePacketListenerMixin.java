package com.swornhero.steward.mixin.staffmode;

import com.swornhero.steward.module.staffmode.service.StaffModeProtectionService;
import com.swornhero.steward.module.staffmode.service.StaffModeService;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerMixin {

    @Shadow
    public ServerPlayer player;

    @Inject(
            method = "handlePlayerAction",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$protectStaffModeFromPlayerActions(
            ServerboundPlayerActionPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!StaffModeService.isActive(player.getUUID())) {
            return;
        }

        ServerboundPlayerActionPacket.Action action =
                packet.getAction();

        if (action != ServerboundPlayerActionPacket.Action.DROP_ITEM
                && action != ServerboundPlayerActionPacket.Action.DROP_ALL_ITEMS
                && action != ServerboundPlayerActionPacket.Action.SWAP_ITEM_WITH_OFFHAND) {
            return;
        }

        callbackInfo.cancel();
        StaffModeProtectionService.denyAndResynchronize(player);
    }

    @Inject(
            method = "handleContainerClick",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$protectStaffModeFromContainerClicks(
            ServerboundContainerClickPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!StaffModeService.isActive(player.getUUID())
                || steward$isAllowedMenuClick(packet)) {
            return;
        }

        callbackInfo.cancel();
        StaffModeProtectionService.denyAndResynchronize(player);
    }

    @Inject(
            method = "handleSetCreativeModeSlot",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$protectStaffModeFromCreativeChanges(
            ServerboundSetCreativeModeSlotPacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!StaffModeService.isActive(player.getUUID())) {
            return;
        }

        callbackInfo.cancel();
        StaffModeProtectionService.denyAndResynchronize(player);
    }

    @Inject(
            method = "handlePlaceRecipe",
            at = @At("HEAD"),
            cancellable = true
    )
    private void steward$protectStaffModeFromRecipePlacement(
            ServerboundPlaceRecipePacket packet,
            CallbackInfo callbackInfo
    ) {
        if (!StaffModeService.isActive(player.getUUID())) {
            return;
        }

        callbackInfo.cancel();
        StaffModeProtectionService.denyAndResynchronize(player);
    }

    @Unique
    private boolean steward$isAllowedMenuClick(
            ServerboundContainerClickPacket packet
    ) {
        if (player.containerMenu == player.inventoryMenu) {
            return false;
        }

        int slotNumber = packet.slotNum();

        if (slotNumber < 0
                || slotNumber >= player.containerMenu.slots.size()) {
            return false;
        }

        Slot slot = player.containerMenu.getSlot(slotNumber);

        return slot.container != player.getInventory();
    }
}