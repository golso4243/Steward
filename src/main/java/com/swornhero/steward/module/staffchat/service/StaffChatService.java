package com.swornhero.steward.module.staffchat.service;

import com.swornhero.steward.Steward;
import com.swornhero.steward.core.permission.StewardPermissions;
import com.swornhero.steward.module.staffchat.model.StaffChatMessage;
import com.swornhero.steward.module.staffchat.storage.StaffChatHistoryStorage;
import com.swornhero.steward.module.staffchat.storage.StaffChatStorageService;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class StaffChatService {
    public static final int MAX_MESSAGE_LENGTH = 256;

    public static final int HISTORY_PAGE_SIZE = 10;

    /**
     * Messages kept in memory for in-game history. The log file on disk
     * keeps every message regardless of this limit.
     */
    public static final int HISTORY_MEMORY_LIMIT = 5000;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter HISTORY_TIME_FORMAT =
            DateTimeFormatter.ofPattern("MMM d HH:mm").withZone(ZoneId.systemDefault());
    private static final Set<UUID> TOGGLED = new HashSet<>();
    private static final Deque<StaffChatMessage> HISTORY = new ArrayDeque<>();

    private StaffChatService() {
    }

    public static void register() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register(
                (message, sender, params) -> allowChatMessage(sender, message.signedContent())
        );
        ServerPlayConnectionEvents.JOIN.register(
                (handler, sender, server) -> onJoin(handler.player)
        );
    }

    public static synchronized void load() {
        TOGGLED.clear();
        TOGGLED.addAll(StaffChatStorageService.load());
        HISTORY.clear();
        HISTORY.addAll(StaffChatHistoryStorage.loadRecent(HISTORY_MEMORY_LIMIT));
        Steward.LOGGER.info("Loaded {} persistent staff-chat toggles and {} history messages.",
                TOGGLED.size(), HISTORY.size());
    }

    /**
     * Shows one page of Staff Chat history in chat. Page 1 holds the
     * most recent messages; each page is printed oldest to newest.
     */
    public static synchronized void showHistory(ServerPlayer viewer, int requestedPage) {
        if (!StewardPermissions.require(viewer, StewardPermissions.STAFF_CHAT_HISTORY)) {
            return;
        }
        if (HISTORY.isEmpty()) {
            viewer.sendSystemMessage(Component.literal("[Steward] Staff Chat history is empty."));
            return;
        }

        List<StaffChatMessage> messages = new ArrayList<>(HISTORY);
        int totalPages = (messages.size() + HISTORY_PAGE_SIZE - 1) / HISTORY_PAGE_SIZE;
        int page = Math.max(1, Math.min(requestedPage, totalPages));
        int end = messages.size() - (page - 1) * HISTORY_PAGE_SIZE;
        int start = Math.max(0, end - HISTORY_PAGE_SIZE);

        viewer.sendSystemMessage(Component.literal("━━━ Staff Chat History • Page " + page
                + " of " + totalPages + " ━━━").withStyle(ChatFormatting.GOLD));

        for (StaffChatMessage message : messages.subList(start, end)) {
            viewer.sendSystemMessage(Component.literal(
                            HISTORY_TIME_FORMAT.format(message.sentAt()) + " ")
                    .withStyle(ChatFormatting.DARK_GRAY)
                    .append(Component.literal(message.senderName()).withStyle(ChatFormatting.AQUA))
                    .append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
                    .append(Component.literal(message.text()).withStyle(ChatFormatting.WHITE)));
        }

        MutableComponent footer = Component.literal("");
        if (page < totalPages) {
            footer.append(pageLink("« Older", page + 1));
        }
        if (page > 1) {
            if (page < totalPages) {
                footer.append(Component.literal("   "));
            }
            footer.append(pageLink("Newer »", page - 1));
        }
        if (totalPages > 1) {
            viewer.sendSystemMessage(footer);
        }
    }

    private static Component pageLink(String label, int page) {
        return Component.literal(label).withStyle(style -> style
                .withColor(ChatFormatting.YELLOW)
                .withUnderlined(true)
                .withClickEvent(new ClickEvent.RunCommand("/schistory " + page))
                .withHoverEvent(new HoverEvent.ShowText(Component.literal("Show page " + page))));
    }

    public static synchronized boolean isToggled(ServerPlayer player) {
        return TOGGLED.contains(player.getUUID());
    }

    public static synchronized void toggle(ServerPlayer player) {
        if (!StewardPermissions.require(player, StewardPermissions.STAFF_CHAT_USE)) {
            return;
        }
        UUID uuid = player.getUUID();
        boolean enable = !TOGGLED.contains(uuid);
        if (enable) {
            TOGGLED.add(uuid);
        } else {
            TOGGLED.remove(uuid);
        }
        if (!StaffChatStorageService.save(TOGGLED)) {
            if (enable) {
                TOGGLED.remove(uuid);
            } else {
                TOGGLED.add(uuid);
            }
            player.sendSystemMessage(Component.literal(
                    "[Steward] Staff Chat mode could not be persisted; no change was made."));
            return;
        }
        player.sendSystemMessage(enable
                ? Component.literal("[Steward] Staff Chat mode enabled. Your chat messages now go to staff only.")
                        .withStyle(ChatFormatting.GREEN)
                : Component.literal("[Steward] Staff Chat mode disabled. Your chat messages are public again.")
                        .withStyle(ChatFormatting.YELLOW));
    }

    public static boolean send(ServerPlayer sender, String text) {
        if (!StewardPermissions.require(sender, StewardPermissions.STAFF_CHAT_USE)) {
            return false;
        }
        String normalized = text != null ? text.trim() : "";
        if (normalized.isEmpty()) {
            sender.sendSystemMessage(Component.literal("[Steward] Staff Chat messages cannot be blank."));
            return false;
        }
        if (normalized.length() > MAX_MESSAGE_LENGTH) {
            sender.sendSystemMessage(Component.literal("[Steward] Staff Chat messages are limited to "
                    + MAX_MESSAGE_LENGTH + " characters."));
            return false;
        }
        MinecraftServer server = sender.level().getServer();
        if (server == null) {
            return false;
        }
        String senderName = sender.getName().getString();
        if (!record(new StaffChatMessage(sender.getUUID(), senderName, normalized, Instant.now()))) {
            sender.sendSystemMessage(Component.literal(
                    "[Steward] Your message was delivered but could not be saved to Staff Chat history."));
        }
        Component message = format(senderName, normalized);
        for (ServerPlayer recipient : server.getPlayerList().getPlayers()) {
            if (StewardPermissions.has(recipient, StewardPermissions.STAFF_CHAT_USE)) {
                recipient.sendSystemMessage(message);
            }
        }
        server.sendSystemMessage(message);
        return true;
    }

    public static Component format(String senderName, String text) {
        MutableComponent name = Component.literal(senderName).withStyle(style -> style
                .withColor(ChatFormatting.AQUA)
                .withHoverEvent(new HoverEvent.ShowText(
                        Component.literal("Sent at " + LocalTime.now().format(TIME_FORMAT)))));
        return Component.literal("[Staff] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                .append(name.withStyle(style -> style.withBold(false)))
                .append(Component.literal(": ").withStyle(style -> style
                        .withBold(false).withColor(ChatFormatting.GRAY)))
                .append(Component.literal(text).withStyle(style -> style
                        .withBold(false).withColor(ChatFormatting.WHITE)));
    }

    private static synchronized boolean record(StaffChatMessage message) {
        HISTORY.addLast(message);
        if (HISTORY.size() > HISTORY_MEMORY_LIMIT) {
            HISTORY.removeFirst();
        }
        return StaffChatHistoryStorage.append(message);
    }

    private static boolean allowChatMessage(ServerPlayer sender, String content) {
        synchronized (StaffChatService.class) {
            if (!TOGGLED.contains(sender.getUUID())) {
                return true;
            }
            if (!StewardPermissions.has(sender, StewardPermissions.STAFF_CHAT_USE)) {
                clearToggle(sender.getUUID());
                return true;
            }
        }
        send(sender, content);
        return false;
    }

    private static void onJoin(ServerPlayer player) {
        synchronized (StaffChatService.class) {
            if (!TOGGLED.contains(player.getUUID())) {
                return;
            }
            if (!StewardPermissions.has(player, StewardPermissions.STAFF_CHAT_USE)) {
                clearToggle(player.getUUID());
                return;
            }
        }
        player.sendSystemMessage(Component.literal(
                        "[Steward] Staff Chat mode is still enabled. Use /sc to return to public chat.")
                .withStyle(ChatFormatting.GOLD));
    }

    private static void clearToggle(UUID uuid) {
        if (TOGGLED.remove(uuid) && !StaffChatStorageService.save(TOGGLED)) {
            Steward.LOGGER.warn("Cleared staff-chat toggle for {} but could not persist the change.", uuid);
        }
    }
}
