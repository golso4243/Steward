package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import com.swornhero.steward.module.punishment.service.PunishmentService;
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

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class ActivePunishmentDetailScreen {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm:ss a"
                    )
                    .withZone(
                            ZoneId.systemDefault()
                    );

    private ActivePunishmentDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID punishmentId,
            int activePunishmentPage
    ) {
        if (!StewardPermissions.require(
                viewer,
                StewardPermissions.PUNISHMENT_VIEW
        )) {
            return;
        }

        PunishmentRecord record =
                PunishmentService.findById(
                        punishmentId
                );

        if (record == null || !record.isActive()) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That punishment is no longer active."
                    )
            );

            ActivePunishmentScreen.open(
                    viewer,
                    activePunishmentPage
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        ActivePunishmentDetailMenu.MENU_SIZE
                );

        populate(
                container,
                record
        );

        Component title =
                Component.literal(
                        record.type().displayName()
                                + " • "
                                + record.targetName()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new ActivePunishmentDetailMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        punishmentId,
                                        activePunishmentPage
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            PunishmentRecord record
    ) {
        addBorder(container);

        setButton(
                container,
                10,
                Items.PLAYER_HEAD,
                "Player: "
                        + record.targetName()
        );

        setButton(
                container,
                11,
                Items.NAME_TAG,
                "Punishment ID: "
                        + PunishmentService.formatPunishmentId(
                        record.punishmentId()
                )
        );

        setButton(
                container,
                12,
                itemFor(record.type()),
                "Type: "
                        + record.type().displayName()
        );

        setButton(
                container,
                13,
                Items.REDSTONE_TORCH,
                "Status: "
                        + record.status().displayName()
        );

        setButton(
                container,
                14,
                Items.CLOCK,
                "Issued At: "
                        + DATE_FORMAT.format(
                        record.issuedAt()
                )
        );

        setButton(
                container,
                15,
                Items.WRITABLE_BOOK,
                "Issued By: "
                        + record.issuedByName()
        );

        setButton(
                container,
                16,
                Items.PAPER,
                "Target Was: "
                        + (
                        record.targetWasOnline()
                                ? "Online"
                                : "Offline"
                )
        );

        setButton(
                container,
                19,
                Items.BOOK,
                "Reason: "
                        + record.reason()
        );

        setButton(
                container,
                20,
                Items.COMPASS,
                "Duration: "
                        + formatDuration(record)
        );

        setButton(
                container,
                21,
                Items.CLOCK,
                "Expires: "
                        + formatExpiration(record)
        );

        setButton(
                container,
                23,
                Items.WRITABLE_BOOK,
                "Staff Notes: "
                        + displayOptional(
                        record.staffNotes()
                )
        );

        setButton(
                container,
                24,
                Items.MAP,
                "Evidence: "
                        + displayOptional(
                        record.evidenceReference()
                )
        );

        setButton(
                container,
                25,
                Items.CLOCK,
                "Time Remaining: "
                        + formatRemaining(record)
        );

        setButton(
                container,
                ActivePunishmentDetailMenu.REVOKE_SLOT,
                Items.MAGMA_CREAM,
                "Revoke Punishment"
        );

        setButton(
                container,
                ActivePunishmentDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to Active Punishments"
        );

        setButton(
                container,
                ActivePunishmentDetailMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static Item itemFor(
            PunishmentType type
    ) {
        return switch (type) {
            case MUTE ->
                    Items.NAME_TAG;

            case KICK ->
                    Items.LEATHER_BOOTS;

            case TEMPORARY_BAN ->
                    Items.CLOCK;

            case PERMANENT_BAN ->
                    Items.BARRIER;
        };
    }

    private static String formatDuration(
            PunishmentRecord record
    ) {
        if (record.expiresAt() == null) {
            return "Permanent";
        }

        Duration duration =
                Duration.between(
                        record.issuedAt(),
                        record.expiresAt()
                );

        long totalHours =
                Math.max(
                        0,
                        duration.toHours()
                );

        long days =
                totalHours / 24;

        long hours =
                totalHours % 24;

        if (days > 0) {
            return days
                    + "d "
                    + hours
                    + "h";
        }

        return totalHours + "h";
    }

    private static String formatExpiration(
            PunishmentRecord record
    ) {
        if (record.expiresAt() == null) {
            return "Never";
        }

        return DATE_FORMAT.format(
                record.expiresAt()
        );
    }

    private static String formatRemaining(
            PunishmentRecord record
    ) {
        if (record.expiresAt() == null) {
            return "Permanent";
        }

        long totalSeconds =
                Math.max(
                        0,
                        Duration.between(
                                Instant.now(),
                                record.expiresAt()
                        ).getSeconds()
                );

        long days =
                totalSeconds / 86_400;

        long hours =
                (totalSeconds % 86_400) / 3_600;

        long minutes =
                (totalSeconds % 3_600) / 60;

        if (days > 0) {
            return days
                    + "d "
                    + hours
                    + "h";
        }

        if (hours > 0) {
            return hours
                    + "h "
                    + minutes
                    + "m";
        }

        return minutes + "m";
    }

    private static String displayOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "None";
        }

        return value;
    }

    private static void addBorder(
            SimpleContainer container
    ) {
        Item borderItem =
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                "purple_stained_glass_pane"
                        )
                );

        for (int slot = 0;
             slot < ActivePunishmentDetailMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == ActivePunishmentDetailMenu.ROWS - 1
                            || column == 0
                            || column == 8;

            if (!border) {
                continue;
            }

            ItemStack pane =
                    new ItemStack(borderItem);

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

    private static void setButton(
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