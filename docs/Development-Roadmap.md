# Steward Development Roadmap

## Purpose

This file is Steward's durable development status and validation log. It is
updated in the same commit as each substantial implementation increment.

## Delivery workflow

Steward development uses large, coherent increments instead of one commit per
small milestone:

1. Implement a complete, related portion of the active roadmap item.
2. Run static checks and a clean build when the local toolchain is available.
3. Commit the complete increment with its documentation.
4. Use GitHub Actions as the authoritative clean-build result after publishing.
5. Batch behavior that truly requires Minecraft into the smallest practical
   runtime test gate.

Build validation and runtime acceptance are tracked separately. An item may be
`Implemented` while waiting for its consolidated runtime gate, but it is not
`Accepted` until the required runtime cases pass.

## Status definitions

- `Planned`: scoped but not started.
- `Active`: currently being implemented.
- `Implemented`: code and documentation are committed and build validation has
  passed, but a required runtime gate remains.
- `Accepted`: all required build and runtime validation has passed.
- `Closed`: accepted work has been included in a later regression gate and no
  follow-up remains.

## Roadmap

| Item | Scope | Status |
|---|---|---|
| 1 | Hierarchy enforcement and action-specific bypass permissions | Implemented |
| 2 | Teleport integration with staff control and player profiles | Implemented |
| 3 | Warning lifecycle, warning notes, and evidence | Implemented |
| 4 | Inventory inspection | Implemented |
| 5 | Vanish | Implemented |
| 6 | Staff chat | Implemented |
| 7 | Reports and persistent player staff notes | Implemented |
| 8 | Tests, documentation, metadata, and template cleanup | Planned |

## Current baseline

- Development branch: `v26.2` (Minecraft 26.2); this is the only branch.
- Previous audit baseline: `7137e337c4aa24e53935a9e9b02e0058b959fc0a`
- Current implementation baseline before this update:
  `5031e9e9dbe42e2ee426cdb402307205e3637409`
- Latest baseline GitHub Actions result: successful (run `36628016454`)
- Automated test suite: not yet present

## Item 1: Hierarchy enforcement

Status: `Implemented`; consolidated runtime gate pending. The seven post-audit
hierarchy commits passed GitHub Actions.

Implemented:

- Callers select their own freeze, warning, punishment, or teleport bypass.
- Bypasses skip rank comparison but never permit self-targeting.
- Warning and all four punishment issuance paths enforce hierarchy at final
  confirmation.
- Punishment revocation resolves offline UUIDs asynchronously and returns GUI
  mutation to the server thread.
- Failed LuckPerms resolution denies the action.

Static audit:

- All reachable `StaffHierarchyService` call sites pass an explicit bypass
  permission.
- No action-family bypass is hard-coded in the hierarchy service.

## Item 2: Teleport integration

Status: `Implemented`; consolidated runtime gate pending.

Implemented:

- Staff-control Teleport Tools opens the player browser in teleport context.
- Player selection opens Teleport To and Bring Here actions.
- Player profiles expose the same actions and return to the correct source.
- Teleport To moves only the acting staff member and rejects self-targeting.
- Bring Here uses teleport hierarchy policy, supports cross-dimension moves,
  and routes frozen targets through audited freeze relocation.

## Item 3: Warning lifecycle, notes, and evidence

Status: `Implemented`.

Already implemented before this roadmap refresh:

- Warning issue workflow, persistence, expiration, history, summaries, and
  escalation recommendations.
- Warning hierarchy enforcement and action-specific bypass.
- Warning revocation workflow with offline hierarchy lookup.

Implemented in the warning lifecycle increment:

- Add optional staff notes and evidence references before final confirmation.
- Preserve the draft while staff enters free-form text through a restricted
  command-backed prompt.
- Add target-only warning acknowledgment.
- Expose staff escalation behind confirmation, manage permission, and warning
  hierarchy enforcement.
- Document warning permissions and operating procedures.

Remaining after this increment:

- Add warning lifecycle automated tests under roadmap item 8.
- Pass the consolidated warning runtime gate.

