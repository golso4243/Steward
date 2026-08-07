package com.swornhero.steward.module.staffmode.model;

import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class StaffModeSnapshot {

    private final UUID staffUuid;

    private final List<ItemStack> inventory;
    private final int selectedSlot;

    public StaffModeSnapshot(
            UUID staffUuid,
            List<ItemStack> inventory,
            int selectedSlot
    ) {
        if (staffUuid == null) {
            throw new IllegalArgumentException(
                    "Staff UUID cannot be null."
            );
        }

        if (inventory == null) {
            throw new IllegalArgumentException(
                    "Inventory snapshot cannot be null."
            );
        }

        this.staffUuid = staffUuid;

        this.inventory = inventory.stream()
                .map(ItemStack::copy)
                .toList();

        this.selectedSlot = selectedSlot;
    }

    public UUID staffUuid() {
        return staffUuid;
    }

    public List<ItemStack> inventory() {
        return inventory.stream()
                .map(ItemStack::copy)
                .toList();
    }

    public int selectedSlot() {
        return selectedSlot;
    }
}