package com.swornhero.steward.freeze;

import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FreezeProtectionService {
    private static final long MESSAGE_COOLDOWN_MS = 1500L;

    private static final Map<UUID, Long> LAST_MESSAGE_TIME =
            new HashMap<>();

    private FreezeProtectionService() {
        // Utility class
    }

    public static void register() {
        registerBlockBreakingProtection();
        registerBlockUseProtection();
        registerItemUseProtection();
        registerEntityProtection();
    }

    private static void registerBlockBreakingProtection() {
        AttackBlockCallback.EVENT.register(
                (player, level, hand, position, direction) -> {
                    if (FreezeService.isFrozen(player.getUUID())) {
                        return denyInteraction(player);
                    }

                    return InteractionResult.PASS;
                }
        );

        PlayerBlockBreakEvents.BEFORE.register(
                (level, player, position, state, blockEntity) ->
                        !FreezeService.isFrozen(player.getUUID())
        );
    }

    private static void registerBlockUseProtection() {
        UseBlockCallback.EVENT.register(
                (player, level, hand, hitResult) -> {
                    if (FreezeService.isFrozen(player.getUUID())) {
                        return denyInteraction(player);
                    }

                    return InteractionResult.PASS;
                }
        );
    }

    private static void registerItemUseProtection() {
        UseItemCallback.EVENT.register(
                (player, level, hand) -> {
                    if (FreezeService.isFrozen(player.getUUID())) {
                        return denyInteraction(player);
                    }

                    return InteractionResult.PASS;
                }
        );
    }

    private static void registerEntityProtection() {
        AttackEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    if (FreezeService.isFrozen(player.getUUID())) {
                        return denyInteraction(player);
                    }

                    return InteractionResult.PASS;
                }
        );

        UseEntityCallback.EVENT.register(
                (player, level, hand, entity, hitResult) -> {
                    if (FreezeService.isFrozen(player.getUUID())) {
                        return denyInteraction(player);
                    }

                    return InteractionResult.PASS;
                }
        );
    }

    private static InteractionResult denyInteraction(
            Player player
    ) {
        if (player instanceof ServerPlayer serverPlayer) {
            resynchronizeInventory(serverPlayer);
            sendFrozenMessage(serverPlayer);
        }

        return InteractionResult.FAIL;
    }

    public static void denyAndResynchronize(
            ServerPlayer player
    ) {
        resynchronizeInventory(player);
        sendFrozenMessage(player);
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

    private static void sendFrozenMessage(
            ServerPlayer player
    ) {
        long currentTime = System.currentTimeMillis();
        long previousTime = LAST_MESSAGE_TIME.getOrDefault(
                player.getUUID(),
                0L
        );

        if (currentTime - previousTime < MESSAGE_COOLDOWN_MS) {
            return;
        }

        LAST_MESSAGE_TIME.put(
                player.getUUID(),
                currentTime
        );

        player.sendSystemMessage(
                Component.literal(
                        "You cannot perform that action while frozen."
                ),
                true
        );
    }
}