## Item 4: Inventory inspection

Status: `Implemented`.

Implemented in the inventory inspection increment:

- Add a read-only snapshot of an online player's main inventory, hotbar, armor,
  and offhand slots.
- Connect inspection to the control panel, Staff Mode tool, player browser, and
  player profile.
- Enforce `steward.inspection.view`, hierarchy checks, self-target rejection,
  and the action-specific `steward.inspection.bypass-hierarchy` permission.
- Preserve the originating navigation flow and allow explicit snapshot refresh.
- Document the module and its permission boundary.

Remaining after this increment:

- Pass the consolidated inspection runtime gate.
- Add automated permission and snapshot-mapping tests under roadmap item 8.

## Item 5: Vanish

Status: `Implemented`; consolidated runtime gate pending.

Implemented in the Vanish increment:

- Add a dedicated module with connection, domain, and atomic JSON persistence
  services.
- Add separate use, identify/see, and staff-awareness permissions.
- Toggle Vanish from the Staff Control panel and Staff Mode Vanish tool.
- Reapply persisted invisible state on reconnect and reconcile tab-list entries
  for both the joining viewer and already connected viewers.
- Hide unauthorized tab entries using mapped Minecraft 26.2 player-info
  packets while retaining identification for authorized staff.
- Report Vanish as active in `/steward status` and document the operational
  concealment boundary.

Implemented in the Vanish completion increment:

- Reassert invisibility at the end of every server tick, because vanilla resets
  the invisible flag whenever mob effects change (milk, effect expiry,
  `/effect clear`).
- Reapply invisibility and tab policy after death or respawn.
- Hide vanished staff from the player browser for viewers without
  `steward.vanish.see` and reject stale selections of newly vanished players.
- Recheck `steward.vanish.see` for online viewers about once per second and
  resend tab policy when it changes.

Implementation decisions and discovered limitations:

- Entity invisibility and mapped player-info packets were selected instead of
  brittle entity tracking packets or a connection/tracker mixin.
- Authorized staff can identify vanished players in tab and through separately
  permissioned awareness notices, but the entity model remains invisible.
- Vanilla entity tracking, equipment, effects, sounds, collision, and indirect
  interactions are not concealed by this safe API surface.
- The current Fabric connection events do not safely suppress targeted vanilla
  join/leave messages. Strict connection-message secrecy therefore remains an
  explicitly documented integration limitation, not a claimed behavior.
- Invisibility is reasserted by tick reconciliation instead of a mixin, so an
  effect change can expose the model for at most one tick.
- Permission changes reach the tab list within about one second, not instantly.
- Minecraft runtime validation remains required; the item is not Accepted.

## Item 6: Staff chat

Status: `Implemented`; consolidated runtime gate B pending.

Implemented in the Staff Chat increment:

- Add a `staffchat` module with a permissioned channel
  (`steward.staff-chat.use`) that delivers to online staff and the console.
- Send with `/sc <message>` or `/staffchat <message>`; toggle mode with bare
  `/sc`, the Staff Control panel, or the Staff Mode Staff Chat tool.
- Route toggled players' normal chat to staff before public broadcast. The
  listener is registered before mute enforcement so muted staff keep Staff
  Chat.
- Persist mode atomically in `steward/data/staff-chat-toggles.json`, remind on
  reconnect, and clear the mode when the permission is gone.
- Format messages with a gold tag, aqua name with a send-time hover, and a
  256-character limit.
- Document the module in `docs/StaffChat-Module.md`.

## Item 7: Reports and persistent player staff notes

Status: `Implemented`; consolidated runtime gate B pending.

Implemented in the Reports and Staff Notes increment:

- Add a `report` module: `/report <player> <reason>` for every player (online
  or cached offline targets), self-report, 60-second cooldown, and duplicate
  open-report guards, and clickable staff alerts.
- Report lifecycle Open, Claimed, Resolved, and Dismissed, with claim
  ownership, an override permission, required resolution notes through a
  command prompt, and a final confirmation screen.
