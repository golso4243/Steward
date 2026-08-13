# Vanish Module

## Purpose and behavior

Vanish is a staff privacy tool. A staff member can toggle their own state from
the Staff Control panel or the Vanish tool in Staff Mode. The state is written
to `steward/data/vanished-players.json` before the visible state changes and is
reapplied after a disconnect or server restart.

While vanished, Minecraft's server-side invisible flag hides the player's
model from every viewer. Steward additionally removes the vanished player's
entry from the tab list of viewers who lack `steward.vanish.see`. The vanished
player keeps their own entry. Viewers with the see permission retain the tab
entry, receive staff-awareness messages when authorized, and can therefore
identify the vanished staff member even though the entity model remains
invisible.

Disabling Vanish clears the invisible flag and initializes the player's tab
entry for all connected players. A persistence write failure is fail-closed:
the in-memory state is rolled back and no visibility change is made.

## Permissions

| Permission | Purpose | Operator fallback |
|---|---|---|
| `steward.vanish.use` | Toggle the caller's own Vanish state. | Game master |
| `steward.vanish.see` | Keep vanished staff identifiable in the tab list. | Game master |
| `steward.vanish.notifications` | Receive enable, disable, and vanished-reconnect awareness notices. | Game master |

The notification permission is separate because operational awareness can
disclose a vanished staff member even when the recipient should not otherwise
identify vanished staff. Deployments should normally grant notifications only
with `steward.vanish.see`.

## Connection and persistence operations

1. On server start Steward loads the UUID set from disk.
2. On join, a persisted vanished player has invisibility and per-viewer tab
   policy reapplied on the server task queue, after initial connection setup.
3. Every joining viewer has all currently vanished tab entries reconciled.
4. On toggle, the new UUID set is atomically replaced on disk before packets or
   entity state change.
5. To recover manually, stop the server, back up and edit
   `steward/data/vanished-players.json`, then restart. Do not edit it while the
   server is running.

## Minecraft 26.2 API decisions and limitations

This increment deliberately uses the mapped server APIs
`ServerPlayer.setInvisible`, `ClientboundPlayerInfoRemovePacket`, and
`ClientboundPlayerInfoUpdatePacket.createPlayerInitializing`. It does not add a
tracking mixin or send entity-destroy/spawn packet sequences. The latter would
be sensitive to protocol and tracking lifecycle changes and could desynchronize
clients.

Consequently, Vanish is privacy-oriented rather than a security boundary:

- authorized staff identify vanished players through tab and notifications;
  their entity model is still invisible;
- Minecraft may still expose armor, held items, particles, sounds, collision,
  commands, or indirect world interaction;
- Steward does not remove the entity from chunk tracking;
- Fabric exposes no stable targeted hook for suppressing vanilla join/leave
  system messages in this dependency set. A default server may briefly reveal
  a persisted vanished player's name in those messages before tab policy is
  reconciled. Operators requiring strict connection-message secrecy must use a
  compatible connection-message mod and validate its ordering with Steward.

These limitations are intentional rather than silently relying on invasive
mixins. They must be retested whenever Minecraft mappings or Fabric APIs change.

## Runtime operating checklist

- Test ordinary, `vanish.see`, and `vanish.notifications` accounts separately.
- Verify tab removal and restoration with players already online and joining
  after Vanish is enabled.
- Disconnect and reconnect a vanished account, then restart the server while it
  is offline and reconnect again.
- Confirm persisted JSON is valid and an unwritable data directory leaves the
  prior state active with an error response.
- Observe entity model, armor, held items, particles, sounds, collision, and
  join/leave messages so the deployment's exact concealment boundary is known.
