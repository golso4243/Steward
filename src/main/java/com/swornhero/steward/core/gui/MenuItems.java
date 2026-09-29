package com.swornhero.steward.core.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class MenuItems {

    /**
     * The 28 inner slots of a bordered 9x6 menu, in reading order.
     */
    public static final int[] CONTENT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private static final int LORE_LINE_LENGTH = 48;

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
                    .withZone(ZoneId.systemDefault());

    private MenuItems() {
        // Utility class
    }

    public static String formatDate(Instant instant) {
        return instant != null ? DATE_FORMAT.format(instant) : "Unknown";
    }

    public static SimpleContainer borderedContainer() {
        SimpleContainer container =
                new SimpleContainer(StewardMenu.MENU_SIZE);

        Item borderItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "purple_stained_glass_pane"
                )
        );

        for (int slot = 0; slot < StewardMenu.MENU_SIZE; slot++) {
            int row = slot / 9;
            int column = slot % 9;

            boolean border = row == 0
                    || row == StewardMenu.ROWS - 1
                    || column == 0
                    || column == 8;

            if (!border) {
                continue;
            }

            ItemStack pane = new ItemStack(borderItem);

            pane.set(
                    DataComponents.TOOLTIP_DISPLAY,
                    new TooltipDisplay(true, new LinkedHashSet<>())
            );

            container.setItem(slot, pane);
        }

        return container;
    }

    public static void setButton(
            SimpleContainer container,
            int slot,
            Item item,
            String name,
            String... loreLines
    ) {
        ItemStack stack = new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        List<Component> lore = new ArrayList<>();

        for (String line : loreLines) {
            if (line == null) {
                continue;
            }

            for (String wrapped : wrap(line)) {
                lore.add(
                        Component.literal(wrapped)
                                .withStyle(style -> style
                                        .withItalic(false)
                                        .withColor(ChatFormatting.GRAY))
                );
            }
        }

        if (!lore.isEmpty()) {
            stack.set(
                    DataComponents.LORE,
                    new ItemLore(
                            lore.subList(
                                    0,
                                    Math.min(lore.size(), ItemLore.MAX_LINES)
                            )
                    )
            );
        }

        LinkedHashSet<DataComponentType<?>> hidden =
                new LinkedHashSet<>();

        hidden.add(DataComponents.ATTRIBUTE_MODIFIERS);

        stack.set(
                DataComponents.TOOLTIP_DISPLAY,
                new TooltipDisplay(false, hidden)
        );

        container.setItem(slot, stack);
    }

    public static void setPagination(
            SimpleContainer container,
            int page,
            int totalPages,
            String backLabel
    ) {
        if (page > 0) {
            setButton(
                    container,
                    StewardMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                StewardMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page " + (page + 1) + " of " + totalPages
        );

        if (page + 1 < totalPages) {
            setButton(
                    container,
                    StewardMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                StewardMenu.BACK_SLOT,
                Items.OAK_DOOR,
                backLabel
        );

        setButton(
                container,
                StewardMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    public static int totalPages(int recordCount) {
        return Math.max(
                1,
                (int) Math.ceil(recordCount / (double) CONTENT_SLOTS.length)
        );
    }

    public static int clampPage(int requestedPage, int totalPages) {
        return Math.max(0, Math.min(requestedPage, totalPages - 1));
    }

    private static List<String> wrap(String text) {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (String word : text.split(" ")) {
            if (current.length() > 0
                    && current.length() + word.length() + 1 > LORE_LINE_LENGTH) {
                lines.add(current.toString());
                current.setLength(0);
            }

            if (current.length() > 0) {
                current.append(' ');
            }

            current.append(word);
        }

        if (current.length() > 0 || lines.isEmpty()) {
            lines.add(current.toString());
        }

        return lines;
    }
}
