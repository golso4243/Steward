package com.swornhero.steward.module.staffmode.gui;

public enum TeleportAction {
    TELEPORT_TO(11),
    BRING_HERE(15),
    BACK(18),
    CLOSE(26);

    private final int slot;

    TeleportAction(int slot) {
        this.slot = slot;
    }

    public int slot() {
        return slot;
    }

    public static TeleportAction fromSlot(int slot) {
        for (TeleportAction action : values()) {
            if (action.slot == slot) {
                return action;
            }
        }

        return null;
    }
}