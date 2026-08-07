package com.swornhero.steward.core.chat;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public final class ClickableRecordId {

    private ClickableRecordId() {
        // Utility class
    }

    public static MutableComponent create(
            String displayId,
            String command,
            String hoverText
    ) {
        if (displayId == null
                || displayId.isBlank()) {

            throw new IllegalArgumentException(
                    "Display ID cannot be blank."
            );
        }

        if (command == null
                || command.isBlank()) {

            throw new IllegalArgumentException(
                    "Command cannot be blank."
            );
        }

        if (hoverText == null
                || hoverText.isBlank()) {

            throw new IllegalArgumentException(
                    "Hover text cannot be blank."
            );
        }

        return Component.literal(
                displayId
        ).withStyle(style ->
                style
                        .withColor(
                                ChatFormatting.AQUA
                        )
                        .withUnderlined(true)
                        .withClickEvent(
                                new ClickEvent.RunCommand(
                                        command
                                )
                        )
                        .withHoverEvent(
                                new HoverEvent.ShowText(
                                        Component.literal(
                                                hoverText
                                        )
                                )
                        )
        );
    }
}