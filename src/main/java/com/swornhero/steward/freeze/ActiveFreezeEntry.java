package com.swornhero.steward.freeze;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.time.Instant;
import java.util.UUID;

public record ActiveFreezeEntry(
        UUID targetUuid,
        String targetName,
        UUID frozenByUuid,
        String frozenByName,
        String reason,
        String dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        Instant frozenAt,
        int disconnectCount,
        int reconnectCount
) {
    public static ActiveFreezeEntry fromRecord(
            FreezeRecord record
    ) {
        return new ActiveFreezeEntry(
                record.targetUuid(),
                record.targetName(),
                record.frozenByUuid(),
                record.frozenByName(),
                record.reason(),
                record.position()
                        .dimension()
                        .identifier()
                        .toString(),
                record.position().x(),
                record.position().y(),
                record.position().z(),
                record.position().yaw(),
                record.position().pitch(),
                record.frozenAt(),
                record.disconnectCount(),
                record.reconnectCount()
        );
    }

    public FreezeRecord toRecord() {
        Identifier dimensionIdentifier =
                Identifier.parse(dimension);

        ResourceKey<Level> dimensionKey =
                ResourceKey.create(
                        Registries.DIMENSION,
                        dimensionIdentifier
                );

        FreezePosition position =
                new FreezePosition(
                        dimensionKey,
                        x,
                        y,
                        z,
                        yaw,
                        pitch
                );

        FreezeRecord record =
                new FreezeRecord(
                        targetUuid,
                        targetName,
                        frozenByUuid,
                        frozenByName,
                        position,
                        reason,
                        frozenAt
                );

        record.restoreConnectionCounts(
                disconnectCount,
                reconnectCount
        );

        return record;
    }
}