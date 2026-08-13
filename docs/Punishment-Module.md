# Steward Punishment Module Documentation

## Overview

The **Punishment module** provides Steward's GUI-first disciplinary workflow for actions that directly restrict, remove, or prevent a player's access to server features.

The module currently supports four punishment types:

- Mute
- Kick
- Temporary Ban
- Permanent Ban

Punishments are issued through Steward's moderation GUI, persisted to disk, exposed through dedicated history interfaces, integrated into unified moderation history, and protected by Steward's permission and hierarchy systems.

The module is designed so that punishment records remain useful after the immediate action is complete. Each punishment receives a persistent ID, retains its target and issuing staff identity, stores reason and timing information, tracks status, and can be reviewed later through Steward's history interfaces.

The system also supports:

- Permission-based punishment actions
- Staff hierarchy protection
- Active punishment lookup
- Punishment history
- Status transitions
- Automatic expiration
- Punishment revocation
- Persistent punishment storage
- Mute enforcement
- Temporary and permanent ban enforcement
- Server-restart restoration
- Player-specific punishment history
- Global punishment history
- Unified moderation-history integration
- Clickable punishment IDs in staff chat
- Direct punishment-record opening by ID

---

## Primary Staff Commands

### `/steward`

Opens the Steward staff interface.

This is the primary entry point for GUI-based moderation.

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

Displays Steward's core status information.

Required permission:

```text
steward.status.view
```

### `/steward view punishment <id>`

Opens a punishment record directly by its Steward display ID.

Example:

```text
/steward view punishment PUN-A1B2C3D4
```

The command resolves the short display ID to the stored punishment record and opens the punishment detail interface.

Access is allowed when the staff member has the appropriate punishment-view or unified-history permission.

Relevant permissions:

```text
steward.punishment.view
steward.history.view
```

This command is also used internally by clickable punishment IDs shown to staff.

---

## Punishment Workflow

Steward's punishment system is primarily GUI-driven.

A typical workflow is:

1. Run `/steward` or `/staff`.
2. Open the player browser.
3. Select the target player.
4. Open the player's moderation profile.
5. Choose **Punish Player**.
6. Select the punishment type.
7. Select or confirm the punishment reason.
8. Select a duration when the punishment type requires one.
9. Review the confirmation screen.
10. Confirm the punishment.
11. Review the resulting punishment record through the player's profile, punishment history, global history, or clickable punishment ID.

This approach keeps punishment actions consistent and reduces command-entry mistakes.

---

## Supported Punishment Types

### Mute

A mute prevents the player from sending normal chat messages while the punishment remains active.

Mute is an active punishment and supports expiration.

Required permission:

```text
steward.punishment.mute
```

When a muted player attempts to chat, Steward blocks the message and tells the player that they are currently muted.

The notice can include:

- Punishment reason
- Remaining duration
- Punishment ID

Mute enforcement is restored from persisted punishment data after a server restart.

---

### Kick

A kick immediately disconnects the target from the server.

Kick is treated as a completed punishment rather than a continuing active restriction.

Required permission:

```text
steward.punishment.kick
```

The disconnect message includes the punishment reason and punishment ID.

Because Kick is not an ongoing punishment, it does not have an expiration time and does not remain actively enforced after the initial disconnect.

The punishment record itself remains stored in history.

---

### Temporary Ban

A temporary ban disconnects the player and prevents them from remaining connected while the ban is active.

Temporary Ban is an active punishment and supports expiration.

Required permission:

```text
steward.punishment.temporary-ban
```

When the punishment is issued:

- The punishment record is persisted.
- The target is disconnected.
- The disconnect notice includes the reason.
- The selected duration is recorded.
- The punishment ID is shown.

When the banned player later joins, Steward checks for an active temporary ban and disconnects the player if one is still enforced.

The reconnect-time ban message can include:

- Ban type
- Reason
- Remaining duration
- Punishment ID

Once its expiration time is reached, the punishment transitions out of the active state.

---

### Permanent Ban

A permanent ban disconnects the player and prevents continued access until the punishment is revoked.

Permanent Ban is an active punishment but does not expire automatically.

Required permission:

```text
steward.punishment.permanent-ban
```

When a permanently banned player joins, Steward checks for an active permanent ban and disconnects the player.