- Notify reporters when their report closes, including delivery on next join.
- Report Queue from the control panel, per-player reports from the profile,
  and `/steward view report <id>`.
- Add a `notes` module: append-only staff notes with soft-delete archiving by
  the author (`notes.archive-own`) or a manager (`notes.manage`), an archived
  filter, and `/steward view note <id>`.
- Notes open from the profile, the control panel through the player browser,
  and report details.
- Persist reports and notes atomically in `steward/history/reports.json` and
  `steward/history/staff-notes.json`.
- Add Report and Staff Note entries to player and global moderation history,
  dedicated history views and hub buttons, and permission filtering so viewers
  without `report.view` or `notes.view` never see those entries or counts.
- Replace the control panel, player profile, and `/steward status`
  placeholders with live counts and Active module lines.
- Add shared infrastructure: a read-only `StewardMenu`/`ActionMenu` base,
  `MenuItems` builders, `TextPromptService`, `JsonListStorage`, and
  `RecordIds`.
- Document the modules in `docs/Report-Module.md` and
  `docs/StaffNotes-Module.md`.

Implementation decisions and discovered limitations:

- Resolving requires a claim; dismissing does not.
- Staff cannot handle reports filed against themselves.
- Resolution notes are staff-only and are not shown to the reporter.
- Offline name resolution uses the server profile cache, which can perform a
  Mojang lookup on the server thread like vanilla `/ban`. Unresolved lookups
  are throttled per reporter.
- Staff Chat is not stored as Steward history; only the console log records it.

## Item 8: Quality and release readiness

Planned work:

- Add automated service, model, persistence, and permission-boundary tests.
- Replace the Fabric template README.
- Remove `ExampleClientMixin` and the Mod Menu placeholder.
- Replace placeholder project metadata and Discord URL.
- Add Warning, Staff Mode, Teleport, Inspection, Vanish, Staff Chat, Reports,
  and Staff Notes documentation.
- Verify the Gradle wrapper executable bit.

## Consolidated runtime gate A

Run once after item 5 implementation is build-clean:

- Freeze, warning, punishment issuance, and revocation reject self-targets.
- Equal/higher staff targets are denied without the family bypass.
- Each family bypass permits a higher-ranked target but not a self-target.
- Punishment and warning revocation work for an offline lower-ranked target.
- Teleport To and Bring Here work in the same and different dimensions.
- Frozen-player Bring Here preserves freeze enforcement and audit history.
- Warning notes and evidence survive persistence and appear in details.
- Only the target can acknowledge an active warning.
- Warning escalation confirms, enforces hierarchy, and persists.
- Inspection opens from every entry point, maps main inventory, hotbar, armor,
  and offhand correctly, and refreshes without permitting item movement.
- Inspection rejects self-targets and equal or higher ranks unless the
  inspection-specific hierarchy bypass is granted.
- Vanish use is denied without `steward.vanish.use`; see and notification
  permissions do not implicitly grant use.
- Vanish toggles from both the control panel and Staff Mode tool and gives clear
  actor feedback.
- Unauthorized viewers lose the vanished tab entry; authorized viewers retain
  it and notification-only viewers receive only the configured awareness.
- A vanished model is invisible to ordinary viewers, and disabling restores
  the model and tab entry without requiring reconnect.
- Vanish survives disconnect/reconnect and a full server restart; joining
  viewers receive the correct tab policy for all already vanished staff.
- Persistence write failure leaves the prior state and visibility unchanged.
- Milk, potion expiry, and `/effect clear` do not reveal a vanished player.
- Death and respawn keep the vanished model and tab entry hidden.
- The player browser hides vanished staff from viewers without
  `steward.vanish.see`, and selecting from a browser opened before the target
  vanished is rejected.
- Granting or revoking `steward.vanish.see` while online updates the tab list
  within about one second.
- Record actual 26.2 behavior for armor, held items, particles, sounds,
  collision, commands, and vanilla join/leave messages.

## Consolidated runtime gate B

Run after items 6-7 are build-clean, together with gate A if practical:

- Staff Chat reaches only `steward.staff-chat.use` holders and the console;
  players without the permission neither send nor receive.
