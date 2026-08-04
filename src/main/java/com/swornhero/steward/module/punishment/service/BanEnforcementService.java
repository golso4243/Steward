package com.swornhero.steward.module.punishment.service;

import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class BanEnforcementService {

    private BanEnforcementService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) ->
                        enforceActiveBan(handler.player)
        );
    }

    private static void enforceActiveBan(
            ServerPlayer player
    ) {
        PunishmentRecord activeBan =
                findActiveBan(player.getUUID());

        if (activeBan == null) {
            return;
        }

        player.connection.disconnect(
                Component.literal(
                        buildBanMessage(activeBan)
                )
        );
    }

    private static PunishmentRecord findActiveBan(
            UUID targetUuid
    ) {
        List<PunishmentRecord> permanentBans =
                PunishmentService.activePunishmentsFor(
                        targetUuid,
                        PunishmentType.PERMANENT_BAN
                );

        if (!permanentBans.isEmpty()) {
            return permanentBans.getFirst();
        }

        List<PunishmentRecord> temporaryBans =
                PunishmentService.activePunishmentsFor(
                        targetUuid,
                        PunishmentType.TEMPORARY_BAN
                );

        if (temporaryBans.isEmpty()) {
            return null;
        }

        return temporaryBans.getFirst();
    }

    private static String buildBanMessage(
            PunishmentRecord record
    ) {
        StringBuilder message =
                new StringBuilder();

        if (record.type()
                == PunishmentType.PERMANENT_BAN) {

            message.append(
                    "You are permanently banned from this server."
            );
        } else {
            message.append(
                    "You are temporarily banned from this server."
            );
        }

        message.append(
                "\n\nReason: "
        );

        message.append(
                record.reason()
        );

        if (record.expiresAt() != null) {
            message.append(
                    "\nRemaining: "
            );

            message.append(
                    formatRemainingTime(
                            record.expiresAt()
                    )
            );
        }

        message.append(
                "\nPunishment ID: "
        );

        message.append(
                PunishmentService.formatPunishmentId(
                        record.punishmentId()
                )
        );

        return message.toString();
    }

    private static String formatRemainingTime(
            Instant expiresAt
    ) {
        Duration remaining =
                Duration.between(
                        Instant.now(),
                        expiresAt
                );

        if (remaining.isNegative()
                || remaining.isZero()) {

            return "expired";
        }

        long totalMinutes =
                Math.max(
                        1L,
                        remaining.toMinutes()
                );

        long days =
                totalMinutes / 1440;

        long hours =
                totalMinutes % 1440 / 60;

        long minutes =
                totalMinutes % 60;

        if (days > 0) {
            return days
                    + "d "
                    + hours
                    + "h";
        }

        if (hours > 0) {
            return hours
                    + "h "
                    + minutes
                    + "m";
        }

        return minutes + "m";
    }
}