Permanent bans are checked before temporary bans when Steward determines which active ban should be enforced.

The ban message includes:

- Permanent-ban notice
- Reason
- Punishment ID

A permanent ban remains active until it is revoked.

---

## Punishment Durations

Steward currently defines the following punishment duration options:

```text
1 Hour
6 Hours
1 Day
3 Days
7 Days
14 Days
30 Days
Permanent
```

Temporary punishment types require a non-permanent duration.

The currently implemented duration model is used by punishment types that support expiration, including:

- Mute
- Temporary Ban

Kick does not use a duration.

Permanent Ban does not use a temporary duration.

---

## Punishment Records

When Steward creates a punishment, the record stores core moderation information including:

- Unique punishment UUID
- Punishment type
- Target UUID
- Target name
- Issuing staff UUID
- Issuing staff name
- Reason
- Optional staff notes
- Optional evidence reference
- Issue timestamp
- Expiration timestamp when applicable
- Whether the target was online when issued
- Punishment status
- Revocation information when applicable

The record remains available after the immediate punishment action is complete.

---

## Punishment Status

Punishment records can use the following statuses:

```text
Active
Expired
Revoked
Completed
```

### Active

The punishment is currently enforced.

Examples include:

- Active Mute
- Active Temporary Ban
- Active Permanent Ban

### Expired

The punishment reached its expiration time.

This applies to punishment types that support expiration.

### Revoked

An authorized staff member manually ended the punishment.

### Completed

The punishment represents an action that completed immediately rather than remaining continuously enforced.

Kick is the main example.

Only `Active` punishment records are treated as currently enforced.

---

## Automatic Expiration

Steward refreshes expiration status when punishment records are accessed or persisted.

When a temporary punishment passes its expiration time:

- Its status is refreshed.
- It no longer appears as an active punishment.
- Enforcement services stop treating it as active.
- The updated state is saved.

This allows temporary mutes and temporary bans to end automatically without requiring staff to manually revoke them.

---

## Mute Enforcement

Steward registers a server chat-message check for mute enforcement.

When a player sends a chat message:

1. Steward checks the player's active punishments.
2. Steward searches for an active `MUTE` punishment.
3. If no active mute exists, the message is allowed.
4. If an active mute exists, the chat message is rejected.
5. The player receives a system message explaining the mute.

A typical mute notice contains:

```text
You are currently muted. Reason: <reason>. Remaining: <time>. Punishment ID: PUN-XXXXXXXX
```

The exact remaining-time format depends on the duration.

Examples:

```text
2d 4h
3h 20m
45m
```

---

## Ban Enforcement

Steward registers a player-join check for ban enforcement.

When a player joins:

1. Steward checks for an active Permanent Ban.
2. If none exists, Steward checks for an active Temporary Ban.
3. If no active ban exists, the player remains connected.
4. If an active ban exists, the player is disconnected.

Permanent bans are prioritized before temporary bans.

A temporary-ban enforcement notice can contain:

```text
You are temporarily banned from this server.

Reason: <reason>
Remaining: <time>
Punishment ID: PUN-XXXXXXXX
```

A permanent-ban enforcement notice can contain:

```text
You are permanently banned from this server.

Reason: <reason>
Punishment ID: PUN-XXXXXXXX
```

---

## Punishment Revocation

Authorized staff can revoke punishments through Steward's punishment-management workflow.

Required permission:

```text
steward.punishment.revoke
```

When a punishment is revoked, Steward records:

- Punishment ID
- Acting staff UUID
- Acting staff name
- Revocation time
- Revocation reason

The punishment status becomes:

```text
Revoked
```

A revoked punishment is no longer enforced.

This is especially important for:

- Active Mutes
- Temporary Bans
- Permanent Bans

---

## Active Punishment Management

Steward can distinguish all punishment history from currently active punishments.

The punishment service supports:

- All punishments
- Punishments for a specific player
- All active punishments
- Active punishments for a specific player
- Active punishments for a specific player and punishment type
- Lifetime punishment counts
- Active punishment counts

This allows the GUI and enforcement services to use the same persisted punishment records.

---

## Punishment History

Punishment records are retained after issuance.

Staff can review punishment history through dedicated Steward interfaces.

Punishment history can include:

