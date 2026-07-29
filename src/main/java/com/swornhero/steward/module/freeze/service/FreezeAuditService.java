package com.swornhero.steward.module.freeze.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.module.freeze.model.FreezePosition;
import com.swornhero.steward.module.freeze.model.FreezeRecord;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;

public final class FreezeAuditService {

    private FreezeAuditService() {
        // Utility class
    }

    public static void recordFreeze(
            FreezeRecord record
    ) {
        Steward.LOGGER.info(
                "[Freeze:{}] {} was frozen by {}. "
                        + "Reason: {}. "
                        + "Location: {} at [{}, {}, {}].",
                shortId(record),
                record.targetName(),
                record.frozenByName(),
                record.reason(),
                record.position()
                        .dimension()
                        .identifier(),
                formatCoordinate(record.position().x()),
                formatCoordinate(record.position().y()),
                formatCoordinate(record.position().z())
        );
    }

    public static void recordDisconnect(
            FreezeRecord record
    ) {
        Steward.LOGGER.warn(
                "[Freeze:{}] {} disconnected while frozen. "
                        + "Disconnect count: {}.",
                shortId(record),
                record.targetName(),
                record.disconnectCount()
        );
    }

    public static void recordReconnect(
            FreezeRecord record
    ) {
        Steward.LOGGER.info(
                "[Freeze:{}] {} reconnected while still frozen. "
                        + "Reconnect count: {}.",
                shortId(record),
                record.targetName(),
                record.reconnectCount()
        );
    }

    public static void recordRelocation(
            FreezeRecord record,
            ServerPlayer staff,
            FreezePosition previousPosition,
            FreezePosition newPosition,
            String note
    ) {
        Steward.LOGGER.info(
                "[Freeze:{}] {} was relocated while frozen by {}. "
                        + "Original freeze location: {} at [{}, {}, {}]. "
                        + "Previous anchor: {} at [{}, {}, {}]. "
                        + "New anchor: {} at [{}, {}, {}]. "
                        + "Note: {}",
                shortId(record),
                record.targetName(),
                staff.getName().getString(),

                record.position()
                        .dimension()
                        .identifier(),
                formatCoordinate(record.position().x()),
                formatCoordinate(record.position().y()),
                formatCoordinate(record.position().z()),

                previousPosition
                        .dimension()
                        .identifier(),
                formatCoordinate(previousPosition.x()),
                formatCoordinate(previousPosition.y()),
                formatCoordinate(previousPosition.z()),

                newPosition
                        .dimension()
                        .identifier(),
                formatCoordinate(newPosition.x()),
                formatCoordinate(newPosition.y()),
                formatCoordinate(newPosition.z()),

                note
        );
    }

    public static void recordUnfreeze(
            FreezeRecord record,
            ServerPlayer staff
    ) {
        long durationSeconds =
                Math.max(
                        0L,
                        Duration.between(
                                record.frozenAt(),
                                Instant.now()
                        ).getSeconds()
                );

        Steward.LOGGER.info(
                "[Freeze:{}] {} was unfrozen by {} "
                        + "after {} seconds. Reason: {}.",
                shortId(record),
                record.targetName(),
                staff.getName().getString(),
                durationSeconds,
                record.reason()
        );
    }

    private static String shortId(
            FreezeRecord record
    ) {
        String fullId =
                record.freezeId()
                        .toString();

        return fullId.substring(
                0,
                8
        );
    }

    private static String formatCoordinate(
            double coordinate
    ) {
        return String.format(
                Locale.ROOT,
                "%.2f",
                coordinate
        );
    }
}