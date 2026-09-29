# Staff Chat Module

## Purpose and behavior

Staff Chat is a private channel for staff. Every online player with
`steward.staff-chat.use` receives every Staff Chat message. Each message is
also saved to Steward's Staff Chat history and written to the server console
log.

There are two ways to send:

- `/sc <message>` (alias `/staffchat <message>`) sends one message without
  changing modes.
- `/sc` with no message toggles Staff Chat mode. While the mode is on, the
  staff member's normal chat messages are cancelled before public broadcast and
  sent to Staff Chat instead.

Staff Chat mode can also be toggled from the Staff Control panel, where the
button shows `Staff Chat Mode: On` or `Off`, and from the Staff Chat tool in
Staff Mode hotbar slot 7.

Messages are formatted as a bold gold `[Staff]` tag, the sender's name in aqua,
and the message in white. Hovering the sender's name shows the send time.
Messages are trimmed, blank messages are rejected, and messages are limited to
256 characters.

## Mode persistence

The set of players with the mode on is written atomically to
`steward/data/staff-chat-toggles.json` before the change takes effect. A write
failure rolls the toggle back and tells the actor that no change was made.

When a player with the mode on reconnects, Steward reminds them that their chat
is still routed to staff. If the player no longer holds
`steward.staff-chat.use`, whether at join or when they next chat, the mode is
silently cleared and their message is sent publicly as normal.

## History

Every sent message (sender UUID, sender name, text, and time) is appended as
one JSON line to `steward/history/staff-chat.jsonl`. Appending means the file
is never rewritten, and a damaged line is skipped at load without hiding the
rest. The file keeps every message; Steward does not prune it.

`/schistory [page]` shows the history in chat, 10 messages per page. Page 1 is
the most recent messages, each page reads oldest to newest, and clickable
`« Older` and `Newer »` links move between pages. The most recent 5,000
messages are loaded into memory at server start and are available in game;
older messages remain in the file.

If a message cannot be saved, it is still delivered, and the sender is told
that it was not saved to history.

## Mutes

The Staff Chat listener is registered before public mute enforcement, so a
muted staff member with the mode on can still reach Staff Chat, and
`/sc <message>` is a command that mutes never block. Mutes continue to block
the muted player's public chat.

## Permissions

| Permission | Purpose | Operator fallback |
|---|---|---|
| `steward.staff-chat.use` | Send, toggle mode, and receive Staff Chat. | Game master |
| `steward.staff-chat.history` | View Staff Chat history with `/schistory`. | Game master |

## Limitations

- In game, `/schistory` reaches only the most recent 5,000 messages. Older
  messages must be read from `steward/history/staff-chat.jsonl`.
- The history file grows without limit; archive or trim it manually while the
  server is stopped.
- Messages are sent as system messages, so they are not signed and cannot be
  reported through Mojang's chat reporting.
- Recipients are checked when each message is sent. A permission change takes
  effect on the next message.

## Runtime operating checklist

- Send with `/sc <message>` and `/staffchat <message>` and confirm only staff
  and the console receive it.
- Toggle the mode from the command, the control panel, and the Staff Mode tool;
  confirm normal chat is routed privately while on and publicly while off.
- Mute a staff member with the mode on and confirm Staff Chat still works while
  public chat is blocked after toggling off.
- Reconnect and restart with the mode on and confirm it is restored and the
  reminder appears.
- Revoke the permission while the mode is on and confirm the next chat message
  is public and the mode is cleared.
- Send messages, restart, and confirm `/schistory` shows them in order with
  working page links; confirm players without `steward.staff-chat.history`
  cannot run it.
