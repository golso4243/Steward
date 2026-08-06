package com.swornhero.steward.core.history;

public enum GlobalHistoryView {

    ALL_ACTIVITY(
            "Global History",
            null,
            false,
            HistoryReturnTarget.GLOBAL_ALL_ACTIVITY
    ),

    WARNING_HISTORY(
            "Global Warning History",
            ModerationActionType.WARNING,
            false,
            HistoryReturnTarget.GLOBAL_WARNING_HISTORY
    ),

    FREEZE_HISTORY(
            "Global Freeze History",
            ModerationActionType.FREEZE,
            false,
            HistoryReturnTarget.GLOBAL_FREEZE_HISTORY
    ),

    PUNISHMENT_HISTORY(
            "Global Punishment History",
            null,
            true,
            HistoryReturnTarget.GLOBAL_PUNISHMENT_HISTORY
    );

    private final String title;
    private final ModerationActionType actionType;
    private final boolean punishmentHistory;
    private final HistoryReturnTarget returnTarget;

    GlobalHistoryView(
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

    public boolean showsAllTypes() {
        return actionType == null
                && !punishmentHistory;
    }
}