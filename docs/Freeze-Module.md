# Steward Freeze Module Documentation

## Overview

The **Freeze module** is Steward's first completed moderation module. It provides a GUI-first workflow for temporarily restricting a player while staff investigate a situation, communicate with the player, or prepare further moderation action.

The system is designed to do more than simply stop movement. A frozen player is kept at a recorded anchor position, protected from environmental hazards, restricted from normal interactions, tracked through disconnects and reconnects, and recorded in both active-case storage and completed history.

The module also supports:

- Online and offline unfreezing
- Staff hierarchy protection
- Permission-based access
- Confirmed frozen-player relocation
- Original-location preservation
- Relocation history
- Unique freeze case IDs
- Active freeze persistence across restarts
- Completed freeze history
- Console audit logging
- Configurable freeze behavior

---

## Primary Staff Commands

### `/steward`

Opens the Steward staff interface.

This is the main entry point for staff using the Freeze module.

Required permission:

```text
steward.staff.open
```

### `/staff`

Alias for `/steward`.

It opens the same Steward staff interface.

Required permission:

```text
steward.staff.open
```

### `/steward status`

Displays Steward's core status information, including installed version information, module status, developer attribution, and the SwornHero website.

Required permission:

```text
steward.status.view
```

The status command is intended for staff, administrators, developers, and owners. It does not display active freeze counts or private case information.

---

## Freeze Workflow

Steward's Freeze module is primarily operated through the staff GUI rather than direct `/freeze` or `/unfreeze` commands.

A typical workflow is:

1. Run `/steward` or `/staff`.
2. Open the player browser.
3. Select the player.
4. Open the player's moderation profile.
5. Choose the freeze action.
6. Enter or confirm the freeze reason.
7. Review the active freeze through the Active Freezes screen.
8. Unfreeze the player when the review is complete.

This GUI-first design reduces command mistakes and gives staff a consistent way to view case information before acting.

---

## What Happens When a Player Is Frozen

When staff freeze a player, Steward creates a new freeze record containing:

- A unique freeze case ID
- Target UUID
- Target name
- Staff UUID
- Staff name
- Freeze reason
- Original dimension
- Original coordinates
- Original yaw and pitch
- Freeze timestamp
- Current freeze anchor
- Disconnect count
- Reconnect count
- Relocation history

The original freeze position is preserved for the lifetime of the case.

At first, the current freeze anchor is effectively the same as the original freeze position. If staff later relocate the player, the original position remains unchanged while the current anchor is updated.

The player receives messages explaining that they have been frozen and should remain connected while waiting for staff instructions.

---

## Movement Restriction

Steward maintains frozen players every server tick.

While frozen, the player's movement velocity is repeatedly cleared and the player is restored to the current freeze anchor. This prevents ordinary movement, knockback, falling, swimming, or other displacement from permanently moving the player away from the controlled location.

The system records:

- Dimension
- X coordinate
- Y coordinate
- Z coordinate
- Yaw
- Pitch

If the current anchor is in another dimension, Steward can restore the player across dimensions.

---

## Interaction Restrictions

Frozen players are prevented from carrying out normal gameplay actions.

The implemented protection is intended to cover actions such as:

- Breaking blocks
- Placing blocks
- Using blocks
- Opening containers
- Using items
- Eating and drinking
- Dropping items
- Swapping offhand items
- Moving inventory contents
- Attacking entities
- Interacting with entities
- Picking up items
- Picking up experience
- Using blocked commands
- Other packet-driven interactions covered by Steward's freeze protections

When a client predicts an inventory change that the server rejects, Steward resynchronizes inventory and container data to reduce ghost items or client/server disagreement.

---

## Command Restrictions While Frozen

Frozen players may only use commands listed in the freeze policy configuration.

Default allowed root commands:

```text
msg
tell
w
reply
r
staff
steward
```

Commands are entered in the configuration without the leading slash.

All other commands are blocked by default. The default blocked-command message is:

```text
You cannot use that command while frozen.
```

This allows a frozen player to communicate with staff while preventing commands that could escape or interfere with the freeze.

Configuration file:

```text
config/steward/freeze-policy.json
```

---

## Damage and Environmental Safety

By default, Steward protects frozen players from incoming damage.

The freeze policy includes settings for:

```json
{
  "preventDamageWhileFrozen": true,
  "extinguishFrozenPlayers": true,
  "restoreAirWhileFrozen": true
}
```

