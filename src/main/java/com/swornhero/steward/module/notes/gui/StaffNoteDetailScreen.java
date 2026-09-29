package com.swornhero.steward.module.notes.gui;

import com.swornhero.steward.core.gui.ActionMenu;
import com.swornhero.steward.core.gui.MenuItems;
import com.swornhero.steward.core.gui.StewardMenu;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.notes.model.StaffNoteRecord;
import com.swornhero.steward.module.notes.service.StaffNoteService;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public final class StaffNoteDetailScreen {

    private static final int SUMMARY_SLOT = 13;
    private static final int TEXT_SLOT = 22;
    private static final int AUTHOR_SLOT = 29;
    private static final int TARGET_SLOT = 31;
    private static final int ARCHIVE_INFO_SLOT = 33;
    private static final int ARCHIVE_SLOT = 40;

    private static final int CONFIRM_SLOT = 29;
    private static final int CANCEL_SLOT = 33;

    private StaffNoteDetailScreen() {
        // Utility class
    }

    public static void open(
            ServerPlayer viewer,
            UUID noteId,
            Consumer<ServerPlayer> back
    ) {
        if (!StewardPermissions.require(viewer, StewardPermissions.NOTES_VIEW)) {
            return;
        }

        StaffNoteRecord note = StaffNoteService.findById(noteId);

        if (note == null) {
            viewer.sendSystemMessage(
                    Component.literal("[Steward] That staff note could not be found.")
            );

            back.accept(viewer);
            return;
        }

        String displayId = StaffNoteService.formatNoteId(noteId);
        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();

        MenuItems.setButton(
                container,
                SUMMARY_SLOT,
                note.archived() ? Items.BOOK : Items.WRITTEN_BOOK,
                displayId + (note.archived() ? " • Archived" : " • Active"),
                "Added: " + MenuItems.formatDate(note.createdAt())
        );

        MenuItems.setButton(container, TEXT_SLOT, Items.WRITABLE_BOOK, "Note", note.text());
        MenuItems.setButton(container, AUTHOR_SLOT, Items.PLAYER_HEAD, "Author: " + note.authorName());
        MenuItems.setButton(container, TARGET_SLOT, Items.PLAYER_HEAD, "Player: " + note.targetName());

        if (note.archived()) {
            MenuItems.setButton(
                    container,
                    ARCHIVE_INFO_SLOT,
                    Items.BOOK,
                    "Archived by " + note.archivedByName(),
                    "Archived: " + MenuItems.formatDate(note.archivedAt())
            );
        }

        if (StaffNoteService.canArchive(viewer, note)) {
            MenuItems.setButton(
                    container,
                    ARCHIVE_SLOT,
                    Items.REDSTONE,
                    "Archive Note",
                    "Hides the note from default views. It stays in history."
            );

            actions.put(ARCHIVE_SLOT, player -> openArchiveConfirm(player, noteId, back));
        }

        MenuItems.setButton(container, StewardMenu.BACK_SLOT, Items.OAK_DOOR, "Back");
        MenuItems.setButton(container, StewardMenu.CLOSE_SLOT, Items.BARRIER, "Close");
        actions.put(StewardMenu.BACK_SLOT, back);
        actions.put(StewardMenu.CLOSE_SLOT, ServerPlayer::closeContainer);

        ActionMenu.open(
                viewer,
                "Staff Note • " + displayId,
                container,
                actions,
                StewardPermissions.NOTES_VIEW
        );
    }

    private static void openArchiveConfirm(
            ServerPlayer viewer,
            UUID noteId,
            Consumer<ServerPlayer> back
    ) {
        StaffNoteRecord note = StaffNoteService.findById(noteId);

        if (!StaffNoteService.canArchive(viewer, note)) {
            viewer.sendSystemMessage(
                    Component.literal("[Steward] That note can no longer be archived by you.")
            );

            open(viewer, noteId, back);
            return;
        }

        String displayId = StaffNoteService.formatNoteId(noteId);
        SimpleContainer container = MenuItems.borderedContainer();
        Map<Integer, Consumer<ServerPlayer>> actions = new HashMap<>();

        MenuItems.setButton(
                container,
                SUMMARY_SLOT,
                Items.WRITTEN_BOOK,
                "Archive " + displayId + "?",
                note.text()
        );

        MenuItems.setButton(container, CONFIRM_SLOT, Items.EMERALD, "Confirm Archive");
        MenuItems.setButton(container, CANCEL_SLOT, Items.BARRIER, "Cancel");

        actions.put(CONFIRM_SLOT, player -> {
            StaffNoteService.archive(player, noteId);
            back.accept(player);
        });

        actions.put(CANCEL_SLOT, player -> open(player, noteId, back));

        ActionMenu.open(
                viewer,
                "Archive Note • " + displayId,
                container,
                actions,
                StewardPermissions.NOTES_VIEW
        );
    }
}
