package com.swornhero.steward.freeze;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record FreezePositionEntry(
        String dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {

    public static FreezePositionEntry fromPosition(
            FreezePosition position
    ) {
        return new FreezePositionEntry(
                position.dimension()
                        .identifier()
                        .toString(),
                position.x(),
                position.y(),
                position.z(),
                position.yaw(),
                position.pitch()
        );
    }

    public FreezePosition toPosition() {
        if (dimension == null || dimension.isBlank()) {
            throw new IllegalStateException(
                    "Freeze position dimension is missing."
            );
        }

        if (!Double.isFinite(x)
                || !Double.isFinite(y)
                || !Double.isFinite(z)) {

            throw new IllegalStateException(
                    "Freeze position contains invalid coordinates."
            );
        }

        Identifier dimensionIdentifier =
                Identifier.parse(dimension);

        ResourceKey<Level> dimensionKey =
                ResourceKey.create(
                        Registries.DIMENSION,
                        dimensionIdentifier
                );

        return new FreezePosition(
                dimensionKey,
                x,
                y,
                z,
                yaw,
                pitch
        );
    }
}