With the default settings:

- Incoming damage is prevented.
- Fire ticks are cleared.
- Air supply is restored.
- Fall distance is reset.
- Movement velocity is cleared.

These protections are intended to stop a frozen player from dying because they were frozen in fire, lava, water, during a fall, or near another hazard.

---

## Disconnect and Reconnect Behavior

A freeze remains active when the player disconnects.

### Disconnect

When a frozen player disconnects:

- The disconnect count is incremented.
- The active case is saved.
- Authorized staff may receive an alert.
- The event is written to the audit log.

### Reconnect

When the player reconnects:

- The reconnect count is incremented.
- The player remains frozen.
- Steward restores the player to the current freeze anchor.
- The player's inventory is synchronized.
- The player may be shown the recorded freeze reason.
- Authorized staff may receive a reconnect alert.

The exact alert and reconnect-message behavior is controlled by the freeze policy.

---

## Active Freeze Management

The Active Freezes interface allows authorized staff to review current cases.

A freeze detail view can include:

- Full case ID
- Player name
- Freeze reason
- Frozen-by staff member
- Freeze timestamp
- Original dimension and coordinates
- Current anchor dimension and coordinates
- Disconnect count
- Reconnect count
- Relocation controls
- Unfreeze controls

Required permission:

```text
steward.freeze.view
```

Management actions may require additional permissions.

---

## Frozen-Player Relocation

Staff may move an online frozen player to the staff member's current location.

This is useful when:

- The original location is unsafe.
- The player needs to be moved away from other players.
- Staff want to conduct the review in a controlled area.
- The original area is obstructed or otherwise unsuitable.

Required permission:

```text
steward.freeze.relocate
```

### Confirmation Requirement

Relocation uses a confirmation screen.

Before confirming, staff can review:

- Original freeze location
- Current freeze anchor
- Proposed destination
- A warning that relocation changes the investigation context

No relocation occurs until staff confirm the action.

### What Is Preserved

Relocation does not overwrite the original freeze position.

Steward stores:

- Original freeze position
- Previous anchor
- New anchor
- Relocating staff UUID
- Relocating staff name
- Relocation timestamp
- Relocation note

The player remains frozen after relocation.

### Relocation Safety Check

The destination must have usable space at the player's feet and head positions.

If the destination is unsafe, unavailable, or cannot be restored successfully:

- The relocation is rejected or rolled back.
- The previous anchor is restored.
- The unsuccessful relocation is not retained as a completed relocation entry.

The target must be online to be relocated.

---

## Safe-Position Fallback

When Steward restores a frozen player, it validates the saved anchor.

A position is considered unusable when:

- Coordinates are non-finite or invalid.
- The dimension is unavailable.
- The feet position is obstructed.
- The head position is obstructed.

When the saved anchor cannot be used, Steward attempts a fallback location rather than crashing the server or leaving the player in an invalid state.

Fallback warnings may be sent to staff and written to the server console, depending on configuration.

---

## Unfreezing a Player

Unfreezing ends the active freeze and creates a completed history record.

Required permission:

```text
steward.freeze.unfreeze
```

For an offline target:

```text
steward.freeze.unfreeze.offline
```

The unfreeze action uses a confirmation step.

When the case is completed:

- The active record is removed.
- The unfreeze timestamp is recorded.
- The acting staff UUID and name are recorded.
- The event is written to the audit log.
- A completed history entry is created.
- The active-freeze data file is updated.

If the target is online, they receive an unfreeze notice immediately.

If the target is offline and offline notices are enabled, Steward queues a notice for their next login.

---

## Completed Freeze History

Completed cases are retained in freeze history.

The history record preserves:

- Freeze case ID
- Target identity
- Frozen-by identity
- Unfrozen-by identity
- Reason
- Original position
- Final freeze anchor
- Freeze timestamp
- Unfreeze timestamp
- Disconnect count
- Reconnect count
- Relocation history

Required permission:

```text
steward.freeze.history
```

History file:

```text
steward/history/freeze-history.json
```

This path is relative to the server working directory.

---

## Active Freeze Persistence

Active freezes are saved to:

```text
steward/data/active-freezes.json
```

This file preserves active cases across clean server restarts.

When Steward starts:

- Active freeze records are loaded.
- Duplicate case IDs are rejected.
- Duplicate active cases for the same target are rejected.
- Invalid entries are logged and skipped when possible.
- Valid entries continue loading.

