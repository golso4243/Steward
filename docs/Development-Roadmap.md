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
| 5 | Vanish | Active |
| 6 | Staff chat | Planned |
| 7 | Reports and persistent player staff notes | Planned |
| 8 | Tests, documentation, metadata, and template cleanup | Planned |

## Current baseline

- Branch: `v26.2`
- Previous audit baseline: `7137e337c4aa24e53935a9e9b02e0058b959fc0a`
- Current implementation baseline before this update:
  `503b01ea90a7953ac9b06b74dabe74b728fac116`
- Latest baseline GitHub Actions result: successful
- Automated test suite: not yet present

## Item 1: Hierarchy enforcement

Status: `Implemented`; consolidated runtime gate pending.

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

## Items 5-7

These items currently have control-panel or player-profile placeholders but no
complete domain service:

5. Vanish: visibility, join/leave behavior, persistence, and staff awareness.
6. Staff chat: permissioned channel, toggle/send behavior, and formatting.
7. Reports and persistent player staff notes: independent persistent records,
   history integration, permissions, and profile workflows.

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

Run once after item 4 implementation is build-clean:

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

## Next action

Implement vanish visibility, connection handling, persistence, staff awareness,
and permission boundaries. Keep roadmap items 1-4 in one deferred runtime gate.
