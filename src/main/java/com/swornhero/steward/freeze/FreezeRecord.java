package com.swornhero.steward.freeze;

import java.time.Instant;
import java.util.UUID;

public final class FreezeRecord {
    private final UUID targetUuid;
    private final String targetName;

    private final UUID frozenByUuid;
    private final String frozenByName;

    private final FreezePosition position;
    private final String reason;
    private final Instant frozenAt;

    private Instant unfrozenAt;
    private UUID unfrozenByUuid;
    private String unfrozenByName;

    private int disconnectCount;
    private int reconnectCount;

    public FreezeRecord(
            UUID targetUuid,
            String targetName,
            UUID frozenByUuid,
            String frozenByName,
            FreezePosition position,
            String reason,
            Instant frozenAt
    ) {
        this.targetUuid = targetUuid;
        this.targetName = targetName;
        this.frozenByUuid = frozenByUuid;
        this.frozenByName = frozenByName;
        this.position = position;
        this.reason = reason;
        this.frozenAt = frozenAt;
    }

    public UUID targetUuid() {
        return targetUuid;
    }

    public String targetName() {
        return targetName;
    }

    public UUID frozenByUuid() {
        return frozenByUuid;
    }

    public String frozenByName() {
        return frozenByName;
    }

    public FreezePosition position() {
        return position;
    }

    public String reason() {
        return reason;
    }

    public Instant frozenAt() {
        return frozenAt;
    }

    public Instant unfrozenAt() {
        return unfrozenAt;
    }

    public UUID unfrozenByUuid() {
        return unfrozenByUuid;
    }

    public String unfrozenByName() {
        return unfrozenByName;
    }

    public int disconnectCount() {
        return disconnectCount;
    }

    public int reconnectCount() {
        return reconnectCount;
    }

    public boolean isActive() {
        return unfrozenAt == null;
    }

    public void recordDisconnect() {
        disconnectCount++;
    }

    public void recordReconnect() {
        reconnectCount++;
    }

    public void complete(
            UUID staffUuid,
            String staffName,
            Instant completedAt
    ) {
        this.unfrozenByUuid = staffUuid;
        this.unfrozenByName = staffName;
        this.unfrozenAt = completedAt;
    }
}