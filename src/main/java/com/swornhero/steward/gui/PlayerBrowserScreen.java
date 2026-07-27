package com.swornhero.steward.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class PlayerBrowserScreen {
    public static final int PLAYERS_PER_PAGE = 28;

    private static final int[] PLAYER_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private PlayerBrowserScreen() {
        // Utility class
    }

    public static void open(ServerPlayer viewer) {
        open(viewer, 0);
    }

    public static void open(ServerPlayer viewer, int requestedPage) {
        List<ServerPlayer> onlinePlayers =
                new ArrayList<>(
                        viewer.level()
                                .getServer()
                                .getPlayerList()
                                .getPlayers()
                );

        onlinePlayers.sort(
                (first, second) ->
                        first.getName()
                                .getString()
                                .compareToIgnoreCase(
                                        second.getName().getString()
                                )
        );

        int totalPages = Math.max(
                1,
                (int) Math.ceil(
                        onlinePlayers.size()
                                / (double) PLAYERS_PER_PAGE
                )
        );

        int page = Math.max(
                0,
                Math.min(requestedPage, totalPages - 1)
        );

        SimpleContainer container =
                new SimpleContainer(PlayerBrowserMenu.MENU_SIZE);

        Map<Integer, UUID> playerSlots = new LinkedHashMap<>();

        populate(
                container,
                onlinePlayers,
                playerSlots,
                page,
                totalPages
        );

        Component title = Component.literal(
                "Steward • Players "
                        + (page + 1)
                        + "/"
                        + totalPages
        );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PlayerBrowserMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        playerSlots,
                                        page,
                                        totalPages
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            List<ServerPlayer> onlinePlayers,
            Map<Integer, UUID> playerSlots,
            int page,
            int totalPages
    ) {
        addBorder(container);

        int startIndex = page * PLAYERS_PER_PAGE;
        int endIndex = Math.min(
                startIndex + PLAYERS_PER_PAGE,
                onlinePlayers.size()
        );

        for (int playerIndex = startIndex;
             playerIndex < endIndex;
             playerIndex++) {

            int pageIndex = playerIndex - startIndex;
            int slot = PLAYER_SLOTS[pageIndex];

            ServerPlayer target = onlinePlayers.get(playerIndex);

            ItemStack playerHead =
                    new ItemStack(Items.PLAYER_HEAD);

            playerHead.set(
                    DataComponents.CUSTOM_NAME,
                    Component.literal(
                            target.getName().getString()
                    )
            );

            container.setItem(slot, playerHead);
            playerSlots.put(slot, target.getUUID());
        }

        if (onlinePlayers.isEmpty()) {
            setButton(
                    container,
                    22,
                    Items.BARRIER,
                    "No Players Online"
            );
        }

        if (page > 0) {
            setButton(
                    container,
                    PlayerBrowserMenu.PREVIOUS_PAGE_SLOT,
                    Items.ARROW,
                    "Previous Page"
            );
        }

        setButton(
                container,
                PlayerBrowserMenu.PAGE_INFO_SLOT,
                Items.PAPER,
                "Page " + (page + 1) + " of " + totalPages
        );

        if (page + 1 < totalPages) {
            setButton(
                    container,
                    PlayerBrowserMenu.NEXT_PAGE_SLOT,
                    Items.ARROW,
                    "Next Page"
            );
        }

        setButton(
                container,
                PlayerBrowserMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back"
        );

        setButton(
                container,
                PlayerBrowserMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static void addBorder(SimpleContainer container) {
        Item borderItem = BuiltInRegistries.ITEM.getValue(
                Identifier.fromNamespaceAndPath(
                        "minecraft",
                        "purple_stained_glass_pane"
                )
        );

        for (int slot = 0;
             slot < PlayerBrowserMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row == PlayerBrowserMenu.ROWS - 1
                            || column == 0
                            || column == 8;

            if (border) {
                ItemStack pane = new ItemStack(borderItem);

                pane.set(
                        DataComponents.TOOLTIP_DISPLAY,
                        new TooltipDisplay(
                                true,
                                new LinkedHashSet<>()
                        )
                );

                container.setItem(slot, pane);
            }
        }
    }

    private static void setButton(
            SimpleContainer container,
            int slot,
            Item item,
            String name
    ) {
        ItemStack stack = new ItemStack(item);

        stack.set(
                DataComponents.CUSTOM_NAME,
                Component.literal(name)
        );

        container.setItem(slot, stack);
    }
}