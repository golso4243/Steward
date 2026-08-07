package com.swornhero.steward.module.punishment.gui;

import com.swornhero.steward.core.gui.PlayerProfileScreen;
import com.swornhero.steward.core.gui.StaffControlScreen;
import com.swornhero.steward.core.history.HistoryReturnTarget;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentStatus;
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

public final class PunishmentHistoryDetailScreen {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern(
                            "MMM d, yyyy h:mm:ss a"
                    )
                    .withZone(
                            ZoneId.systemDefault()
                    );

    private PunishmentHistoryDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID punishmentId,
            int historyPage
    ) {
        open(
                viewer,
                punishmentId,
                new UUID(0L, 0L),
                0,
                historyPage,
                HistoryReturnTarget.PUNISHMENT_MODULE_HISTORY
        );
    }

    public static void open(
            ServerPlayer viewer,
            UUID punishmentId,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        if (!hasRequiredPermission(
                viewer,
                returnTarget
        )) {
            return;
        }

        PunishmentRecord record =
                PunishmentService.findById(
                        punishmentId
                );

        if (record == null) {
            viewer.sendSystemMessage(
                    Component.literal(
                            "That punishment record is no longer available."
                    )
            );

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
                        PunishmentHistoryDetailMenu.MENU_SIZE
                );

        populate(
                container,
                record,
                returnTarget
        );

        Component title =
                Component.literal(
                        "Punishment Record • "
                                + record.targetName()
                );

        viewer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, ignored) ->
                                new PunishmentHistoryDetailMenu(
                                        containerId,
                                        inventory,
                                        container,
                                        punishmentId,
                                        targetUuid,
                                        browserPage,
                                        historyPage,
                                        returnTarget
                                ),
                        title
                )
        );
    }

    private static boolean hasRequiredPermission(
            ServerPlayer viewer,
            HistoryReturnTarget returnTarget
    ) {
        HistoryReturnTarget safeReturnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget
                        .PUNISHMENT_MODULE_HISTORY;

        if (safeReturnTarget
                == HistoryReturnTarget
                .PUNISHMENT_MODULE_HISTORY) {

            return StewardPermissions.require(
                    viewer,
                    StewardPermissions.PUNISHMENT_VIEW
            );
        }

        return StewardPermissions.require(
                viewer,
                StewardPermissions.HISTORY_VIEW
        );
    }

    private static void populate(
            SimpleContainer container,
            PunishmentRecord record,
            HistoryReturnTarget returnTarget
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
                statusItem(record.status()),
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
                "Expiration: "
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
                "Record State: "
                        + formatRecordState(record)
        );

        if (record.status()
                == PunishmentStatus.REVOKED) {

            setButton(
                    container,
                    28,
                    Items.MAGMA_CREAM,
                    "Revoked At: "
                            + formatDate(
                            record.revokedAt()
                    )
            );

            setButton(
                    container,
                    29,
                    Items.PLAYER_HEAD,
                    "Revoked By: "
                            + displayOptional(
                            record.revokedByName()
                    )
            );

            setButton(
                    container,
                    30,
                    Items.PAPER,
                    "Revocation Reason: "
                            + displayOptional(
                            record.revocationReason()
                    )
            );
        }

        setButton(
                container,
                PunishmentHistoryDetailMenu.BACK_SLOT,
                Items.OAK_DOOR,
                backButtonText(returnTarget)
        );

        setButton(
                container,
                PunishmentHistoryDetailMenu.PROFILE_SLOT,
                Items.PLAYER_HEAD,
                "Player Profile"
        );

        setButton(
                container,
                PunishmentHistoryDetailMenu.CLOSE_SLOT,
                Items.BARRIER,
                "Close"
        );
    }

    private static String backButtonText(
            HistoryReturnTarget returnTarget
    ) {
        HistoryReturnTarget safeReturnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget.PUNISHMENT_MODULE_HISTORY;

        return switch (safeReturnTarget) {
            case ALL_ACTIVITY ->
                    "Back to All Activity";

            case PUNISHMENT_HISTORY ->
                    "Back to Punishment History";

            case GLOBAL_ALL_ACTIVITY ->
                    "Back to Global Activity";

            case GLOBAL_WARNING_HISTORY ->
                    "Back to Global Warning History";

            case GLOBAL_FREEZE_HISTORY ->
                    "Back to Global Freeze History";

            case GLOBAL_PUNISHMENT_HISTORY ->
                    "Back to Global Punishment History";

            case WARNING_HISTORY ->
                    "Back to Warning History";

            case FREEZE_HISTORY ->
                    "Back to Freeze History";

            case PUNISHMENT_MODULE_HISTORY ->
                    "Back to Punishment History";

            case STAFF_MENU ->
                    "Back to Staff Menu";
        };
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

    private static Item statusItem(
            PunishmentStatus status
    ) {
        return switch (status) {
            case ACTIVE ->
                    Items.REDSTONE_TORCH;

            case EXPIRED ->
                    BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(
                                    "minecraft",
                                    "gray_wool"
                            )
                    );

            case REVOKED ->
                    Items.MAGMA_CREAM;

            case COMPLETED ->
                    BuiltInRegistries.ITEM.getValue(
                            Identifier.fromNamespaceAndPath(
                                    "minecraft",
                                    "lime_wool"
                            )
                    );
        };
    }

    private static String formatDuration(
            PunishmentRecord record
    ) {
        if (record.type() == PunishmentType.KICK) {
            return "Immediate";
        }

        if (record.expiresAt() == null) {
            return "Permanent";
        }

        Duration duration =
                Duration.between(
                        record.issuedAt(),
                        record.expiresAt()
                );

        long totalMinutes =
                Math.max(
                        0,
                        duration.toMinutes()
                );

        long days =
                totalMinutes / 1_440;

        long hours =
                (totalMinutes % 1_440) / 60;

        long minutes =
                totalMinutes % 60;

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

    private static String formatExpiration(
            PunishmentRecord record
    ) {
        if (record.type() == PunishmentType.KICK) {
            return "Not Applicable";
        }

        if (record.expiresAt() == null) {
            return "Never";
        }

        return DATE_FORMAT.format(
                record.expiresAt()
        );
    }

    private static String formatRecordState(
            PunishmentRecord record
    ) {
        return switch (record.status()) {
            case ACTIVE -> {
                if (record.expiresAt() == null) {
                    yield "Currently enforced permanently";
                }

                long seconds =
                        Math.max(
                                0,
                                Duration.between(
                                        Instant.now(),
                                        record.expiresAt()
                                ).getSeconds()
                        );

                yield formatRemaining(seconds)
                        + " remaining";
            }

            case EXPIRED ->
                    "Expired automatically";

            case REVOKED ->
                    "Ended early by staff";

            case COMPLETED ->
                    "Immediate action completed";
        };
    }

    private static String formatRemaining(
            long totalSeconds
    ) {
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

    private static String formatDate(
            Instant instant
    ) {
        if (instant == null) {
            return "Unknown";
        }

        return DATE_FORMAT.format(instant);
    }

    private static String displayOptional(
            String value
    ) {
        if (value == null || value.isBlank()) {
            return "None";
        }

        return value;
    }

    private static void returnToSource(
            ServerPlayer viewer,
            UUID targetUuid,
            int browserPage,
            int historyPage,
            HistoryReturnTarget returnTarget
    ) {
        HistoryReturnTarget safeReturnTarget =
                returnTarget != null
                        ? returnTarget
                        : HistoryReturnTarget
                        .PUNISHMENT_MODULE_HISTORY;

        switch (safeReturnTarget) {
            case PUNISHMENT_MODULE_HISTORY ->
                    PunishmentHistoryScreen.open(
                            viewer,
                            historyPage
                    );

            case ALL_ACTIVITY, WARNING_HISTORY, FREEZE_HISTORY ->
                    com.swornhero.steward.core.history
                            .ModerationHistoryScreen.open(
                                    viewer,
                                    targetUuid,
                                    browserPage,
                                    com.swornhero.steward.core.history
                                            .PlayerHistoryView.ALL_ACTIVITY,
                                    historyPage
                            );

            case PUNISHMENT_HISTORY ->
                    com.swornhero.steward.core.history
                            .ModerationHistoryScreen.open(
                                    viewer,
                                    targetUuid,
                                    browserPage,
                                    com.swornhero.steward.core.history
                                            .PlayerHistoryView
                                            .PUNISHMENT_HISTORY,
                                    historyPage
                            );

            case GLOBAL_ALL_ACTIVITY ->
                    com.swornhero.steward.core.history
                            .GlobalModerationHistoryScreen.open(
                                    viewer,
                                    com.swornhero.steward.core.history
                                            .GlobalHistoryView.ALL_ACTIVITY,
                                    historyPage
                            );

            case GLOBAL_PUNISHMENT_HISTORY ->
                    com.swornhero.steward.core.history
                            .GlobalModerationHistoryScreen.open(
                                    viewer,
                                    com.swornhero.steward.core.history
                                            .GlobalHistoryView
                                            .PUNISHMENT_HISTORY,
                                    historyPage
                            );

            case GLOBAL_WARNING_HISTORY ->
                    com.swornhero.steward.core.history
                            .GlobalModerationHistoryScreen.open(
                                    viewer,
                                    com.swornhero.steward.core.history
                                            .GlobalHistoryView
                                            .WARNING_HISTORY,
                                    historyPage
                            );

            case GLOBAL_FREEZE_HISTORY ->
                    com.swornhero.steward.core.history
                            .GlobalModerationHistoryScreen.open(
                                    viewer,
                                    com.swornhero.steward.core.history
                                            .GlobalHistoryView
                                            .FREEZE_HISTORY,
                                    historyPage
                            );

            case STAFF_MENU ->
                    StaffControlScreen.open(viewer);

        }
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
             slot
                     < PunishmentHistoryDetailMenu.MENU_SIZE;
             slot++) {

            int row = slot / 9;
            int column = slot % 9;

            boolean border =
                    row == 0
                            || row
                            == PunishmentHistoryDetailMenu.ROWS - 1
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

        LinkedHashSet<net.minecraft.core.component.DataComponentType<?>>
                hiddenComponents =
                new LinkedHashSet<>();

        hiddenComponents.add(
                DataComponents.ATTRIBUTE_MODIFIERS
        );

        stack.set(
                DataComponents.TOOLTIP_DISPLAY,
                new TooltipDisplay(
                        false,
                        hiddenComponents
                )
        );

        container.setItem(
                slot,
                stack
        );
    }
}