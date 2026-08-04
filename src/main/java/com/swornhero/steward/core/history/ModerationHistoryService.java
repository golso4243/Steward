package com.swornhero.steward.core.history;

import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;
import com.swornhero.steward.module.punishment.model.PunishmentRecord;
import com.swornhero.steward.module.punishment.model.PunishmentType;
import com.swornhero.steward.module.punishment.service.PunishmentService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ModerationHistoryService {

    private static final Comparator<ModerationHistoryItem>
            NEWEST_FIRST =
            Comparator.comparing(
                    ModerationHistoryItem::occurredAt
            ).reversed();

    private ModerationHistoryService() {
        // Utility class
    }

    /**
     * Returns every supported moderation-history record
     * across the entire server.
     */
    public static List<ModerationHistoryItem> getAll() {
        List<ModerationHistoryItem> history =
                new ArrayList<>();

        addWarningHistory(
                history,
                WarningService.allWarnings()
        );

        addFreezeHistory(
                history,
                FreezeHistoryService.getAll()
        );

        addPunishmentHistory(
                history,
                PunishmentService.allPunishments()
        );

        return sortedCopy(history);
    }

    /**
     * Returns all supported moderation-history records
     * belonging to one player.
     */
    public static List<ModerationHistoryItem> getForPlayer(
            UUID targetUuid
    ) {
        if (targetUuid == null) {
            return List.of();
        }

        List<ModerationHistoryItem> history =
                new ArrayList<>();

        addWarningHistory(
                history,
                WarningService.warningsFor(
                        targetUuid
                )
        );

        addFreezeHistory(
                history,
                FreezeHistoryService.getForPlayer(
                        targetUuid
                )
        );

        addPunishmentHistory(
                history,
                PunishmentService.punishmentsFor(
                        targetUuid
                )
        );

        return sortedCopy(history);
    }

    /**
     * Returns every global history record of one action type.
     */
    public static List<ModerationHistoryItem> getAllByType(
            ModerationActionType type
    ) {
        if (type == null) {
            return List.of();
        }

        return getAll()
                .stream()
                .filter(item -> item.type() == type)
                .toList();
    }

    /**
     * Returns one player's history records of one action type.
     */
    public static List<ModerationHistoryItem> getForPlayerByType(
            UUID targetUuid,
            ModerationActionType type
    ) {
        if (targetUuid == null || type == null) {
            return List.of();
        }

        return getForPlayer(targetUuid)
                .stream()
                .filter(item -> item.type() == type)
                .toList();
    }

    public static int countAll() {
        return getAll().size();
    }

    public static int countAllByType(
            ModerationActionType type
    ) {
        return getAllByType(type).size();
    }

    public static int countForPlayer(
            UUID targetUuid
    ) {
        return getForPlayer(targetUuid).size();
    }

    public static int countForPlayerByType(
            UUID targetUuid,
            ModerationActionType type
    ) {
        return getForPlayerByType(
                targetUuid,
                type
        ).size();
    }

    private static void addWarningHistory(
            List<ModerationHistoryItem> history,
            List<WarningRecord> warnings
    ) {
        for (WarningRecord warning : warnings) {
            if (warning == null) {
                continue;
            }

            String summary =
                    warning.level().displayName()
                            + " • "
                            + warning.category().displayName()
                            + " • "
                            + warning.status().displayName();

            history.add(
                    new ModerationHistoryItem(
                            ModerationActionType.WARNING,
                            warning.warningId(),
                            warning.targetUuid(),
                            warning.targetName(),
                            summary,
                            warning.issuedAt()
                    )
            );
        }
    }

    private static void addFreezeHistory(
            List<ModerationHistoryItem> history,
            List<FreezeHistoryEntry> freezes
    ) {
        for (FreezeHistoryEntry freeze : freezes) {
            if (freeze == null) {
                continue;
            }

            String summary =
                    "Freeze • "
                            + freeze.reason();

            history.add(
                    new ModerationHistoryItem(
                            ModerationActionType.FREEZE,
                            freeze.freezeId(),
                            freeze.targetUuid(),
                            freeze.targetName(),
                            summary,
                            freeze.frozenAt()
                    )
            );
        }
    }

    private static void addPunishmentHistory(
            List<ModerationHistoryItem> history,
            List<PunishmentRecord> punishments
    ) {
        for (PunishmentRecord punishment : punishments) {
            if (punishment == null) {
                continue;
            }

            String summary =
                    punishment.type().displayName()
                            + " • "
                            + punishment.reason()
                            + " • "
                            + punishment.status().displayName();

            history.add(
                    new ModerationHistoryItem(
                            moderationTypeFor(
                                    punishment.type()
                            ),
                            punishment.punishmentId(),
                            punishment.targetUuid(),
                            punishment.targetName(),
                            summary,
                            punishment.issuedAt()
                    )
            );
        }
    }

    private static ModerationActionType moderationTypeFor(
            PunishmentType punishmentType
    ) {
        return switch (punishmentType) {
            case MUTE ->
                    ModerationActionType.MUTE;

            case KICK ->
                    ModerationActionType.KICK;

            case TEMPORARY_BAN ->
                    ModerationActionType.TEMPORARY_BAN;

            case PERMANENT_BAN ->
                    ModerationActionType.PERMANENT_BAN;
        };
    }

    private static List<ModerationHistoryItem> sortedCopy(
            List<ModerationHistoryItem> history
    ) {
        history.sort(NEWEST_FIRST);

        return List.copyOf(history);
    }
}