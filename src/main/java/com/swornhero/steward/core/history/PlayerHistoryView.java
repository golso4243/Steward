package com.swornhero.steward.core.history;

public enum PlayerHistoryView {

    ALL_ACTIVITY(
            "Moderation History",
            false,
            HistoryReturnTarget.ALL_ACTIVITY
    ),

    PUNISHMENT_HISTORY(
            "Punishment History",
            true,
            HistoryReturnTarget.PUNISHMENT_HISTORY
    );

    private final String title;
    private final boolean punishmentHistory;
    private final HistoryReturnTarget returnTarget;

    PlayerHistoryView(
            String title,
            boolean punishmentHistory,
            HistoryReturnTarget returnTarget
    ) {
        this.title = title;
        this.punishmentHistory = punishmentHistory;
        this.returnTarget = returnTarget;
    }

    public String title() {
        return title;
    }

    public boolean isPunishmentHistory() {
        return punishmentHistory;
    }

    public HistoryReturnTarget returnTarget() {
        return returnTarget;
    }
}