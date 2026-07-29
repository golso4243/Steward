package com.swornhero.steward.freeze;

import java.time.Instant;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

public final class FreezeRecord {
    private final UUID targetUuid;
    private final String targetName;

    private final UUID frozenByUuid;
    private final String frozenByName;
    private final UUID freezeId;

    private final FreezePosition position;
    private FreezePosition currentPosition;

    private final List<FreezeRelocation> relocations =
            new ArrayList<>();

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
        this(
                UUID.randomUUID(),
                targetUuid,
                targetName,
                frozenByUuid,
                frozenByName,
                position,
                reason,
                frozenAt
        );
    }

    public FreezeRecord(
            UUID freezeId,
            UUID targetUuid,
            String targetName,
            UUID frozenByUuid,
            String frozenByName,
            FreezePosition position,
            String reason,
            Instant frozenAt
    ) {
        this.freezeId =
                freezeId != null
                        ? freezeId
                        : UUID.randomUUID();

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

    public UUID freezeId() {
        return freezeId;
    }

    public FreezePosition position() {
        return position;
    }

    public FreezePosition currentPosition() {
        if (currentPosition == null) {
            return position;
        }

        return currentPosition;
    }

    public List<FreezeRelocation> relocations() {
        if (relocations == null) {
            return List.of();
        }

        return List.copyOf(relocations);
    }

    public void relocate(
            FreezePosition newPosition,
            UUID staffUuid,
            String staffName,
            String note
    ) {
        FreezePosition previousPosition =
                currentPosition();

        relocations.add(
                new FreezeRelocation(
                        previousPosition,
                        newPosition,
                        staffUuid,
                        staffName,
                        Instant.now(),
                        note
                )
        );

        currentPosition = newPosition;
    }

    public void restoreCurrentPosition(
            FreezePosition restoredPosition
    ) {
        currentPosition = restoredPosition;
    }

    public void restoreRelocations(
            List<FreezeRelocation> restoredRelocations
    ) {
        relocations.clear();

        if (restoredRelocations == null) {
            return;
        }

        relocations.addAll(
                restoredRelocations
        );
    }

    public void removeLatestRelocation() {
        if (!relocations.isEmpty()) {
            relocations.remove(
                    relocations.size() - 1
            );
        }
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

    public void restoreConnectionCounts(
            int disconnectCount,
            int reconnectCount
    ) {
        this.disconnectCount =
                Math.max(0, disconnectCount);

        this.reconnectCount =
                Math.max(0, reconnectCount);
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