- Punishment ID
- Player
- Punishment type
- Status
- Reason
- Issued-by staff member
- Issue time
- Expiration
- Revocation state
- Other stored punishment metadata

Required permission for the dedicated punishment-history system:

```text
steward.punishment.view
```

The punishment module also integrates with Steward's unified history system.

---

## Unified Player Moderation History

Punishments are included alongside other Steward moderation actions in a player's unified moderation history.

The unified history can contain actions such as:

- Warning
- Freeze
- Mute
- Kick
- Temporary Ban
- Permanent Ban

Required permission:

```text
steward.history.view
```

This gives staff one chronological view of a player's moderation activity rather than requiring them to open each module separately.

---

## Global Moderation History

Punishment records are also integrated into Steward's global moderation history.

Staff can review moderation activity across players and filter or navigate into punishment-related records through the global history system.

Punishment detail screens opened from global or player history retain source-aware navigation so the Back button returns staff to the appropriate originating view.

---

## Dedicated Punishment History

Steward also provides a punishment-specific history view.

This view is useful when staff want to focus specifically on punishment records without unrelated warning or freeze activity.

The punishment detail screen can be opened from:

- Dedicated Punishment History
- Player moderation history
- Global moderation history
- Direct `/steward view punishment <id>` command
- Clickable punishment IDs in staff chat

The detail screen adjusts its Back navigation according to the source.

---

## Unique Punishment IDs

Every punishment receives a UUID-based internal identifier.

For staff-facing readability, Steward formats punishment IDs using the first eight hexadecimal characters of the UUID.

Format:

```text
PUN-XXXXXXXX
```

Example:

```text
PUN-A1B2C3D4
```

The full UUID remains part of the stored punishment record.

The display ID is used for:

- Staff confirmation messages
- Punishment detail screens
- History views
- Enforcement messages
- Direct punishment lookup
- Clickable staff-chat links

Steward can resolve a short `PUN-XXXXXXXX` display ID back to its stored punishment record.

If a short display ID would be ambiguous, lookup fails rather than opening the wrong punishment.

---

## Clickable Punishment IDs

Staff-facing punishment confirmation messages use clickable punishment IDs.

Examples include punishment confirmation messages for:

- Mute
- Kick
- Temporary Ban
- Permanent Ban

The clickable text appears using Steward's shared clickable-record component.

Clicking a punishment ID runs:

```text
/steward view punishment PUN-XXXXXXXX
```

Hovering the ID displays a punishment-detail hint.

The command validates:

- Staff access
- Punishment lookup
- Record availability

When successful, the existing Punishment History Detail interface opens.

When opened directly from chat, the detail screen uses the Staff Menu as its return target.

---

## Punishment Persistence

Punishment history is saved to:

```text
steward/history/punishments.json
```

This path is relative to the server working directory.

The file contains persisted punishment entries representing Steward's punishment records.

When Steward starts:

- The punishment file is loaded.
- Null or invalid entries are skipped when possible.
- Duplicate punishment IDs are rejected.
- Valid punishment records are restored.
- Expiration status is refreshed.
- Updated records are saved when necessary.

---

## Safe File Replacement

Punishment persistence uses a temporary file:

```text
steward/history/punishments.json.tmp
```

Steward first writes the complete updated punishment list to the temporary file.

It then replaces:

```text
steward/history/punishments.json
```

using an atomic move when the filesystem supports it.

If atomic replacement is unavailable, Steward falls back to a normal replacement move.

This reduces the chance of leaving a partially written punishment-history file.

---

## Restart Behavior

Punishment records are restored when Steward starts.

Because enforcement services use the restored punishment service:

- Active Mutes continue to block chat after restart.
- Active Temporary Bans continue to disconnect players after restart.
- Active Permanent Bans continue to disconnect players after restart.
- Expired temporary punishments stop being enforced after their status is refreshed.
- Revoked punishments remain inactive.
- Completed punishments remain available as history.

This allows punishment enforcement to survive clean and unclean restart scenarios as long as the persisted punishment data remains valid.

---

## Concurrency and Record Safety

Punishment data is protected by a service-level synchronization lock.

Operations that access or modify the punishment map use synchronized sections where appropriate.

This includes operations such as:

