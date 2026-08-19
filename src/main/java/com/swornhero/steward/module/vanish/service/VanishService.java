package com.swornhero.steward.module.vanish.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.vanish.storage.VanishStorageService;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VanishService {
    private static final Set<UUID> VANISHED = new HashSet<>();

    private VanishService() {
    }

    public static synchronized void load(MinecraftServer server) {
        VANISHED.clear();
        VANISHED.addAll(VanishStorageService.load());
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (isVanished(player)) {
                apply(player);
            }
        }
        Steward.LOGGER.info("Loaded {} persistent vanish states.", VANISHED.size());
    }

    public static synchronized boolean isVanished(ServerPlayer player) {
        return VANISHED.contains(player.getUUID());
    }

    public static void toggle(ServerPlayer player) {
        if (!StewardPermissions.require(player, StewardPermissions.VANISH_USE)) {
            return;
        }
        if (isVanished(player)) {
            disable(player);
        } else {
            enable(player);
        }
    }

    public static synchronized void enable(ServerPlayer player) {
        if (!VANISHED.add(player.getUUID())) {
            player.sendSystemMessage(Component.literal("[Steward] Vanish is already enabled."));
            return;
        }
        if (!VanishStorageService.save(VANISHED)) {
            VANISHED.remove(player.getUUID());
            player.sendSystemMessage(Component.literal("[Steward] Vanish could not be persisted; no change was made."));
            return;
        }
        apply(player);
        player.sendSystemMessage(Component.literal("[Steward] Vanish enabled.").withStyle(ChatFormatting.GREEN));
        notifyStaff(player, true);
    }

    public static synchronized void disable(ServerPlayer player) {
        if (!VANISHED.remove(player.getUUID())) {
            player.sendSystemMessage(Component.literal("[Steward] Vanish is already disabled."));
            return;
        }
        if (!VanishStorageService.save(VANISHED)) {
            VANISHED.add(player.getUUID());
            player.sendSystemMessage(Component.literal("[Steward] Vanish could not be persisted; no change was made."));
            return;
        }
        player.setInvisible(false);
        showInPlayerList(player);
        player.sendSystemMessage(Component.literal("[Steward] Vanish disabled.").withStyle(ChatFormatting.YELLOW));
        notifyStaff(player, false);
    }

    public static void apply(ServerPlayer player) {
        player.setInvisible(true);
        refreshPlayerListVisibility(player);
    }

    public static void refreshViewer(ServerPlayer viewer) {
        MinecraftServer server = viewer.getServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer vanished : server.getPlayerList().getPlayers()) {
            if (isVanished(vanished)) {
                sendPlayerListState(viewer, vanished);
            }
        }
    }

    private static void refreshPlayerListVisibility(ServerPlayer vanished) {
        MinecraftServer server = vanished.getServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            sendPlayerListState(viewer, vanished);
        }
    }

    private static void sendPlayerListState(ServerPlayer viewer, ServerPlayer vanished) {
        boolean maySee = viewer == vanished || StewardPermissions.has(viewer, StewardPermissions.VANISH_SEE);
        if (maySee) {
            viewer.connection.send(ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(vanished)));
        } else {
            viewer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(vanished.getUUID())));
        }
    }

    private static void showInPlayerList(ServerPlayer player) {
        MinecraftServer server = player.getServer();
        if (server == null) {
            return;
        }
        ClientboundPlayerInfoUpdatePacket packet =
                ClientboundPlayerInfoUpdatePacket.createPlayerInitializing(List.of(player));
        for (ServerPlayer viewer : server.getPlayerList().getPlayers()) {
            viewer.connection.send(packet);
        }
    }

    private static void notifyStaff(ServerPlayer actor, boolean enabled) {
        MinecraftServer server = actor.getServer();
        if (server == null) {
            return;
        }
        Component message = Component.literal("[Steward] " + actor.getName().getString()
                + " " + (enabled ? "enabled" : "disabled") + " vanish.")
                .withStyle(ChatFormatting.GRAY);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (player != actor && StewardPermissions.has(player, StewardPermissions.VANISH_NOTIFICATIONS)) {
                player.sendSystemMessage(message);
            }
        }
    }
}
