# Steward Hierarchy and Teleportation

## Hierarchy policy

Steward uses LuckPerms primary-group weights for target-affecting staff
actions. The acting staff member must have a strictly greater weight than the
target. Equal weights are denied.

Every action family selects its own bypass permission:

| Action family | Bypass permission |
|---|---|
| Freeze and freeze relocation | `steward.freeze.bypass-hierarchy` |
| Warning issue, revoke, and escalate | `steward.warning.bypass-hierarchy` |
| Punishment issue and revoke | `steward.punishment.bypass-hierarchy` |
| Bring Player Here | `steward.teleport.bypass-hierarchy` |
| Inventory inspection | `steward.inspection.bypass-hierarchy` |

A bypass skips only rank comparison. It never permits a staff member to target
themselves. Actions fail closed when Steward cannot resolve the required
LuckPerms users or primary groups.

Recommended owner-only grants:

```text
/lp group owner permission set steward.freeze.bypass-hierarchy true
/lp group owner permission set steward.warning.bypass-hierarchy true
/lp group owner permission set steward.punishment.bypass-hierarchy true
/lp group owner permission set steward.teleport.bypass-hierarchy true
/lp group owner permission set steward.inspection.bypass-hierarchy true
```

Do not grant one family bypass as a substitute for another. For example, a
freeze bypass does not authorize Bring Player Here.

## Teleport permissions

| Permission | Purpose |
|---|---|
| `steward.teleport.use` | Teleport the acting staff member to a selected online player. |
| `steward.teleport.others` | Bring a selected online player to the acting staff member. |
| `steward.teleport.bypass-hierarchy` | Bypass rank comparison for Bring Player Here. |

Teleport To does not compare target hierarchy because it moves only the actor.
It still rejects selecting oneself.

Bring Player Here moves the target and therefore enforces hierarchy at the
execution boundary. It supports cross-dimension movement. If the target is
frozen, Steward uses the freeze relocation service so the new position is
audited and freeze enforcement remains active.

## Access paths

Teleport Tools is available from:

- The main Staff Control interface
- The Staff Mode Teleport Tools hotbar item
- The player profile Teleport To and Bring Here actions

Each path returns to its originating player browser or profile when Back is
selected.
