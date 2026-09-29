package com.swornhero.steward.module.notes.gui;

import com.swornhero.steward.core.gui.ActionMenu;
import com.swornhero.steward.core.gui.MenuItems;
import com.swornhero.steward.core.gui.StewardMenu;
import com.swornhero.steward.core.input.TextPromptService;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.notes.model.StaffNoteRecord;
import com.swornhero.steward.module.notes.service.StaffNoteService;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class PlayerNotesScreen {

    public static final String INPUT_CHANNEL = "notes";

    private static final int FILTER_SLOT = 3;
    private static final int ADD_NOTE_SLOT = 5;

    private PlayerNotesScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            String targetName,
            int page,
            Consumer<ServerPlayer> back,
            String backLabel
    ) {
        open(viewer, targetUuid, targetName, false, page, back, backLabel);
    }

    private static void open(
            ServerPlayer viewer,
            UUID targetUuid,
            String targetName,
            boolean includeArchived,
            int requestedPage,
            Consumer<ServerPlayer> back,
            String backLabel
    ) {
        if (!StewardPermissions.require(viewer, StewardPermissions.NOTES_VIEW)) {
            return;
        }

        List<StaffNoteRecord> notes =
                StaffNoteService.notesFor(targetUuid, includeArchived);

        int totalPages = MenuItems.totalPages(notes.size());
        int page = MenuItems.clampPage(requestedPage, totalPages);

        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();

        Consumer<ServerPlayer> reopen = player -> open(
                player,
                targetUuid,
                targetName,
                includeArchived,
                page,
                back,
                backLabel
        );

        int start = page * MenuItems.CONTENT_SLOTS.length;
        int end = Math.min(start + MenuItems.CONTENT_SLOTS.length, notes.size());

        for (int index = start; index < end; index++) {
            StaffNoteRecord note = notes.get(index);
            int slot = MenuItems.CONTENT_SLOTS[index - start];

            MenuItems.setButton(
                    container,
                    slot,
                    note.archived() ? Items.BOOK : Items.WRITTEN_BOOK,
                    StaffNoteService.formatNoteId(note.noteId())
                            + " • " + note.authorName()
                            + (note.archived() ? " • Archived" : ""),
                    note.text(),
                    "Added: " + MenuItems.formatDate(note.createdAt())
            );

            UUID noteId = note.noteId();
            actions.put(slot, player -> StaffNoteDetailScreen.open(player, noteId, reopen));
        }

        if (notes.isEmpty()) {
            MenuItems.setButton(
                    container,
                    22,
                    Items.PAPER,
                    "No Staff Notes"
            );
        }

        MenuItems.setButton(
                container,
                FILTER_SLOT,
                Items.HOPPER,
                includeArchived ? "Showing: All Notes" : "Showing: Active Notes",
                includeArchived
                        ? "Click to hide archived notes."
                        : "Click to include archived notes."
        );

        actions.put(FILTER_SLOT, player -> open(
                player,
                targetUuid,
                targetName,
                !includeArchived,
                0,
                back,
                backLabel
        ));

        if (StewardPermissions.has(viewer, StewardPermissions.NOTES_CREATE)) {
            MenuItems.setButton(
                    container,
                    ADD_NOTE_SLOT,
                    Items.WRITABLE_BOOK,
                    "Add Note",
                    "Notes cannot be edited after they are added."
            );

            actions.put(ADD_NOTE_SLOT, player -> TextPromptService.begin(
                    player,
                    INPUT_CHANNEL,
                    "staff note for " + targetName,
                    StaffNoteRecord.MAX_TEXT_LENGTH,
                    (staff, text) -> {
                        StaffNoteService.create(staff, targetUuid, targetName, text);
                        open(staff, targetUuid, targetName, includeArchived, 0, back, backLabel);
                    },
                    reopen
            ));
        }

        MenuItems.setPagination(container, page, totalPages, backLabel);

        actions.put(StewardMenu.PREVIOUS_PAGE_SLOT, player -> {
            if (page > 0) {
                open(player, targetUuid, targetName, includeArchived, page - 1, back, backLabel);
            }
        });

        actions.put(StewardMenu.NEXT_PAGE_SLOT, player -> {
            if (page + 1 < totalPages) {
                open(player, targetUuid, targetName, includeArchived, page + 1, back, backLabel);
            }
        });

        actions.put(StewardMenu.BACK_SLOT, back);
        actions.put(StewardMenu.CLOSE_SLOT, ServerPlayer::closeContainer);

        ActionMenu.open(
                viewer,
                "Staff Notes • " + targetName + " " + (page + 1) + "/" + totalPages,
                container,
                actions,
                StewardPermissions.NOTES_VIEW
        );
    }
}
