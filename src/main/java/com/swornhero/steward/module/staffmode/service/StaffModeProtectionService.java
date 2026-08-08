package com.swornhero.steward.module.staffmode.service;

import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;

public final class StaffModeProtectionService {

    private StaffModeProtectionService() {
        // Utility class
    }

    public static void register() {
        ServerPlayerEvents.AFTER_RESPAWN.register(
                (oldPlayer, newPlayer, alive) -> {
                    if (!StaffModeService.isActive(
                            newPlayer.getUUID()
                    )) {
                        return;
                    }

                    StaffModeSnapshotService.prepareStaffInventory(
                            newPlayer
                    );

                    StaffModeToolService.giveDefaultTools(
                            newPlayer
                    );
                }
        );

        UseBlockCallback.EVENT.register(
                (player, level, hand, hitResult) -> {
                    if (!StaffModeService.isActive(
                            player.getUUID()
                    )) {
                        return InteractionResult.PASS;
                    }

                    if (player instanceof ServerPlayer serverPlayer) {
                        resynchronizeInventory(serverPlayer);
                    }

                    return InteractionResult.FAIL;
                }
        );

        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    if (!StaffModeService.isActive(
                            player.getUUID()
                    )) {
                        return InteractionResult.PASS;
                    }

                    if (player instanceof ServerPlayer serverPlayer) {
                        resynchronizeInventory(serverPlayer);
                    }

                    return InteractionResult.FAIL;
                }
        );
    }

    public static void denyAndResynchronize(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        resynchronizeInventory(player);
    }

    private static void resynchronizeInventory(
            ServerPlayer player
    ) {
        player.getInventory().setChanged();
        player.inventoryMenu.sendAllDataToRemote();

        if (player.containerMenu != player.inventoryMenu) {
            player.containerMenu.sendAllDataToRemote();
        }
    }
}