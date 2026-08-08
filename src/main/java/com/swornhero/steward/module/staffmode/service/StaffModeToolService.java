package com.swornhero.steward.module.staffmode.service;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class StaffModeToolService {

    private StaffModeToolService() {
        // Utility class
    }

    public static void giveDefaultTools(
            ServerPlayer player
    ) {
        if (player == null) {
            return;
        }

        Inventory inventory =
                player.getInventory();

        inventory.setItem(
                0,
                createTool(
                        Items.COMPASS,
                        "Player Browser"
                )
        );

        inventory.setItem(
                1,
                createTool(
                        Items.ENDER_PEARL,
                        "Teleport Tools"
                )
        );

        inventory.setItem(
                2,
                createTool(
                        Items.PACKED_ICE,
                        "Freeze"
                )
        );

        inventory.setItem(
                3,
                createTool(
                        Items.ANVIL,
                        "Punishments"
                )
        );

        inventory.setItem(
                4,
                createTool(
                        Items.SPYGLASS,
                        "Inspection"
                )
        );

        inventory.setItem(
                5,
                createTool(
                        Items.PHANTOM_MEMBRANE,
                        "Vanish"
                )
        );

        inventory.setItem(
                6,
                createTool(
                        Items.ECHO_SHARD,
                        "Staff Chat"
                )
        );

        inventory.setItem(
                7,
                createTool(
                        Items.NETHER_STAR,
                        "Staff Control"
                )
        );

        inventory.setItem(
                8,
                createTool(
                        Items.BARRIER,
                        "Exit Staff Mode"
                )
        );

        inventory.setSelectedSlot(0);
        inventory.setChanged();
    }

    private static ItemStack createTool(
            Item item,
            String name
    ) {
        ItemStack stack =
                new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        return stack;
    }
}