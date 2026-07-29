package com.swornhero.steward.config;

public final class FreezePolicy {

    private boolean disconnectAlerts = true;
    private boolean reconnectAlerts = true;
    private boolean showReasonOnReconnect = true;
    private boolean offlineUnfreezeNotices = true;
    private boolean fallbackStaffAlerts = true;
    private boolean fallbackConsoleWarnings = true;

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
}