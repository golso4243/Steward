package com.swornhero.steward.core.input;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Collects one line of free text from a staff member through a
 * command, while the menu that requested it is closed.
 */
public final class TextPromptService {

    private static final Map<UUID, Prompt> PROMPTS = new HashMap<>();

    private TextPromptService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> PROMPTS.remove(handler.player.getUUID())
        );
    }

    /**
     * @param channel   the command family, for example {@code report},
     *                  used in {@code /steward <channel> input <text>}
     * @param onSubmit  receives the staff member and the validated text
     * @param onCancel  reopens the originating menu without changes
     */
    public static void begin(
            ServerPlayer staff,
            String channel,
            String label,
            int maximumLength,
            BiConsumer<ServerPlayer, String> onSubmit,
            Consumer<ServerPlayer> onCancel
    ) {
        PROMPTS.put(
                staff.getUUID(),
                new Prompt(channel, label, maximumLength, onSubmit, onCancel)
        );

        staff.closeContainer();
        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] Enter the " + label
                                + " with /steward " + channel + " input <text>. "
                                + "Use /steward " + channel
                                + " cancel-input to go back without changes."
                )
        );
    }

    public static boolean submit(
            ServerPlayer staff,
            String channel,
            String value
    ) {
        Prompt prompt = PROMPTS.get(staff.getUUID());

        if (prompt == null || !prompt.channel().equals(channel)) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You do not have a " + channel
                                    + " input pending."
                    )
            );

            return false;
        }

        String normalized = value != null ? value.trim() : "";

        if (normalized.isBlank()) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] The " + prompt.label()
                                    + " cannot be blank."
                    )
            );

            return false;
        }

        if (normalized.length() > prompt.maximumLength()) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] That " + prompt.label()
                                    + " is too long. Maximum length: "
                                    + prompt.maximumLength() + " characters."
                    )
            );

            return false;
        }

        PROMPTS.remove(staff.getUUID());
        prompt.onSubmit().accept(staff, normalized);
        return true;
    }

    public static boolean cancel(
            ServerPlayer staff,
            String channel
    ) {
        Prompt prompt = PROMPTS.get(staff.getUUID());

        if (prompt == null || !prompt.channel().equals(channel)) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You do not have a " + channel
                                    + " input pending."
                    )
            );

            return false;
        }

        PROMPTS.remove(staff.getUUID());
        prompt.onCancel().accept(staff);
        return true;
    }

    private record Prompt(
            String channel,
            String label,
            int maximumLength,
            BiConsumer<ServerPlayer, String> onSubmit,
            Consumer<ServerPlayer> onCancel
    ) {
    }
}
