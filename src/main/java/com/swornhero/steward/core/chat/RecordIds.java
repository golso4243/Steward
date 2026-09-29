package com.swornhero.steward.core.chat;

import java.util.UUID;

/**
 * Short, prefixed display IDs such as {@code RPT-1A2B3C4D}.
 */
public final class RecordIds {

    private RecordIds() {
        // Utility class
    }

    public static String format(String prefix, UUID id) {
        if (id == null) {
            return prefix + "-UNKNOWN";
        }

        return prefix + "-" + compact(id);
    }

    /**
     * Returns the eight-character compact form of a display ID, or
     * {@code null} if the input cannot be one of this prefix's IDs.
     */
    public static String normalize(String prefix, String displayId) {
        if (displayId == null || displayId.isBlank()) {
            return null;
        }

        String normalized = displayId.trim().toUpperCase();

        if (normalized.startsWith(prefix + "-")) {
            normalized = normalized.substring(prefix.length() + 1);
        }

        return normalized.length() == 8 ? normalized : null;
    }

    public static String compact(UUID id) {
        return id.toString()
                .replace("-", "")
                .substring(0, 8)
                .toUpperCase();
    }
}
