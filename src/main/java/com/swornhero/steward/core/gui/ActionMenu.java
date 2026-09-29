package com.swornhero.steward.core.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;

import java.util.Map;
import java.util.function.Consumer;

/**
 * A read-only menu whose slots map directly to server-side actions.
 * The optional permission is rechecked on every click so a revoked
 * permission takes effect even while the menu is open.
 */
public final class ActionMenu extends StewardMenu {

    private final Map<Integer, Consumer<ServerPlayer>> actions;
    private final Identifier requiredPermission;

    private ActionMenu(
            int containerId,
            Inventory playerInventory,
            Container container,
            Map<Integer, Consumer<ServerPlayer>> actions,
            Identifier requiredPermission
    ) {
        super(containerId, playerInventory, container);

        this.actions = Map.copyOf(actions);
        this.requiredPermission = requiredPermission;
    }

    public static void open(
            ServerPlayer viewer,
            String title,
            Container container,
            Map<Integer, Consumer<ServerPlayer>> actions,
            Identifier requiredPermission
    ) {
        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ActionMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        actions,
                                        requiredPermission
                                ),
                        Component.literal(title)
                )
        );
    }

    @Override
    protected void onSlotClicked(ServerPlayer viewer, int slot) {
        if (requiredPermission != null
                && !StewardPermissions.require(viewer, requiredPermission)) {
            viewer.closeContainer();
            return;
        }

        Consumer<ServerPlayer> action = actions.get(slot);

        if (action != null) {
            action.accept(viewer);
        }
    }
}
