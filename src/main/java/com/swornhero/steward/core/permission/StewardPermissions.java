package com.swornhero.steward.core.permission;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionLevel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class StewardPermissions {

    // Must stay above the permission constants so create() can populate it.
    private static final List<Identifier> ALL =
            new ArrayList<>();

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

    public static final Identifier TELEPORT_BYPASS_HIERARCHY =
            create("teleport.bypass-hierarchy");

    public static final Identifier INSPECTION_VIEW =
            create("inspection.view");

    public static final Identifier INSPECTION_BYPASS_HIERARCHY =
            create("inspection.bypass-hierarchy");

    public static final Identifier VANISH_USE =
            create("vanish.use");

    public static final Identifier VANISH_SEE =
            create("vanish.see");

    public static final Identifier VANISH_NOTIFICATIONS =
            create("vanish.notifications");

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

    public static final Identifier STAFF_CHAT_USE =
            create("staff-chat.use");

    public static final Identifier STAFF_CHAT_HISTORY =
            create("staff-chat.history");

    public static final Identifier REPORT_SUBMIT =
            create("report.submit");

    public static final Identifier REPORT_VIEW =
            create("report.view");

    public static final Identifier REPORT_MANAGE =
            create("report.manage");

    public static final Identifier REPORT_OVERRIDE_CLAIM =
            create("report.override-claim");

    public static final Identifier REPORT_ALERTS =
            create("report.alerts");

    public static final Identifier REPORT_BYPASS_COOLDOWN =
            create("report.bypass-cooldown");

    public static final Identifier NOTES_VIEW =
            create("notes.view");

    public static final Identifier NOTES_CREATE =
            create("notes.create");

    public static final Identifier NOTES_ARCHIVE_OWN =
            create("notes.archive-own");

    public static final Identifier NOTES_MANAGE =
            create("notes.manage");

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

    /**
     * Checks a permission that every player holds unless a
     * permission provider explicitly denies it.
     */
    public static boolean hasDefault(
            CommandSourceStack source,
            Identifier permission
    ) {
        return source.checkPermission(
                permission,
                PermissionLevel.ALL
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

    public static List<Identifier> all() {
        return Collections.unmodifiableList(ALL);
    }

    public static String toNode(Identifier permission) {
        return permission.getNamespace()
                + "."
                + permission.getPath();
    }

    private static Identifier create(String path) {
        Identifier permission =
                Identifier.fromNamespaceAndPath(
                        "steward",
                        path
                );

        ALL.add(permission);

        return permission;
    }
}
