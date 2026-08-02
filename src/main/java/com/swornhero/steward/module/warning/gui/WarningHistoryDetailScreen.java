package com.swornhero.steward.module.warning.gui;

import com.swornhero.steward.core.history.GlobalHistoryView;
import com.swornhero.steward.core.history.ModerationHistoryScreen;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
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
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.history.GlobalModerationHistoryScreen;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.UUID;

public final class WarningHistoryDetailScreen {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                    "MMM d, yyyy h:mm:ss a"
            ).withZone(
                    ZoneId.systemDefault()
            );

    private WarningHistoryDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            WarningRecord record
    ) {
        open(
                viewer,
                targetUuid,
                browserPage,
                historyPage,
                record,
                HistoryReturnTarget.WARNING_HISTORY
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            WarningRecord record,
            HistoryReturnTarget returnTarget
    ) {
        if (record == null) {
            returnToSource(
                    viewer,
                    targetUuid,
                    browserPage,
                    historyPage,
                    returnTarget
            );

            return;
        }

        SimpleContainer container =
                new SimpleContainer(
                        WarningHistoryDetailMenu.MENU_SIZE
                );

        populate(
                container,
                record
        );

        Component title =
                Component.literal(
                        "Warning Details • "
                                + record.targetName()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new WarningHistoryDetailMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        record,
                                        returnTarget
                                ),
                        title
                )
        );
    }

    private static void populate(
            SimpleContainer container,
            WarningRecord record
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
                "Warning ID: "
                        + WarningService.formatWarningId(
                        record.warningId()
                )
        );

        setButton(
                container,
                12,
                Items.PAPER,
                "Status: "
                        + record.status().displayName()
        );

        setButton(
                container,
                14,
                warningLevelIcon(record),
                "Level: "
                        + record.level().displayName()
                        + " • "
                        + record.level().points()
                        + " Points"
        );

        setButton(
                container,
                15,
                Items.WRITABLE_BOOK,
                "Category: "
                        + record.category().displayName()
        );

        setButton(
                container,
                16,
                Items.BOOK,
                "Reason: "
                        + record.reason()
        );

        setButton(
                container,
                20,
                Items.PLAYER_HEAD,
                "Issued By: "
                        + record.issuedByName()
        );

        setButton(
                container,
                21,
                Items.CLOCK,
                "Issued: "
                        + formatDate(
                        record.issuedAt()
                )
        );

        setButton(
                container,
                22,
                Items.RECOVERY_COMPASS,
                "Expires: "
                        + formatExpiration(record)
        );

        Item acknowledgmentItem =
                BuiltInRegistries.ITEM.getValue(
                        Identifier.fromNamespaceAndPath(
                                "minecraft",
                                record.acknowledged()
                                        ? "lime_concrete"
                                        : "gray_concrete"
                        )
                );

        String acknowledgmentText =
                record.acknowledged()
                        ? "Acknowledged: "
                        + formatDate(
                        record.acknowledgedAt()
                )
                        : "Not Acknowledged";

        setButton(
                container,
                23,
                acknowledgmentItem,
                acknowledgmentText
        );

        setButton(
                container,
                24,
                record.targetWasOnline()
                        ? Items.ENDER_EYE
                        : Items.ENDER_PEARL,
                record.targetWasOnline()
                        ? "Player Was Online"
                        : "Player Was Offline"
        );

        addLifecycleDetails(
                container,
                record
        );

        addOptionalDetails(
                container,
                record
        );

        setButton(
                container,
                WarningHistoryDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                "Back to History"
        );

        setButton(
                container,
                WarningHistoryDetailMenu.PROFILE_SLOT,
                Items.PLAYER_HEAD,
                "Player Profile"
        );

        setButton(
                container,
                WarningHistoryDetailMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static void addLifecycleDetails(
            SimpleContainer container,
            WarningRecord record
    ) {
        if (record.revokedAt() != null) {
            setButton(
                    container,
                    29,
                    BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(
                                    "minecraft",
                                    "red_concrete"
                            )
                    ),
                    "Revoked By: "
                            + safeText(
                            record.revokedByName(),
                            "Unknown"
                    )
            );

            setButton(
                    container,
                    30,
                    Items.CLOCK,
                    "Revoked: "
                            + formatDate(
                            record.revokedAt()
                    )
            );

            setButton(
                    container,
                    31,
                    Items.WRITTEN_BOOK,
                    "Revocation Reason: "
                            + safeText(
                            record.revocationReason(),
                            "Not provided"
                    )
            );
        }

        if (record.escalatedAt() != null) {
            setButton(
                    container,
                    33,
                    Items.ANVIL,
                    "Escalated: "
                            + formatDate(
                            record.escalatedAt()
                    )
            );
        }
    }

    private static void addOptionalDetails(
            SimpleContainer container,
            WarningRecord record
    ) {
        if (record.staffNotes() != null
                && !record.staffNotes().isBlank()) {

            setButton(
                    container,
                    38,
                    Items.WRITTEN_BOOK,
                    "Staff Notes: "
                            + record.staffNotes()
            );
        }

        if (record.evidenceReference() != null
                && !record.evidenceReference().isBlank()) {

            setButton(
                    container,
                    42,
                    Items.SPYGLASS,
                    "Evidence: "
                            + record.evidenceReference()
            );
        }
    }

    private static Item warningLevelIcon(
            WarningRecord record
    ) {
        if (record.level() == null) {
            return Items.PAPER;
        }

        return switch (record.level()) {
            case VERBAL ->
                    Items.PAPER;

            case FORMAL ->
                    Items.WRITABLE_BOOK;

            case FINAL ->
                    Items.ENCHANTED_BOOK;
        };
    }

    private static String formatExpiration(
            WarningRecord record
    ) {
        if (record.expiresAt() == null) {
            return "Never";
        }

        return formatDate(
                record.expiresAt()
        );
    }

    private static String formatDate(
            java.time.Instant instant
    ) {
        if (instant == null) {
            return "Not recorded";
        }

        return DATE_FORMAT.format(instant);
    }

    private static String safeText(
            String value,
            String fallback
    ) {
        if (value == null || value.isBlank()) {
            return fallback;
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
             slot < WarningHistoryDetailMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == WarningHistoryDetailMenu.ROWS - 1
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

    private static void returnToSource(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        switch (returnTarget) {
            case ALL_ACTIVITY ->
                    ModerationHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            historyPage
                    );

            case GLOBAL_ALL_ACTIVITY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            historyPage
                    );

            case GLOBAL_WARNING_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.WARNING_HISTORY,
                            historyPage
                    );

            case GLOBAL_FREEZE_HISTORY ->
                    GlobalModerationHistoryScreen.open(
                            viewer,
                            GlobalHistoryView.FREEZE_HISTORY,
                            historyPage
                    );

            case WARNING_HISTORY, FREEZE_HISTORY ->
                    WarningHistoryScreen.open(
                            viewer,
                            targetUuid,
                            browserPage,
                            historyPage
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