package com.swornhero.steward.freeze;

import net.minecraft.resources.Identifier;

public record FreezePosition(
        Identifier dimension,
        double x,
        double y,
        double z,
        float yaw,
        float pitch
) {
}