package com.swornhero.steward.core.history;

public enum PlayerHistoryView {

    ALL_ACTIVITY(
            "Moderation History",
            null,
            false,
            HistoryReturnTarget.ALL_ACTIVITY
    ),

    PUNISHMENT_HISTORY(
            "Punishment History",
            null,
            true,
            HistoryReturnTarget.PUNISHMENT_HISTORY
    ),

    REPORT_HISTORY(
            "Report History",
            ModerationActionType.REPORT,
            false,
            HistoryReturnTarget.REPORT_HISTORY
    ),

    NOTE_HISTORY(
            "Staff Note History",
            ModerationActionType.NOTE,
            false,
            HistoryReturnTarget.NOTE_HISTORY
    );

    private final String title;
    private final ModerationActionType actionType;
    private final boolean punishmentHistory;
    private final HistoryReturnTarget returnTarget;

    PlayerHistoryView(
            String title,
            ModerationActionType actionType,
            boolean punishmentHistory,
            HistoryReturnTarget returnTarget
    ) {
        this.title = title;
        this.actionType = actionType;
        this.punishmentHistory = punishmentHistory;
        this.returnTarget = returnTarget;
    }

    public String title() {
        return title;
    }

    public ModerationActionType actionType() {
        return actionType;
    }

    public boolean isPunishmentHistory() {
        return punishmentHistory;
    }

    public HistoryReturnTarget returnTarget() {
        return returnTarget;
    }
}
