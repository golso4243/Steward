package com.swornhero.steward.core.permission;

import com.swornhero.steward.Steward;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.Identifier;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class StaffHierarchyService {

    private StaffHierarchyService() {
        // Utility class
    }

    public static boolean requireCanAct(
            ServerPlayer actor,
            ServerPlayer target,
            Identifier bypassPermission
    ) {
        HierarchyResult result =
                checkLoadedUsers(
                        actor,
                        target,
                        bypassPermission
                );

        if (result.allowed()) {
            return true;
        }

        sendDenial(
                actor,
                result
        );

        return false;
    }

    public static CompletableFuture<HierarchyResult>
    checkCanActOnUuid(
            ServerPlayer actor,
            UUID targetUuid,
            Identifier bypassPermission
    ) {
        if (actor.getUUID().equals(targetUuid)) {
            return CompletableFuture.completedFuture(
                    HierarchyResult.denied(
                            getLoadedWeight(actor.getUUID()),
                            getLoadedWeight(targetUuid),
                            "You cannot perform this action on yourself."
                    )
            );
        }

        if (StewardPermissions.has(
                actor,
                bypassPermission
        )) {
            return CompletableFuture.completedFuture(
                    HierarchyResult.allowed(
                            getLoadedWeight(actor.getUUID()),
                            getLoadedWeight(targetUuid)
                    )
            );
        }

        LuckPerms luckPerms =
                LuckPermsProvider.get();

        CompletableFuture<User> actorFuture =
                loadUser(
                        luckPerms,
                        actor.getUUID()
                );

        CompletableFuture<User> targetFuture =
                loadUser(
                        luckPerms,
                        targetUuid
                );

        return actorFuture.thenCombine(
                targetFuture,
                StaffHierarchyService::compareUsers
        );
    }

    public static void sendDenial(
            ServerPlayer actor,
            HierarchyResult result
    ) {
        actor.sendSystemMessage(
                Component.literal(
                        "[Steward] "
                                + result.denialMessage()
                )
        );

        Steward.LOGGER.warn(
                "{} was denied a moderation action by hierarchy. "
                        + "Actor weight: {}, target weight: {}.",
                actor.getName().getString(),
                result.actorWeight(),
                result.targetWeight()
        );
    }

    private static HierarchyResult checkLoadedUsers(
            ServerPlayer actor,
            ServerPlayer target,
            Identifier bypassPermission
    ) {
        if (actor.getUUID().equals(target.getUUID())) {
            return HierarchyResult.denied(
                    getLoadedWeight(actor.getUUID()),
                    getLoadedWeight(target.getUUID()),
                    "You cannot perform this action on yourself."
            );
        }

        if (StewardPermissions.has(
                actor,
                bypassPermission
        )) {
            return HierarchyResult.allowed(
                    getLoadedWeight(actor.getUUID()),
                    getLoadedWeight(target.getUUID())
            );
        }

        LuckPerms luckPerms =
                LuckPermsProvider.get();

        User actorUser =
                luckPerms.getUserManager()
                        .getUser(actor.getUUID());

        User targetUser =
                luckPerms.getUserManager()
                        .getUser(target.getUUID());

        if (actorUser == null || targetUser == null) {
            return HierarchyResult.denied(
                    0,
                    0,
                    "Steward could not resolve the LuckPerms "
                            + "hierarchy for that action."
            );
        }

        return compareUsers(
                actorUser,
                targetUser
        );
    }

    private static CompletableFuture<User> loadUser(
            LuckPerms luckPerms,
            UUID playerUuid
    ) {
        User loadedUser =
                luckPerms.getUserManager()
                        .getUser(playerUuid);

        if (loadedUser != null) {
            return CompletableFuture.completedFuture(
                    loadedUser
            );
        }

        return luckPerms.getUserManager()
                .loadUser(playerUuid);
    }

    private static HierarchyResult compareUsers(
            User actorUser,
            User targetUser
    ) {
        int actorWeight =
                getWeight(actorUser);

        int targetWeight =
                getWeight(targetUser);

        if (actorWeight <= targetWeight) {
            return HierarchyResult.denied(
                    actorWeight,
                    targetWeight,
                    "You cannot moderate a player with an "
                            + "equal or higher staff rank."
            );
        }

        return HierarchyResult.allowed(
                actorWeight,
                targetWeight
        );
    }

    private static int getLoadedWeight(
            UUID playerUuid
    ) {
        LuckPerms luckPerms =
                LuckPermsProvider.get();

        User user =
                luckPerms.getUserManager()
                        .getUser(playerUuid);

        if (user == null) {
            return 0;
        }

        return getWeight(user);
    }

    private static int getWeight(
            User user
    ) {
        LuckPerms luckPerms =
                LuckPermsProvider.get();

        String primaryGroup =
                user.getPrimaryGroup();

        Group group =
                luckPerms.getGroupManager()
                        .getGroup(primaryGroup);

        if (group == null) {
            Steward.LOGGER.warn(
                    "LuckPerms primary group {} could not be "
                            + "resolved for user {}.",
                    primaryGroup,
                    user.getUniqueId()
            );

            return 0;
        }

        return group.getWeight()
                .orElse(0);
    }
}