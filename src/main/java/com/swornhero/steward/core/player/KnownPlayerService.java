package com.swornhero.steward.core.player;

import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.core.storage.JsonListStorage;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerPlayer;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Players who have joined this server, recorded by Steward on every join.
 * Offline name lookups use only this index, so they never contact Mojang
 * and behave identically on online-mode and offline-mode servers.
 */
public final class KnownPlayerService {

    private static final JsonListStorage<KnownPlayer> STORAGE =
            new JsonListStorage<>(
                    Path.of("steward", "data"),
                    "known-players.json",
                    new TypeToken<List<KnownPlayer>>() {
                    }.getType(),
                    "known player index"
            );

    private static final Map<UUID, KnownPlayer> PLAYERS = new HashMap<>();

    private KnownPlayerService() {
        // Utility class
    }

    public static synchronized void register() {
        PLAYERS.clear();

        for (KnownPlayer player : STORAGE.load()) {
            if (player != null
                    && player.uuid() != null
                    && player.name() != null
                    && !player.name().isBlank()) {
                PLAYERS.put(player.uuid(), player);
            }
        }

        Steward.LOGGER.info("Loaded {} known players.", PLAYERS.size());

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> record(handler.player)
        );
    }

    public static synchronized void record(ServerPlayer player) {
        String name = player.getName().getString();

        PLAYERS.put(
                player.getUUID(),
                new KnownPlayer(player.getUUID(), name, Instant.now())
        );

        if (!STORAGE.save(List.copyOf(PLAYERS.values()))) {
            Steward.LOGGER.warn("Could not persist known player {}.", name);
        }
    }

    /**
     * Finds the player who most recently joined under this name,
     * ignoring case. Earlier holders of a since-changed name lose to
     * the latest one.
     */
    public static synchronized Optional<KnownPlayer> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }

        String normalized = name.trim().toLowerCase(Locale.ROOT);

        return PLAYERS.values().stream()
                .filter(player -> player.name().toLowerCase(Locale.ROOT).equals(normalized))
                .max(Comparator.comparing(KnownPlayer::lastSeen));
    }
}
