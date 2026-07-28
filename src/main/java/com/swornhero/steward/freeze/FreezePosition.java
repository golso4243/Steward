package com.swornhero.steward.freeze;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public record FreezePosition(
        ResourceKey<Level> dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}