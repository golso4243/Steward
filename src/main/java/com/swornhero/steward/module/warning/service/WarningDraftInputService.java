package com.swornhero.steward.module.warning.service;

import com.swornhero.steward.module.warning.gui.WarningMetadataScreen;
import com.swornhero.steward.module.warning.model.WarningDraft;
import com.swornhero.steward.module.warning.model.WarningDraftInputType;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class WarningDraftInputService {

    private static final Map<UUID, PendingInput> PENDING_INPUTS =
            new HashMap<>();

    private WarningDraftInputService() {
        // Utility class
    }

    public static void register() {
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) ->
                        clear(handler.player.getUUID())
        );
    }

    public static void begin(
            ServerPlayer staff,
            WarningDraft draft,
            WarningDraftInputType inputType
    ) {
        if (staff == null || draft == null || inputType == null) {
            return;
        }

        PENDING_INPUTS.put(
                staff.getUUID(),
                new PendingInput(draft, inputType)
        );

        staff.closeContainer();
        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] Enter the "
                                + inputType.displayName()
                                + " with /steward warning input <text>. "
                                + "Use /steward warning cancel-input to return "
                                + "without changing it."
                )
        );
    }

    public static boolean submit(
            ServerPlayer staff,
            String value
    ) {
        if (staff == null) {
            return false;
        }

        PendingInput pending =
                PENDING_INPUTS.get(staff.getUUID());

        if (pending == null) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You do not have a warning input pending."
                    )
            );

            return false;
        }

        String normalized =
                value != null
                        ? value.trim()
                        : "";

        if (normalized.isBlank()) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] Warning input cannot be blank."
                    )
            );

            return false;
        }

        if (normalized.length()
                > pending.inputType().maximumLength()) {

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] That "
                                    + pending.inputType().displayName()
                                    + " is too long. Maximum length: "
                                    + pending.inputType().maximumLength()
                                    + " characters."
                    )
            );

            return false;
        }

        WarningDraft updatedDraft =
                switch (pending.inputType()) {
                    case STAFF_NOTES ->
                            pending.draft()
                                    .withStaffNotes(normalized);

                    case EVIDENCE_REFERENCE ->
                            pending.draft()
                                    .withEvidenceReference(normalized);
                };

        PENDING_INPUTS.remove(staff.getUUID());
        WarningMetadataScreen.open(staff, updatedDraft);
        return true;
    }

    public static boolean cancel(ServerPlayer staff) {
        if (staff == null) {
            return false;
        }

        PendingInput pending =
                PENDING_INPUTS.remove(staff.getUUID());

        if (pending == null) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You do not have a warning input pending."
                    )
            );

            return false;
        }

        WarningMetadataScreen.open(staff, pending.draft());
        return true;
    }

    public static void clear(UUID staffUuid) {
        if (staffUuid != null) {
            PENDING_INPUTS.remove(staffUuid);
        }
    }

    private record PendingInput(
            WarningDraft draft,
            WarningDraftInputType inputType
    ) {
    }
}
