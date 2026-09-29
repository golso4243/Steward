package com.swornhero.steward.core.permission;

import com.swornhero.steward.Steward;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.group.Group;
import net.minecraft.resources.Identifier;

/**
 * LuckPerms on Fabric only lists permission nodes it has seen checked.
 * Checking every Steward node against the always-present default group
 * makes them appear in the editor and command suggestions immediately.
 * The check result is discarded, so no one's permissions change.
 */
public final class PermissionRegistrationService {

    private static final String DEFAULT_GROUP =
            "default";

    private PermissionRegistrationService() {
        // Utility class
    }

    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> announcePermissions()
        );
    }

    private static void announcePermissions() {
        LuckPerms luckPerms;

        try {
            luckPerms = LuckPermsProvider.get();
        } catch (IllegalStateException exception) {
            Steward.LOGGER.warn(
                    "LuckPerms is not loaded; Steward permission "
                            + "nodes were not registered with it."
            );

            return;
        }

        Group group =
                luckPerms.getGroupManager()
                        .getGroup(DEFAULT_GROUP);

        if (group == null) {
            Steward.LOGGER.warn(
                    "LuckPerms group {} could not be resolved; "
                            + "Steward permission nodes were not "
                            + "registered with it.",
                    DEFAULT_GROUP
            );

            return;
        }

        CachedPermissionData permissionData =
                group.getCachedData()
                        .getPermissionData();

        for (Identifier permission : StewardPermissions.all()) {
            permissionData.checkPermission(
                    StewardPermissions.toNode(permission)
            );
        }

        Steward.LOGGER.info(
                "Registered {} permission nodes with LuckPerms.",
                StewardPermissions.all().size()
        );
    }
}
