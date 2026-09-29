package com.swornhero.steward.module.report.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import net.minecraft.server.level.ServerPlayer;

import java.util.UUID;

/**
 * Reports filed against one player, opened from the player profile.
 */
public final class PlayerReportsScreen {

    private PlayerReportsScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            ServerPlayer target,
            int browserPage
    ) {
        UUID targetUuid = target.getUUID();

        ReportQueueScreen.openForTarget(
                viewer,
                targetUuid,
                target.getName().getString(),
                0,
                player -> PlayerProfileScreen.open(player, targetUuid, browserPage),
                "Back to Profile"
        );
    }
}
