package com.swornhero.steward.module.staffmode.service;

import com.swornhero.steward.module.staffmode.model.StaffModeSnapshot;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class StaffModeSnapshotService {

    private StaffModeSnapshotService() {
        // Utility class
    }

    public static StaffModeSnapshot capture(
            ServerPlayer player
    ) {
        if (player == null) {
            throw new IllegalArgumentException(
                    "Player cannot be null."
            );
        }

        Inventory inventory =
                player.getInventory();

        List<ItemStack> contents =
                new ArrayList<>();

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            contents.add(
                    inventory.getItem(slot).copy()
            );
        }

        return new StaffModeSnapshot(
                player.getUUID(),
                contents,
                inventory.getSelectedSlot()
        );
    }

    public static boolean restore(
            ServerPlayer player,
            StaffModeSnapshot snapshot
    ) {
        if (player == null || snapshot == null) {
            return false;
        }

        if (!player.getUUID().equals(
                snapshot.staffUuid()
        )) {
            return false;
        }

        Inventory inventory =
                player.getInventory();

        List<ItemStack> contents =
                snapshot.inventory();

        if (contents.size()
                != inventory.getContainerSize()) {

            return false;
        }

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            inventory.setItem(
                    slot,
                    contents.get(slot).copy()
            );
        }

        inventory.setSelectedSlot(
                snapshot.selectedSlot()
        );

        inventory.setChanged();

        return true;
    }

    public static void prepareStaffInventory(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        Inventory inventory =
                player.getInventory();

        for (int slot = 0;
             slot < inventory.getContainerSize();
             slot++) {

            inventory.setItem(
                    slot,
                    ItemStack.EMPTY
            );
        }

        inventory.setSelectedSlot(0);
        inventory.setChanged();
    }
}