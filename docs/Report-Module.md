# Report Module

## Purpose and behavior

Players file reports with `/report <player> <reason>`. Staff triage reports in
the Report Queue, which opens from the Staff Control panel `Reports` button, or
review the reports filed against one player from the player profile.

Each report records the reporter, the reported player, the reason (up to 256
characters), the time filed, and its lifecycle state. Reports are stored in
`steward/history/reports.json` using atomic temp-file replacement. Display IDs
use the form `RPT-1A2B3C4D`, and `/steward view report <id>` opens a report
directly.

## Submission rules

- A player cannot report themselves.
- After a successful report, a reporter must wait 60 seconds before submitting
  another, unless they hold `steward.report.bypass-cooldown`.
- A reporter may have only one open or claimed report against the same player.
- Online players are resolved directly. Offline names are resolved only
  through Steward's known-player index (`steward/data/known-players.json`),
  which records each player's UUID, latest name, and last-seen time on every
  join. Lookups are case-insensitive, never contact Mojang, and work the same
  on online-mode and offline-mode servers. A name nobody has joined with is
  rejected with "No player named X has played on this server."
- If a name has been held by more than one player, it resolves to the player
  who most recently joined with it.
- The index starts empty on first install, so players who have not joined
  since Steward was installed cannot be reported until they join once.
- Name suggestions list online players the reporter can see, which respects
  Vanish.

Staff with `steward.report.alerts` receive a notice with a clickable report ID
whenever a report is filed.

## Lifecycle

| From | Action | To | Notes |
|---|---|---|---|
| Open | Claim | Claimed | The claimer owns the report. |
| Open | Dismiss | Dismissed | Resolution note required. |
| Claimed | Release Claim | Open | Claimer or override only. |
| Claimed | Resolve | Resolved | Claimer or override; resolution note required. |
| Claimed | Dismiss | Dismissed | Claimer or override; resolution note required. |

Resolving requires a claim so that someone has owned the investigation.
Resolution notes are entered through the command prompt
(`/steward report input <text>`, or `/steward report cancel-input` to go back)
and confirmed on a final screen before the report closes. Notes may be up to
500 characters and are visible only to staff.

Staff cannot claim or close a report filed against themselves.

When a report closes, the reporter is told it was resolved or dismissed. The
resolution note is not shown to the reporter. If the reporter is offline, the
notice is delivered the next time they join, and the delivered state is saved.

## Screens

- Report Queue: open and claimed reports, oldest first. The hopper filter
  switches to every report, newest first.
- Player reports: every report against one player, newest first, with the same
  filter.
- Report detail: all fields plus the lifecycle actions the viewer may take,
  a shortcut to the reported player's profile if they are online, and to their
  staff notes if the viewer holds `steward.notes.view`.
- Moderation history: reports appear in each player's and the global
  history, and in dedicated Report History views. Viewers without
  `steward.report.view` never see report entries or counts in history.

## Permissions

| Permission | Purpose | Operator fallback |
|---|---|---|
| `steward.report.submit` | Use `/report`. | Everyone |
| `steward.report.view` | Open the queue, details, and report history. | Game master |
| `steward.report.manage` | Claim, release, resolve, and dismiss reports. | Game master |
| `steward.report.override-claim` | Release or close reports claimed by someone else. | Game master |
| `steward.report.alerts` | Receive new-report notices. | Game master |
| `steward.report.bypass-cooldown` | Skip the 60-second submission cooldown. | Game master |

`steward.report.submit` defaults to every player; a permission provider can
deny it to remove access to `/report`.

## Recovery

Stop the server, back up and edit `steward/history/reports.json`, then restart.
Invalid or duplicate entries are skipped with an error in the log.

## Runtime operating checklist

- File reports against online players and players who joined before but are
  now offline; confirm a never-joined name is rejected; confirm alerts,
  clickable IDs, and `/steward view report`.
- Confirm self-reports, the cooldown, the bypass, and duplicate open reports
  are rejected.
- Claim, release, resolve, and dismiss; confirm resolving requires a claim and
  a second staff member cannot act on another's claim without the override.
- Close a report while the reporter is online and offline; confirm the notice
  arrives exactly once.
- Restart the server and confirm reports and their states persist.
- Confirm report history and counts are hidden without `steward.report.view`.
