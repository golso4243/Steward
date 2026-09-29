package com.swornhero.steward.module.report.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.chat.ClickableRecordId;
import com.swornhero.steward.core.chat.RecordIds;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.report.model.ReportRecord;
import com.swornhero.steward.module.report.model.ReportStatus;
import com.swornhero.steward.module.report.storage.ReportStorageService;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReportService {

    public static final String ID_PREFIX = "RPT";
    public static final Duration SUBMISSION_COOLDOWN = Duration.ofSeconds(60);

    private static final Map<UUID, ReportRecord> REPORTS = new LinkedHashMap<>();
    private static final Map<UUID, Instant> LAST_SUBMISSION = new HashMap<>();

    private ReportService() {
        // Utility class
    }

    public static void register() {
        restore();

        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> deliverPendingNotices(handler.player)
        );
    }

    private static synchronized void restore() {
        REPORTS.clear();

        for (ReportRecord record : ReportStorageService.load()) {
            REPORTS.put(record.reportId(), record);
        }
    }

    /**
     * Validates submission guards that do not depend on the resolved
     * target, so the reporter gets immediate feedback before any
     * profile lookup.
     */
    public static synchronized boolean checkCooldown(ServerPlayer reporter) {
        Duration remaining = remainingCooldown(reporter);

        if (remaining.isZero()) {
            return true;
        }

        reporter.sendSystemMessage(
                Component.literal(
                        "[Steward] Please wait "
                                + Math.max(1, remaining.toSeconds())
                                + " seconds before submitting another report."
                )
        );

        return false;
    }

    public static synchronized ReportRecord submit(
            ServerPlayer reporter,
            UUID targetUuid,
            String targetName,
            String reason
    ) {
        String normalizedReason = reason != null ? reason.trim() : "";

        if (normalizedReason.isEmpty()) {
            reporter.sendSystemMessage(
                    Component.literal("[Steward] Please include a reason for the report.")
            );

            return null;
        }

        if (normalizedReason.length() > ReportRecord.MAX_REASON_LENGTH) {
            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] Report reasons are limited to "
                                    + ReportRecord.MAX_REASON_LENGTH
                                    + " characters."
                    )
            );

            return null;
        }

        if (reporter.getUUID().equals(targetUuid)) {
            reporter.sendSystemMessage(
                    Component.literal("[Steward] You cannot report yourself.")
            );

            return null;
        }

        if (!checkCooldown(reporter)) {
            return null;
        }

        boolean duplicate = REPORTS.values().stream().anyMatch(
                record -> record.isActive()
                        && record.reporterUuid().equals(reporter.getUUID())
                        && record.targetUuid().equals(targetUuid)
        );

        if (duplicate) {
            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] You already have an open report against "
                                    + targetName
                                    + ". Staff will review it soon."
                    )
            );

            return null;
        }

        Instant now = Instant.now();

        ReportRecord record = ReportRecord.create(
                reporter.getUUID(),
                reporter.getName().getString(),
                targetUuid,
                targetName,
                normalizedReason,
                now
        );

        REPORTS.put(record.reportId(), record);

        if (!save()) {
            REPORTS.remove(record.reportId());

            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] Your report could not be saved. Please contact staff directly."
                    )
            );

            return null;
        }

        LAST_SUBMISSION.put(reporter.getUUID(), now);

        reporter.sendSystemMessage(
                Component.literal(
                        "[Steward] Thank you. Your report against "
                                + targetName
                                + " was submitted as "
                                + formatReportId(record.reportId())
                                + "."
                ).withStyle(ChatFormatting.GREEN)
        );

        Steward.LOGGER.info(
                "Report {} submitted by {} against {}: {}",
                formatReportId(record.reportId()),
                record.reporterName(),
                record.targetName(),
                record.reason()
        );

        alertStaff(reporter.level().getServer(), record);
        return record;
    }

    public static synchronized boolean claim(
            ServerPlayer staff,
            UUID reportId
    ) {
        ReportRecord record = requireManageable(staff, reportId);

        if (record == null) {
            return false;
        }

        if (record.status() != ReportStatus.OPEN) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] Only open reports can be claimed.")
            );

            return false;
        }

        return update(
                staff,
                record.withClaim(
                        staff.getUUID(),
                        staff.getName().getString(),
                        Instant.now()
                ),
                "claimed"
        );
    }

    public static synchronized boolean unclaim(
            ServerPlayer staff,
            UUID reportId
    ) {
        ReportRecord record = requireManageable(staff, reportId);

        if (record == null) {
            return false;
        }

        if (record.status() != ReportStatus.CLAIMED) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] That report is not claimed.")
            );

            return false;
        }

        if (!mayActOnClaim(staff, record)) {
            return false;
        }

        return update(staff, record.withoutClaim(), "released");
    }

    public static synchronized boolean resolve(
            ServerPlayer staff,
            UUID reportId,
            String note
    ) {
        return close(staff, reportId, ReportStatus.RESOLVED, note);
    }

    public static synchronized boolean dismiss(
            ServerPlayer staff,
            UUID reportId,
            String note
    ) {
        return close(staff, reportId, ReportStatus.DISMISSED, note);
    }

    private static boolean close(
            ServerPlayer staff,
            UUID reportId,
            ReportStatus closedStatus,
            String note
    ) {
        ReportRecord record = requireManageable(staff, reportId);

        if (record == null) {
            return false;
        }

        if (!record.isActive()) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] That report is already closed.")
            );

            return false;
        }

        if (closedStatus == ReportStatus.RESOLVED
                && record.status() != ReportStatus.CLAIMED) {

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] Claim the report before resolving it."
                    )
            );

            return false;
        }

        if (record.status() == ReportStatus.CLAIMED
                && !mayActOnClaim(staff, record)) {

            return false;
        }

        String normalizedNote = note != null ? note.trim() : "";

        if (normalizedNote.isEmpty()
                || normalizedNote.length() > ReportRecord.MAX_RESOLUTION_LENGTH) {

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] A resolution note of 1-"
                                    + ReportRecord.MAX_RESOLUTION_LENGTH
                                    + " characters is required."
                    )
            );

            return false;
        }

        ReportRecord closed = record.withClosure(
                closedStatus,
                staff.getUUID(),
                staff.getName().getString(),
                Instant.now(),
                normalizedNote
        );

        if (!update(staff, closed, closedStatus.displayName().toLowerCase())) {
            return false;
        }

        MinecraftServer server = staff.level().getServer();
        ServerPlayer reporter = server != null
                ? server.getPlayerList().getPlayer(closed.reporterUuid())
                : null;

        if (reporter != null) {
            deliverPendingNotices(reporter);
        }

        return true;
    }

    public static synchronized ReportRecord findById(UUID reportId) {
        return reportId != null ? REPORTS.get(reportId) : null;
    }

    public static synchronized ReportRecord findByDisplayId(String displayId) {
        String compact = RecordIds.normalize(ID_PREFIX, displayId);

        if (compact == null) {
            return null;
        }

        ReportRecord match = null;

        for (ReportRecord record : REPORTS.values()) {
            if (!RecordIds.compact(record.reportId()).equals(compact)) {
                continue;
            }

            if (match != null) {
                Steward.LOGGER.error("Report display ID {} is ambiguous.", displayId);
                return null;
            }

            match = record;
        }

        return match;
    }

    /**
     * Active reports, oldest first, so the queue is triaged in order.
     */
    public static synchronized List<ReportRecord> activeReports() {
        return REPORTS.values().stream()
                .filter(ReportRecord::isActive)
                .sorted(Comparator.comparing(ReportRecord::createdAt))
                .toList();
    }

    /**
     * Every report, newest first.
     */
    public static synchronized List<ReportRecord> allReports() {
        return REPORTS.values().stream()
                .sorted(Comparator.comparing(ReportRecord::createdAt).reversed())
                .toList();
    }

    public static synchronized List<ReportRecord> reportsAgainst(UUID targetUuid) {
        return REPORTS.values().stream()
                .filter(record -> record.targetUuid().equals(targetUuid))
                .sorted(Comparator.comparing(ReportRecord::createdAt).reversed())
                .toList();
    }

    public static synchronized List<ReportRecord> reportsBy(UUID reporterUuid) {
        return REPORTS.values().stream()
                .filter(record -> record.reporterUuid().equals(reporterUuid))
                .sorted(Comparator.comparing(ReportRecord::createdAt).reversed())
                .toList();
    }

    public static synchronized long activeCount() {
        return REPORTS.values().stream().filter(ReportRecord::isActive).count();
    }

    public static String formatReportId(UUID reportId) {
        return RecordIds.format(ID_PREFIX, reportId);
    }

    public static synchronized void deliverPendingNotices(ServerPlayer reporter) {
        boolean changed = false;

        for (ReportRecord record : List.copyOf(REPORTS.values())) {
            if (record.isActive()
                    || record.reporterNotified()
                    || !record.reporterUuid().equals(reporter.getUUID())) {
                continue;
            }

            reporter.sendSystemMessage(
                    Component.literal(
                            "[Steward] Your report "
                                    + formatReportId(record.reportId())
                                    + " against "
                                    + record.targetName()
                                    + " was reviewed by staff and "
                                    + record.status().displayName().toLowerCase()
                                    + ". Thank you for reporting."
                    ).withStyle(ChatFormatting.AQUA)
            );

            REPORTS.put(record.reportId(), record.withReporterNotified());
            changed = true;
        }

        if (changed && !save()) {
            Steward.LOGGER.warn(
                    "Report notices were delivered to {} but the delivery state could not be saved.",
                    reporter.getName().getString()
            );
        }
    }

    private static Duration remainingCooldown(ServerPlayer reporter) {
        Instant last = LAST_SUBMISSION.get(reporter.getUUID());

        if (last == null
                || StewardPermissions.has(reporter, StewardPermissions.REPORT_BYPASS_COOLDOWN)) {
            return Duration.ZERO;
        }

        Duration remaining = Duration.between(
                Instant.now(),
                last.plus(SUBMISSION_COOLDOWN)
        );

        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    private static ReportRecord requireManageable(
            ServerPlayer staff,
            UUID reportId
    ) {
        if (!StewardPermissions.require(staff, StewardPermissions.REPORT_MANAGE)) {
            return null;
        }

        ReportRecord record = REPORTS.get(reportId);

        if (record == null) {
            staff.sendSystemMessage(
                    Component.literal("[Steward] That report could not be found.")
            );

            return null;
        }

        if (record.targetUuid().equals(staff.getUUID())) {
            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] You cannot handle a report filed against you."
                    )
            );

            return null;
        }

        return record;
    }

    private static boolean mayActOnClaim(
            ServerPlayer staff,
            ReportRecord record
    ) {
        if (record.isClaimedBy(staff.getUUID())
                || StewardPermissions.has(staff, StewardPermissions.REPORT_OVERRIDE_CLAIM)) {
            return true;
        }

        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] That report is claimed by "
                                + record.claimedByName()
                                + "."
                )
        );

        return false;
    }

    private static boolean update(
            ServerPlayer staff,
            ReportRecord updated,
            String verb
    ) {
        ReportRecord previous = REPORTS.put(updated.reportId(), updated);

        if (!save()) {
            REPORTS.put(previous.reportId(), previous);

            staff.sendSystemMessage(
                    Component.literal(
                            "[Steward] The report change could not be saved; no change was made."
                    )
            );

            return false;
        }

        staff.sendSystemMessage(
                Component.literal(
                        "[Steward] Report "
                                + formatReportId(updated.reportId())
                                + " " + verb + "."
                ).withStyle(ChatFormatting.GREEN)
        );

        Steward.LOGGER.info(
                "Report {} {} by {}.",
                formatReportId(updated.reportId()),
                verb,
                staff.getName().getString()
        );

        return true;
    }

    private static void alertStaff(
            MinecraftServer server,
            ReportRecord record
    ) {
        if (server == null) {
            return;
        }

        String displayId = formatReportId(record.reportId());

        Component alert = Component.literal("[Steward] New report ")
                .withStyle(ChatFormatting.GOLD)
                .append(ClickableRecordId.create(
                        displayId,
                        "/steward view report " + displayId,
                        "Open report " + displayId
                ))
                .append(Component.literal(
                        ": " + record.reporterName()
                                + " reported " + record.targetName()
                                + " - " + record.reason()
                ).withStyle(ChatFormatting.YELLOW));

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (StewardPermissions.has(player, StewardPermissions.REPORT_ALERTS)) {
                player.sendSystemMessage(alert);
            }
        }
    }

    private static boolean save() {
        return ReportStorageService.save(REPORTS.values());
    }
}
