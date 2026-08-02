package com.swornhero.steward.core.history;

public enum GlobalHistoryView {

    ALL_ACTIVITY(
            "Global History",
            null,
            HistoryReturnTarget.GLOBAL_ALL_ACTIVITY
    ),

    WARNING_HISTORY(
            "Global Warning History",
            ModerationActionType.WARNING,
            HistoryReturnTarget.GLOBAL_WARNING_HISTORY
    ),

    FREEZE_HISTORY(
            "Global Freeze History",
            ModerationActionType.FREEZE,
            HistoryReturnTarget.GLOBAL_FREEZE_HISTORY
    );

    private final String title;
    private final ModerationActionType actionType;
    private final HistoryReturnTarget returnTarget;

    GlobalHistoryView(
            String title,
            ModerationActionType actionType,
            HistoryReturnTarget returnTarget
    ) {
        this.title = title;
        this.actionType = actionType;
        this.returnTarget = returnTarget;
    }

    public String title() {
        return title;
    }

    public ModerationActionType actionType() {
        return actionType;
    }

    public HistoryReturnTarget returnTarget() {
        return returnTarget;
    }

    public boolean showsAllTypes() {
        return actionType == null;
    }
}