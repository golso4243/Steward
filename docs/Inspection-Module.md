# Inventory Inspection

Steward's inspection module gives authorized staff a read-only snapshot of an
online player's inventory. It is designed for observation only: inspected item
stacks are copied into the interface and no slot in either displayed inventory
can be moved, swapped, dropped, or shift-clicked.

## Entry points

- Staff control panel: select `Inspection`, then choose an online player.
- Staff Mode: use the `Inspection` spyglass, then choose an online player.
- Player profile: select `Inspect Inventory`.

Returning from an inspection preserves the originating player-browser page or
returns directly to the player's profile. `Refresh Snapshot` re-reads the
online player's inventory after rechecking permission and hierarchy.

## Displayed slots

The six-row inspection view shows:

- Helmet, chestplate, leggings, boots, and offhand in the top row.
- The 27-slot main inventory in the middle three rows.
- The nine-slot hotbar in the fifth row.
- Refresh, back, target information, and close controls in the final row.

Empty equipment and offhand slots use clearly named gray placeholders. Main
inventory and hotbar empty slots remain empty.

The view is a point-in-time snapshot. It changes only when staff select
`Refresh Snapshot`; it does not hold or mutate the target's live container.

## Permissions and hierarchy

| Permission | Purpose |
|---|---|
| `steward.inspection.view` | Open and refresh inventory inspections. |
| `steward.inspection.bypass-hierarchy` | Bypass rank comparison for inspection; self-inspection remains denied. |

Inspection uses the same LuckPerms group-weight comparison as other Steward
staff actions. Without the bypass, the acting staff member must have a strictly
higher primary-group weight than the target. Missing hierarchy data denies the
inspection.

Suggested LuckPerms setup:

```text
/lp group moderator permission set steward.inspection.view true
/lp group owner permission set steward.inspection.bypass-hierarchy true
```

Grant the bypass sparingly. It only affects inventory inspection and does not
grant warning, punishment, freeze, or teleport hierarchy bypasses.
