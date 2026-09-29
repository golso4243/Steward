# Staff Notes Module

## Purpose and behavior

Staff notes are persistent, staff-only observations about a player that are
independent of warnings and punishments. Notes open from the player profile
`Staff Notes` button, from the Staff Control panel `Staff Notes` button (which
opens the player browser to pick a player), and from a report's detail screen.

Each note records the player, the author, the text (up to 500 characters), and
the time it was added. Notes are stored in `steward/history/staff-notes.json`
using atomic temp-file replacement. Display IDs use the form `NTE-1A2B3C4D`,
and `/steward view note <id>` opens a note directly.

## Append-only rules

- Notes cannot be edited or deleted after they are added.
- Archiving is a soft delete. An archived note is hidden from the default notes
  list but keeps its text, author, and time, and records who archived it and
  when. The notes list has a filter to show archived notes.
- Authors can archive their own notes with `steward.notes.archive-own`.
  Anyone with `steward.notes.manage` can archive any note.
- Archiving asks for confirmation and cannot be undone from the game.

Notes are added through the command prompt: choose `Add Note`, then enter
`/steward notes input <text>`, or `/steward notes cancel-input` to go back.

## History

Notes, including archived notes, appear in each player's and the global
moderation history, and in dedicated Staff Note History views. Note text is
only visible in the note detail screen. Viewers without `steward.notes.view`
never see note entries or counts in history.

## Permissions

| Permission | Purpose | Operator fallback |
|---|---|---|
| `steward.notes.view` | View notes and note history. | Game master |
| `steward.notes.create` | Add notes. | Game master |
| `steward.notes.archive-own` | Archive notes the staff member wrote. | Game master |
| `steward.notes.manage` | Archive any note. | Game master |

## Recovery

Stop the server, back up and edit `steward/history/staff-notes.json`, then
restart. Invalid or duplicate entries are skipped with an error in the log.

## Runtime operating checklist

- Add notes from the profile, the control panel browser flow, and a report.
- Confirm a note cannot be added without `steward.notes.create` and that blank
  or over-length input is rejected.
- Archive an own note with `notes.archive-own`, confirm another staff member's
  note cannot be archived without `notes.manage`, and that `notes.manage` can.
- Confirm archived notes are hidden by default, shown by the filter, and
  remain in history.
- Restart the server and confirm notes and archive state persist.
- Confirm note history and counts are hidden without `steward.notes.view`.
