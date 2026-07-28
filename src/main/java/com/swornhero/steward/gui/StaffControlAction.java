package com.swornhero.steward.gui;

public enum StaffControlAction {
    STAFF_MODE(10),
    MODE_STYLE(11),
    VANISH(12),
    PLAYERS(13),
    TELEPORT(14),
    ACTIVE_FREEZES(15),
    REPORTS(16),

    NOTES(19),
    INSPECTION(20),
    HISTORY(21),
    PUNISHMENTS(22),
    STAFF_CHAT(23),
    ALERTS(24),
    SETTINGS(25),

    CLOSE(49);

    private final int slot;

    StaffControlAction(int slot) {
        this.slot = slot;
    }

    public int slot() {
        return slot;
    }

    public static StaffControlAction fromSlot(int slot) {
        for (StaffControlAction action : values()) {
            if (action.slot == slot) {
                return action;
            }
        }

        return null;
    }
}