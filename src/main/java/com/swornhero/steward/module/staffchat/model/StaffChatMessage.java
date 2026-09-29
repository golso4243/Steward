package com.swornhero.steward.module.staffchat.model;

import java.time.Instant;
import java.util.UUID;

public record StaffChatMessage(
        UUID senderUuid,
        String senderName,
        String text,
        Instant sentAt
) {
}
