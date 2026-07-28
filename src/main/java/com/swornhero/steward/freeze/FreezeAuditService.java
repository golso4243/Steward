package com.swornhero.steward.freeze;

import com.swornhero.steward.Steward;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.Instant;

public final class FreezeAuditService {

    private FreezeAuditService() {
        // Utility class
    }

    public static void recordFreeze(FreezeRecord record) {
        Steward.LOGGER.info(
                "[Freeze] {} was frozen by {}. Reason: {}. "
                        + "Location: {}, {}, {}.",
                record.targetName(),
                record.frozenByName(),
                record.reason(),
                formatCoordinate(record.position().x()),
                formatCoordinate(record.position().y()),
                formatCoordinate(record.position().z())
        );
    }

    public static void recordDisconnect(FreezeRecord record) {
        Steward.LOGGER.warn(
                "[Freeze] {} disconnected while frozen. "
                        + "Disconnect count: {}.",
                record.targetName(),
                record.disconnectCount()
        );
    }

    public static void recordReconnect(FreezeRecord record) {
        Steward.LOGGER.info(
                "[Freeze] {} reconnected while still frozen. "
                        + "Reconnect count: {}.",
                record.targetName(),
                record.reconnectCount()
        );
    }

    public static void recordUnfreeze(
            FreezeRecord record,
            ServerPlayer staff
    ) {
        long durationSeconds = Duration.between(
                record.frozenAt(),
                Instant.now()
        ).getSeconds();

        Steward.LOGGER.info(
                "[Freeze] {} was unfrozen by {} after {} seconds. "
                        + "Reason: {}.",
                record.targetName(),
                staff.getName().getString(),
                durationSeconds,
                record.reason()
        );
    }

    private static String formatCoordinate(double coordinate) {
        return String.format("%.2f", coordinate);
    }
}