- Punishment restoration
- Punishment creation
- ID lookup
- Display-ID lookup
- Revocation
- Listing punishments
- Active-punishment queries
- Expiration refresh
- Persistence

The service also checks for duplicate punishment IDs during restoration and creation.

This reduces the chance of duplicate records or inconsistent writes when multiple moderation operations occur near the same time.

---

## Staff Hierarchy Protection

Punishment actions integrate with Steward's staff hierarchy system.

Punishment issue and revocation explicitly use the Punishment-family bypass.
Freeze, warning, and teleport bypass permissions do not authorize punishment
actions. See [Hierarchy and Teleportation](Hierarchy-and-Teleport.md) for the
shared rank policy.

The intended behavior is:

- Staff may act on normal members.
- Staff may not act on equal-ranked staff.
- Staff may not act on higher-ranked staff.
- Higher-ranked staff may act on lower-ranked staff.
- Authorized senior staff may bypass hierarchy restrictions.

Punishment hierarchy bypass permission:

```text
steward.punishment.bypass-hierarchy
```

This permission should be granted only to trusted senior staff.

---

## Permission Reference

### General Steward permissions

```text
steward.staff.open
steward.status.view
steward.history.view
```

### Punishment permissions

```text
steward.punishment.view
steward.punishment.manage
steward.punishment.mute
steward.punishment.kick
steward.punishment.temporary-ban
steward.punishment.permanent-ban
steward.punishment.revoke
steward.punishment.bypass-hierarchy
```

### Permission descriptions

| Permission | Purpose |
|---|---|
| `steward.staff.open` | Opens the Steward staff interface. |
| `steward.status.view` | Views `/steward status`. |
| `steward.history.view` | Views Steward's unified moderation history where applicable. |
| `steward.punishment.view` | Views punishment records and punishment-history interfaces. |
| `steward.punishment.manage` | Grants access to punishment-management functionality where required. |
| `steward.punishment.mute` | Allows issuing Mute punishments. |
| `steward.punishment.kick` | Allows issuing Kick punishments. |
| `steward.punishment.temporary-ban` | Allows issuing Temporary Ban punishments. |
| `steward.punishment.permanent-ban` | Allows issuing Permanent Ban punishments. |
| `steward.punishment.revoke` | Allows revoking eligible punishments. |
| `steward.punishment.bypass-hierarchy` | Bypasses normal punishment hierarchy restrictions. |

---

## Suggested LuckPerms Setup

Example staff grants:

```text
/lp group staff permission set steward.staff.open true
/lp group staff permission set steward.status.view true
/lp group staff permission set steward.history.view true
/lp group staff permission set steward.punishment.view true
/lp group staff permission set steward.punishment.manage true
/lp group staff permission set steward.punishment.mute true
/lp group staff permission set steward.punishment.kick true
/lp group staff permission set steward.punishment.temporary-ban true
/lp group staff permission set steward.punishment.permanent-ban true
/lp group staff permission set steward.punishment.revoke true
```

Example owner-only hierarchy bypass:

```text
/lp group owner permission set steward.punishment.bypass-hierarchy true
```

Do not grant Steward punishment permissions to the default member group unless that access is intentional.

---

## Staff Operating Procedure

### Muting a player

1. Open `/steward`.
2. Open the player browser.
3. Select the player.
4. Open the player's moderation profile.
5. Choose **Punish Player**.
6. Select **Mute**.
7. Select a factual reason.
8. Select the required duration.
9. Review the confirmation screen.
10. Confirm the punishment.
11. Verify the `PUN-XXXXXXXX` record was created.
12. Verify the player cannot send normal chat messages.
13. Review the punishment record through history when needed.

### Kicking a player

1. Open `/steward`.
2. Select the target through the player browser.
3. Choose **Punish Player**.
4. Select **Kick**.
5. Select the reason.
6. Review the confirmation screen.
7. Confirm the punishment.
8. Verify the player disconnects.
9. Verify the completed punishment record appears in history.

### Temporarily banning a player

1. Open `/steward`.
2. Select the player.
3. Choose **Punish Player**.
4. Select **Temporary Ban**.
5. Select the reason.
6. Select the duration.
7. Review the confirmation screen.
8. Confirm the punishment.
9. Verify the player disconnects.
10. Verify reconnect attempts are rejected while the ban remains active.
11. Verify the record transitions out of Active after expiration.

