package com.swornhero.steward.core.permission;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

public final class StewardPermissions {

    public static final Identifier STAFF_OPEN =
            create("staff.open");

    public static final Identifier STATUS_VIEW =
            create("status.view");

    public static final Identifier HISTORY_VIEW =
            create("history.view");

    public static final Identifier STAFF_MODE_USE =
            create("staff-mode.use");

    public static final Identifier TELEPORT_USE =
            create("teleport.use");

    public static final Identifier TELEPORT_OTHERS =
            create("teleport.others");

    public static final Identifier FREEZE_USE =
            create("freeze.use");

    public static final Identifier FREEZE_VIEW =
            create("freeze.view");

    public static final Identifier FREEZE_UNFREEZE =
            create("freeze.unfreeze");

    public static final Identifier FREEZE_UNFREEZE_OFFLINE =
            create("freeze.unfreeze.offline");

    public static final Identifier FREEZE_HISTORY =
            create("freeze.history");

    public static final Identifier FREEZE_MANAGE =
            create("freeze.manage");

    public static final Identifier FREEZE_RELOCATE =
            create("freeze.relocate");

    public static final Identifier FREEZE_ALERTS =
            create("freeze.alerts");

    public static final Identifier FREEZE_BYPASS_HIERARCHY =
            create("freeze.bypass-hierarchy");

    public static final Identifier WARNING_ISSUE =
            create("warning.issue");

    public static final Identifier WARNING_VIEW =
            create("warning.view");

    public static final Identifier WARNING_HISTORY =
            create("warning.history");

    public static final Identifier WARNING_REVOKE =
            create("warning.revoke");

    public static final Identifier WARNING_MANAGE =
            create("warning.manage");

    public static final Identifier WARNING_ALERTS =
            create("warning.alerts");

    public static final Identifier WARNING_BYPASS_HIERARCHY =
            create("warning.bypass-hierarchy");

    public static final Identifier PUNISHMENT_VIEW =
            create("punishment.view");

    public static final Identifier PUNISHMENT_MANAGE =
            create("punishment.manage");

    public static final Identifier PUNISHMENT_MUTE =
            create("punishment.mute");

    public static final Identifier PUNISHMENT_KICK =
            create("punishment.kick");

    public static final Identifier PUNISHMENT_TEMPORARY_BAN =
            create("punishment.temporary-ban");

    public static final Identifier PUNISHMENT_PERMANENT_BAN =
            create("punishment.permanent-ban");

    public static final Identifier PUNISHMENT_REVOKE =
            create("punishment.revoke");

    public static final Identifier PUNISHMENT_BYPASS_HIERARCHY =
            create("punishment.bypass-hierarchy");

    private StewardPermissions() {
        // Utility class
    }

    public static boolean has(
            ServerPlayer player,
            Identifier permission
    ) {
        return player.checkPermission(
                permission,
                PermissionLevel.GAMEMASTERS
        );
    }

    public static boolean has(
            CommandSourceStack source,
            Identifier permission
    ) {
        return source.checkPermission(
                permission,
                PermissionLevel.GAMEMASTERS
        );
    }

    public static boolean require(
            ServerPlayer player,
            Identifier permission
    ) {
        if (has(player, permission)) {
            return true;
        }

        player.sendSystemMessage(
                Component.literal(
                        "[Steward] You do not have permission "
                                + "to perform that action."
                )
        );

        return false;
    }

    private static Identifier create(String path) {
        return Identifier.fromNamespaceAndPath(
                "steward",
                path
        );
    }
}