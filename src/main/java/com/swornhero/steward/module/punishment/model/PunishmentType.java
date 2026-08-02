package com.swornhero.steward.module.punishment.model;

public enum PunishmentType {

    MUTE(
            "Mute",
            true,
            true
    ),

    KICK(
            "Kick",
            false,
            false
    ),

    TEMPORARY_BAN(
            "Temporary Ban",
            true,
            true
    ),

    PERMANENT_BAN(
            "Permanent Ban",
            true,
            false
    );

    private final String displayName;
    private final boolean activePunishment;
    private final boolean supportsExpiration;

    PunishmentType(
            String displayName,
            boolean activePunishment,
            boolean supportsExpiration
    ) {
        this.displayName = displayName;
        this.activePunishment = activePunishment;
        this.supportsExpiration = supportsExpiration;
    }

    public String displayName() {
        return displayName;
    }

    public boolean isActivePunishment() {
        return activePunishment;
    }

    public boolean supportsExpiration() {
        return supportsExpiration;
    }

    public boolean isBan() {
        return this == TEMPORARY_BAN
                || this == PERMANENT_BAN;
    }
}