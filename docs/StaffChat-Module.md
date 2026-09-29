# Staff Chat Module

## Purpose and behavior

Staff Chat is a private channel for staff. Every online player with
`steward.staff-chat.use` receives every Staff Chat message, and each message is
also written to the server console log.

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

## Mutes

The Staff Chat listener is registered before public mute enforcement, so a
muted staff member with the mode on can still reach Staff Chat, and
`/sc <message>` is a command that mutes never block. Mutes continue to block
the muted player's public chat.

## Permissions

| Permission | Purpose | Operator fallback |
|---|---|---|
| `steward.staff-chat.use` | Send, toggle mode, and receive Staff Chat. | Game master |

## Limitations

- Staff Chat is not persisted as history. Only the console log records it.
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