- Toggle mode from the command, control panel, and Staff Mode tool routes
  normal chat privately while on and publicly while off.
- A muted staff member with the mode on still reaches Staff Chat; public chat
  stays blocked after toggling off.
- Mode survives reconnect and restart, and revoking the permission clears it on
  the next chat message.
- `/report` rejects self-reports, repeats inside the cooldown (unless bypassed),
  and duplicate open reports; offline cached targets can be reported.
- New-report alerts reach `report.alerts` holders with a working clickable ID.
- Claim, release, resolve, and dismiss follow the lifecycle table; resolving
  requires a claim, other staff cannot act on a claim without
  `report.override-claim`, and staff cannot handle reports against themselves.
- Reporters are notified exactly once on close, online or at next join.
- Notes can be added from the profile, the control panel browser flow, and a
  report; archiving follows `notes.archive-own` and `notes.manage`.
- Reports, notes, claim, closure, and archive state survive a full restart.
- History shows report and note entries and counts only to viewers with
  `report.view` or `notes.view` respectively.

## Validation log

| Date | Increment | Result | Notes |
|---|---|---|---|
| 2026-08-09 | Original item 1 audit | Complete | Audited `7137e33`. |
| 2026-08-09 to 2026-08-13 | Hierarchy milestones | Build passed | Seven post-audit commits passed GitHub Actions. |
| 2026-08-13 | Teleport integration | Build passed | GitHub Actions passed at `c296e87`. |
| 2026-08-13 | Warning revocation | Build passed | GitHub Actions passed at `503b01e`. |
| 2026-08-13 | Local clean build | Environment blocked | Gradle 9.6.1 was not cached and its distribution host was unreachable. |
| 2026-08-13 | Warning lifecycle increment | Static checks passed | All 182 Java files parsed successfully; `git diff --check` passed. |
| 2026-08-13 | Warning lifecycle clean build | Environment blocked | Gradle was recovered, but the isolated build process could not reach Fabric Loom dependencies. |
| 2026-08-13 | Warning lifecycle remote build | Build passed | GitHub Actions passed at `ccdae91` in run `31746147322`. |
| 2026-08-13 | Inventory inspection static gate | Passed | All 180 main-source Java files parsed and `git diff --check` passed. |
| 2026-08-13 | Inventory inspection initial build | Failed, fixed | GitHub Actions identified an unavailable mapped `Items` constant at `4ca986e`; the placeholder now resolves through the item registry. |
| 2026-08-13 | Inventory inspection clean build | Build passed | GitHub Actions passed at `d4b8f31` in run `31746908937`. |
| 2026-08-13 | Vanish static gate | Passed | `git diff --check` passed; stable mapped APIs were selected without new mixins. |
| 2026-08-13 | Vanish local compilation | Environment blocked | Gradle 9.6.1 distribution download was rejected by the environment proxy before compilation. |
| 2026-08-19 | Vanish initial remote build | Failed, fixed | GitHub Actions runs `32290224565` and `32290267598` failed on 26.2 server accessors. |
| 2026-09-29 | Vanish 26.2 accessor fix | Build passed | GitHub Actions passed at `9741fce` in run `36624148646`. |
| 2026-09-29 | Vanish completion static gate | Passed | `git diff --check` passed; no linter errors in changed files. |
| 2026-09-29 | Vanish completion local build | Build passed | `gradlew build` succeeded locally with Fabric Loom 1.17.21. |
| 2026-09-29 | Vanish completion remote build | Build passed | GitHub Actions passed at `f29e7c4` in run `36628016454`. |
| 2026-09-29 | Staff Chat, Reports, and Staff Notes static gate | Passed | `git diff --check` passed; no linter errors in changed packages. |
| 2026-09-29 | Staff Chat, Reports, and Staff Notes local build | Build passed | `gradlew clean build` succeeded locally; the first attempt found three exhaustive `HistoryReturnTarget` label switches, now updated. |

## Next action

Run consolidated runtime gates A and B, or begin item 8 and keep items 1-7 in
the deferred runtime gates.