### Permanently banning a player

1. Open `/steward`.
2. Select the player.
3. Choose **Punish Player**.
4. Select **Permanent Ban**.
5. Select the reason.
6. Review the confirmation screen.
7. Confirm the punishment.
8. Verify the player disconnects.
9. Verify reconnect attempts are rejected.
10. Retain the punishment until an authorized staff member revokes it.

### Revoking a punishment

1. Open the relevant punishment record.
2. Choose the revocation workflow.
3. Enter or select the revocation reason.
4. Review the confirmation screen.
5. Confirm revocation.
6. Verify the status becomes **Revoked**.
7. Verify the punishment is no longer enforced.

---

## Important Operational Notes

- Punishments are disciplinary actions and should use clear, factual reasons.
- A Kick is recorded even though it is not an ongoing punishment.
- Temporary punishments expire automatically according to their recorded expiration time.
- Permanent Bans remain active until revoked.
- Revoked punishments remain in history rather than being deleted.
- Punishment history should be treated as moderation data.
- JSON punishment data should not be manually edited while the server is running.
- Back up Steward data before manual migration or modification.
- A malformed punishment-history file may prevent individual records from loading.
- Staff hierarchy bypass should be limited to the highest trusted roles.
- Punishment IDs should be included when referencing a specific case in staff discussions.

---

## Troubleshooting

### A muted player can still chat

Check:

- The mute record exists.
- The punishment status is `Active`.
- The punishment has not expired.
- The target UUID matches the muted player.
- The punishment service loaded successfully at startup.
- Mute enforcement registered successfully.

### A banned player can reconnect

Check:

- The punishment record exists.
- The type is `Temporary Ban` or `Permanent Ban`.
- The punishment status is `Active`.
- A temporary ban has not expired.
- The target UUID matches the connecting player.
- Ban enforcement registered successfully.
- The server log does not show punishment-data restoration errors.

### A temporary punishment ended unexpectedly

Review:

- `issuedAt`
- `expiresAt`
- punishment duration
- current punishment status

Temporary punishment expiration is refreshed using the current server time.

### A revoked punishment is still enforced

Check:

- The revocation completed successfully.
- The punishment status is `Revoked`.
- The correct punishment ID was revoked.
- Persisted punishment data was saved.
- The server was not running from stale or manually modified data.

### A punishment record is missing after restart

Check:

```text
steward/history/punishments.json
```

Also review the server log for:

- JSON parsing errors
- Invalid punishment entries
- Duplicate punishment IDs
- File-write errors

### A clickable punishment ID does not open

Check:

- The ID is formatted as `PUN-XXXXXXXX`.
- The record still exists.
- The staff member has `steward.punishment.view` or the applicable unified-history permission.
- `/steward view punishment <id>` works when entered manually.
- The short ID is not ambiguous.

---

## Module Integration

The Punishment module is integrated with Steward's broader moderation suite.

Current integrations include:

```text
Staff Interface: Active
Player Browser: Active
Player Profiles: Active
Freeze: Active
Warnings: Active
Punishments: Active
Unified Player History: Active
Global Moderation History: Active
Clickable Record IDs: Active
```

Punishment actions included in moderation history:

```text
Mute
Kick
Temporary Ban
Permanent Ban
```

The module shares core Steward systems for:

- Permissions
- Staff hierarchy
- Player selection
- Navigation
- History routing
- Record detail screens
- Persistent moderation records

---

## Module Status

At the time of this documentation:

```text
Staff Interface: Active
Freeze: Active
Warnings: Active
Punishments: Active

Mute: Active
Kick: Active
Temporary Ban: Active
Permanent Ban: Active

Punishment Persistence: Active
Punishment Revocation: Active
Mute Enforcement: Active
Ban Enforcement: Active
Player Punishment History: Active
Global Punishment History: Active
Unified Moderation History: Active
Clickable Punishment IDs: Active
Direct Punishment Record Lookup: Active

Reports: Not Implemented
```

---

## Project Information

**Project:** Steward  
**Module:** Punishment  
**Developer:** SwornHero  
**Website:** https://www.swornhero.com  
**Source:** https://github.com/golso4243/Steward

Steward is a GUI-first staff and moderation suite for Fabric servers.