The save process uses a temporary file before replacing the final JSON file, reducing the chance of leaving a partially written active-freeze file.

---

## Unique Case IDs

Every freeze receives a UUID-based case ID.

Example:

```text
a18d52f3-7cf3-4ea1-a573-4f0263d7b4c1
```

The full ID is stored with the case.

Audit messages use the first eight characters for readability:

```text
[Freeze:a18d52f3]
```

The same ID follows the case through:

- Initial freeze
- Disconnects
- Reconnects
- Relocations
- Unfreeze
- Completed history

This makes it easier to connect console messages to a specific case.

---

## Audit Logging

Steward writes major freeze events to the server log.

Logged events include:

- Player frozen
- Frozen player disconnected
- Frozen player reconnected
- Frozen player relocated
- Player unfrozen

Relocation logs include:

- Original freeze location
- Previous anchor
- New anchor
- Acting staff member
- Relocation note

Audit logs are not a replacement for a dedicated external moderation database, but they provide a readable server-side record of case activity.

---

## Staff Hierarchy Protection

Steward checks staff hierarchy before allowing one staff member to act on another player.

The intended behavior is:

- Staff may act on regular members.
- Staff may not act on equal-ranked staff.
- Staff may not act on higher-ranked staff.
- Higher-ranked staff may act on lower-ranked staff.
- Authorized owners or senior administrators may bypass hierarchy.

Bypass permission:

```text
steward.freeze.bypass-hierarchy
```

This permission should be granted sparingly.

Hierarchy checks apply to important freeze actions, including freeze, unfreeze, offline unfreeze, and relocation.

---

## Permission Reference

### General Steward permissions

```text
steward.staff.open
steward.status.view
```

### Freeze permissions

```text
steward.freeze.use
steward.freeze.view
steward.freeze.unfreeze
steward.freeze.unfreeze.offline
steward.freeze.history
steward.freeze.manage
steward.freeze.relocate
steward.freeze.alerts
steward.freeze.bypass-hierarchy
```

### Permission descriptions

| Permission | Purpose |
|---|---|
| `steward.staff.open` | Opens the Steward staff interface. |
| `steward.status.view` | Views `/steward status`. |
| `steward.freeze.use` | Allows freezing players. |
| `steward.freeze.view` | Allows viewing active freeze records. |
| `steward.freeze.unfreeze` | Allows unfreezing an online player. |
| `steward.freeze.unfreeze.offline` | Allows unfreezing an offline player. |
| `steward.freeze.history` | Allows viewing completed freeze history. |
| `steward.freeze.manage` | Grants access to freeze-management functionality where required. |
| `steward.freeze.relocate` | Allows confirmed relocation of an online frozen player. |
| `steward.freeze.alerts` | Receives configured freeze disconnect, reconnect, or fallback alerts. |
| `steward.freeze.bypass-hierarchy` | Bypasses normal staff hierarchy restrictions. |

---

## Suggested LuckPerms Setup

Example staff grants:

```text
/lp group staff permission set steward.staff.open true
/lp group staff permission set steward.status.view true
/lp group staff permission set steward.freeze.use true
/lp group staff permission set steward.freeze.view true
/lp group staff permission set steward.freeze.unfreeze true
/lp group staff permission set steward.freeze.unfreeze.offline true
/lp group staff permission set steward.freeze.history true
/lp group staff permission set steward.freeze.manage true
/lp group staff permission set steward.freeze.relocate true
/lp group staff permission set steward.freeze.alerts true
```

Example owner-only hierarchy bypass:

```text
/lp group owner permission set steward.freeze.bypass-hierarchy true
```

Do not grant Steward moderation permissions to the default member group unless that access is intentional.

---

## Freeze Policy Configuration

File:

```text
config/steward/freeze-policy.json
```

Default settings:

```json
{
  "disconnectAlerts": true,
  "reconnectAlerts": true,
  "showReasonOnReconnect": true,
  "offlineUnfreezeNotices": true,
  "fallbackStaffAlerts": true,
  "fallbackConsoleWarnings": true,
  "preventDamageWhileFrozen": true,
  "extinguishFrozenPlayers": true,
  "restoreAirWhileFrozen": true,
  "allowedCommands": [
    "msg",
    "tell",
    "w",
    "reply",
    "r",
    "staff",
    "steward"
  ],
  "blockedCommandMessage": "You cannot use that command while frozen."
}
```

