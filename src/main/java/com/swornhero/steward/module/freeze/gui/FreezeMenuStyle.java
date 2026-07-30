package com.swornhero.steward.module.freeze.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.LinkedHashSet;

public final class FreezeMenuStyle {

    private static final Item BORDER_ITEM =
            BuiltInRegistries.ITEM.getValue(
                    Identifier.fromNamespaceAndPath(
                            "minecraft",
                            "purple_stained_glass_pane"
                    )
            );

    private FreezeMenuStyle() {
        // Utility class
    }

    public static void addBorder(
            SimpleContainer container,
            int rows
    ) {
        int menuSize = rows * 9;

        for (int slot = 0;
             slot < menuSize;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row == rows - 1
                            || column == 0
                            || column == 8;

            if (!border) {
                continue;
            }

            ItemStack pane =
                    new ItemStack(BORDER_ITEM);

            pane.set(
                    DataComponents.TOOLTIP_DISPLAY,
                    new TooltipDisplay(
                            true,
                            new LinkedHashSet<>()
                    )
            );

            container.setItem(
                    slot,
                    pane
            );
        }
    }

    public static void setButton(
            SimpleContainer container,
            int slot,
            Item item,
            String name
    ) {
        ItemStack stack =
                new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        container.setItem(
                slot,
                stack
        );
    }
}