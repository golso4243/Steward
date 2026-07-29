package com.swornhero.steward.module.freeze.model;

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