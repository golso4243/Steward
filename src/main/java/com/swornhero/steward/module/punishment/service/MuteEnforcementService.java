package com.swornhero.steward.module.punishment.service;

import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class MuteEnforcementService {

    private MuteEnforcementService() {
        // Utility class
    }

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
                (message, sender, boundChatType) ->
                        allowChatMessage(sender)
        );
    }

    private static boolean allowChatMessage(
            ServerPlayer sender
    ) {
        PunishmentRecord activeMute =
                findActiveMute(sender.getUUID());

        if (activeMute == null) {
            return true;
        }

        sender.sendSystemMessage(
                Component.literal(
                        buildMutedMessage(activeMute)
                )
        );

        return false;
    }

    private static PunishmentRecord findActiveMute(
            UUID targetUuid
    ) {
        List<PunishmentRecord> activeMutes =
                PunishmentService.activePunishmentsFor(
                        targetUuid,
                        PunishmentType.MUTE
                );

        if (activeMutes.isEmpty()) {
            return null;
        }

        return activeMutes.getFirst();
    }

    private static String buildMutedMessage(
            PunishmentRecord record
    ) {
        StringBuilder message =
                new StringBuilder(
                        "You are currently muted."
                );

        message.append(
                " Reason: "
        );

        message.append(
                record.reason()
        );

        if (record.expiresAt() != null) {
            message.append(
                    ". Remaining: "
            );

            message.append(
                    formatRemainingTime(
                            record.expiresAt()
                    )
            );
        }

        message.append(
                ". Punishment ID: "
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