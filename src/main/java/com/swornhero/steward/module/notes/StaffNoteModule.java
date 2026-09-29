package com.swornhero.steward.module.notes;

import com.swornhero.steward.module.notes.service.StaffNoteService;

public final class StaffNoteModule {

    private StaffNoteModule() {
        // Utility class
    }

    public static void register() {
        StaffNoteService.register();
    }
}