### Setting descriptions

| Setting | Description |
|---|---|
| `disconnectAlerts` | Notifies authorized staff when a frozen player disconnects. |
| `reconnectAlerts` | Notifies authorized staff when a frozen player reconnects. |
| `showReasonOnReconnect` | Shows the player their freeze reason when reconnecting. |
| `offlineUnfreezeNotices` | Queues a notice when a player is unfrozen while offline. |
| `fallbackStaffAlerts` | Sends staff details when Steward must use a fallback location. |
| `fallbackConsoleWarnings` | Writes fallback-location warnings to the console. |
| `preventDamageWhileFrozen` | Prevents incoming damage while frozen. |
| `extinguishFrozenPlayers` | Clears fire from frozen players. |
| `restoreAirWhileFrozen` | Restores air to prevent drowning. |
| `allowedCommands` | Root commands a frozen player may execute. |
| `blockedCommandMessage` | Message shown when a blocked command is attempted. |

The configuration is loaded when Steward starts. If the file is missing, Steward creates the default file. If loading fails, Steward uses safe default settings and writes an error to the log.

---

## Staff Operating Procedure

### Freezing a player

1. Open `/steward`.
2. Open the player browser.
3. Select the target.
4. Review the player's profile.
5. Choose Freeze.
6. Enter or confirm a clear reason.
7. Verify that the player appears in Active Freezes.
8. Communicate with the player.
9. Document any additional moderation action separately if needed.

### Relocating a frozen player

1. Move to a safe, unobstructed location.
2. Open Active Freezes.
3. Open the target's freeze details.
4. Select **Bring Player Here**.
5. Review the original location, current anchor, and destination.
6. Confirm the relocation.
7. Verify that the target remains frozen.
8. Confirm that the current anchor changed but the original position did not.

### Unfreezing a player

1. Open Active Freezes.
2. Select the target.
3. Select Unfreeze.
4. Review the confirmation screen.
5. Confirm the action.
6. Verify that the active case is removed.
7. Verify that the completed record appears in Freeze History.

---

## Important Operational Notes

- Freeze is a temporary moderation control, not a punishment by itself.
- Staff should always record a clear, factual reason.
- Relocation changes the player's current controlled location but does not erase the original freeze location.
- Offline unfreezing should be used only after staff confirm the correct active case.
- The hierarchy bypass permission should be limited to the owner or highest trusted administrators.
- JSON data files should not be edited while the server is running.
- Back up Steward data before manually modifying or migrating records.
- A malformed JSON file may prevent records from loading correctly.
- Console logs should be retained according to the server's moderation-record policy.

---

## Troubleshooting

### The player is not appearing in Active Freezes

Check:

- The freeze action actually completed.
- The acting staff member has `steward.freeze.use`.
- The target was not already frozen.
- Staff hierarchy allowed the action.
- `steward/data/active-freezes.json` is writable.
- The server log does not contain a save or serialization error.

### The player can run a command while frozen

Check:

```text
config/steward/freeze-policy.json
```

The root command may be listed under `allowedCommands`.

Remove only the root name, without a slash, then restart or use the applicable configuration reload workflow if one is later implemented.

### The player returns to the wrong position

Check the active record for:

```text
originalPosition
currentPosition
relocations
```

Steward restores the player to `currentPosition`, not necessarily the original position.

### Relocation is rejected

The destination may be:

- Obstructed at the feet
- Obstructed at the head
- In an unavailable dimension
- Invalid
- Blocked by hierarchy
- Blocked by missing permission

The target must also be online.

### The player remains frozen after restart

This is expected when an active freeze record exists. Active freezes are intentionally persistent.

### The player was unfrozen while offline but received no notice

Check:

```json
"offlineUnfreezeNotices": true
```

Also review the server log and pending-notification data used by the installed build.

---

## Module Status

At the time of this documentation:

```text
Staff Interface: Active
Freeze: Active
Warnings: Not Implemented
Mute: Not Implemented
Reports: Not Implemented
Punishments: Not Implemented
```

---

## Project Information

**Project:** Steward  
**Module:** Freeze  
**Developer:** SwornHero  
**Website:** https://www.swornhero.com  
**Source:** https://github.com/golso4243/Steward

Steward is a GUI-first staff and moderation suite for Fabric servers.
