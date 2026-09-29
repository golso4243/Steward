package com.swornhero.steward.module.report.storage;

import com.google.gson.reflect.TypeToken;
import com.swornhero.steward.Steward;
import com.swornhero.steward.core.storage.JsonListStorage;
import com.swornhero.steward.module.report.model.ReportEntry;
import com.swornhero.steward.module.report.model.ReportRecord;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class ReportStorageService {

    private static final JsonListStorage<ReportEntry> STORAGE =
            new JsonListStorage<>(
                    Path.of("steward", "history"),
                    "reports.json",
                    new TypeToken<List<ReportEntry>>() {
                    }.getType(),
                    "report history"
            );

    private ReportStorageService() {
        // Utility class
    }

    public static List<ReportRecord> load() {
        List<ReportRecord> records = new ArrayList<>();
        Set<UUID> loadedIds = new HashSet<>();

        for (ReportEntry entry : STORAGE.load()) {
            if (entry == null) {
                Steward.LOGGER.warn("Skipped null report entry.");
                continue;
            }

            try {
                ReportRecord record = entry.toRecord();

                if (!loadedIds.add(record.reportId())) {
                    Steward.LOGGER.error(
                            "Skipped duplicate report ID {}.",
                            record.reportId()
                    );

                    continue;
                }

                records.add(record);
            } catch (RuntimeException exception) {
                Steward.LOGGER.error(
                        "Failed to restore report entry.",
                        exception
                );
            }
        }

        Steward.LOGGER.info("Loaded {} report records.", records.size());
        return List.copyOf(records);
    }

    public static boolean save(Collection<ReportRecord> records) {
        return STORAGE.save(
                records.stream()
                        .sorted(Comparator.comparing(ReportRecord::createdAt))
                        .map(ReportEntry::fromRecord)
                        .toList()
        );
    }
}
