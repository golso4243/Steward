package com.swornhero.steward.gui;

public enum PlayerProfileAction {
    PLAYER_INFO(13),

    TELEPORT_TO(19),
    BRING_HERE(20),
    FREEZE(21),
    INSPECT(22),
    REPORTS(23),
    NOTES(24),
    HISTORY(25),

    PUNISHMENTS(31),

    BACK(48),
    CLOSE(50);

    private final int slot;

    PlayerProfileAction(int slot) {
        this.slot = slot;
    }

    public int slot() {
        return slot;
    }

    public static PlayerProfileAction fromSlot(int slot) {
        for (PlayerProfileAction action : values()) {
            if (action.slot == slot) {
                return action;
            }
        }

        return null;
    }
}