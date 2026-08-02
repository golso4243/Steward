package com.swornhero.steward.core.history;

import com.swornhero.steward.module.freeze.model.FreezeHistoryEntry;
import com.swornhero.steward.module.freeze.service.FreezeHistoryService;
import com.swornhero.steward.module.warning.model.WarningRecord;
import com.swornhero.steward.module.warning.service.WarningService;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ModerationHistoryService {

    private ModerationHistoryService() {
        // Utility class
    }

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
                targetUuid
        );

        addFreezeHistory(
                history,
                targetUuid
        );

        history.sort(
                Comparator.comparing(
                        ModerationHistoryItem::occurredAt
                ).reversed()
        );

        return List.copyOf(history);
    }

    private static void addWarningHistory(
            List<ModerationHistoryItem> history,
            UUID targetUuid
    ) {
        for (WarningRecord warning
                : WarningService.warningsFor(
                targetUuid
        )) {

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
            UUID targetUuid
    ) {
        for (FreezeHistoryEntry freeze
                : FreezeHistoryService.getForPlayer(
                targetUuid
        )) {

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
}