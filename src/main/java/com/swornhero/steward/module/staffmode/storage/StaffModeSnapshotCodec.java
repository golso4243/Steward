package com.swornhero.steward.module.staffmode.storage;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.swornhero.steward.module.staffmode.model.StaffModeSnapshot;
import com.swornhero.steward.module.staffmode.model.StaffModeSnapshotEntry;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class StaffModeSnapshotCodec {

    private StaffModeSnapshotCodec() {
        // Utility class
    }

    public static StaffModeSnapshotEntry encode(
            StaffModeSnapshot snapshot,
            HolderLookup.Provider registries
    ) {
        if (snapshot == null) {
            throw new IllegalArgumentException(
                    "Staff Mode snapshot cannot be null."
            );
        }

        if (registries == null) {
            throw new IllegalArgumentException(
                    "Registry provider cannot be null."
            );
        }

        DynamicOps<JsonElement> ops =
                registries.createSerializationContext(
                        JsonOps.INSTANCE
                );

        List<String> encodedInventory =
                new ArrayList<>();

        for (ItemStack stack : snapshot.inventory()) {
            JsonElement encoded =
                    ItemStack.OPTIONAL_CODEC
                            .encodeStart(
                                    ops,
                                    stack
                            )
                            .getOrThrow(
                                    error ->
                                            new IllegalStateException(
                                                    "Failed to encode Staff Mode item: "
                                                            + error
                                            )
                            );

            encodedInventory.add(
                    encoded.toString()
            );
        }

        return new StaffModeSnapshotEntry(
                snapshot.staffUuid(),
                encodedInventory,
                snapshot.selectedSlot()
        );
    }

    public static StaffModeSnapshot decode(
            StaffModeSnapshotEntry entry,
            HolderLookup.Provider registries
    ) {
        if (entry == null) {
            throw new IllegalArgumentException(
                    "Staff Mode snapshot entry cannot be null."
            );
        }

        if (registries == null) {
            throw new IllegalArgumentException(
                    "Registry provider cannot be null."
            );
        }

        DynamicOps<JsonElement> ops =
                registries.createSerializationContext(
                        JsonOps.INSTANCE
                );

        List<ItemStack> inventory =
                new ArrayList<>();

        for (String encodedStack : entry.inventory()) {
            if (encodedStack == null
                    || encodedStack.isBlank()) {

                throw new IllegalStateException(
                        "Staff Mode snapshot contains blank item data."
                );
            }

            JsonElement element =
                    JsonParser.parseString(
                            encodedStack
                    );

            ItemStack stack =
                    ItemStack.OPTIONAL_CODEC
                            .parse(
                                    ops,
                                    element
                            )
                            .getOrThrow(
                                    error ->
                                            new IllegalStateException(
                                                    "Failed to decode Staff Mode item: "
                                                            + error
                                            )
                            );

            inventory.add(stack);
        }

        return new StaffModeSnapshot(
                entry.staffUuid(),
                inventory,
                entry.selectedSlot()
        );
    }
}