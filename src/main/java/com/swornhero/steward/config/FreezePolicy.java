package com.swornhero.steward.config;

import java.util.ArrayList;
import java.util.List;

public final class FreezePolicy {

    private boolean disconnectAlerts = true;
    private boolean reconnectAlerts = true;
    private boolean showReasonOnReconnect = true;
    private boolean offlineUnfreezeNotices = true;
    private boolean fallbackStaffAlerts = true;
    private boolean fallbackConsoleWarnings = true;

    private List<String> allowedCommands =
            new ArrayList<>(
                    List.of(
                            "msg",
                            "tell",
                            "w",
                            "reply",
                            "r",
                            "staff",
                            "steward"
                    )
            );

    private String blockedCommandMessage =
            "You cannot use that command while frozen.";

    public boolean disconnectAlerts() {
        return disconnectAlerts;
    }

    public boolean reconnectAlerts() {
        return reconnectAlerts;
    }

    public boolean showReasonOnReconnect() {
        return showReasonOnReconnect;
    }

    public boolean offlineUnfreezeNotices() {
        return offlineUnfreezeNotices;
    }

    public boolean fallbackStaffAlerts() {
        return fallbackStaffAlerts;
    }

    public boolean fallbackConsoleWarnings() {
        return fallbackConsoleWarnings;
    }

    public List<String> allowedCommands() {
        if (allowedCommands == null) {
            return List.of();
        }

        return List.copyOf(
                allowedCommands
        );
    }

    public String blockedCommandMessage() {
        return blockedCommandMessage;
    }
}