# Steward Warning Module

## Overview

Warnings are persistent moderation records with a severity level, category,
reason, expiration, issuing staff identity, optional private staff notes, and
an optional evidence reference.

## Workflow

1. Open a player's profile and select Warn.
2. Select the level, category, reason, and expiration.
3. Optionally add staff notes or an evidence reference.
4. Review and confirm the warning.
5. The target may acknowledge the active warning using the clickable action in
   their notification.
6. Authorized staff may later revoke or escalate the warning from its details.

Free-form notes and evidence use a command-backed input prompt because Steward
uses server-driven inventory interfaces. After selecting the relevant button,
enter:

```text
/steward warning input <text>
```

To return without changing the field:

```text
/steward warning cancel-input
```

Staff notes are limited to 500 characters. Evidence references are limited to
300 characters. Both are stored in the warning record and are visible only in
staff warning-detail interfaces.

## Lifecycle

| Status | Meaning |
|---|---|
| Active | Contributes warning points until expiration or staff action. |
| Expired | Reached its configured expiration. |
| Revoked | Ended by authorized staff with a recorded reason. |
| Escalated | Marked for further moderation action by authorized staff. |

A target may acknowledge only their own active warning. Acknowledgment records
the time but does not change the warning's active status or point value.

Revocation and escalation are target-affecting moderation actions. Both use
`steward.warning.bypass-hierarchy`, reject self-targeting even with a bypass,
and support offline hierarchy resolution.

## Permissions

| Permission | Purpose |
|---|---|
| `steward.warning.issue` | Issue warnings and enter warning notes/evidence. |
| `steward.warning.view` | View warning records. |
| `steward.warning.history` | Reserved for dedicated warning-history policy. |
| `steward.warning.revoke` | Revoke an active warning. |
| `steward.warning.manage` | Escalate an active warning and access management operations. |
| `steward.warning.alerts` | Reserved for warning staff-alert delivery. |
| `steward.warning.bypass-hierarchy` | Bypass warning rank comparison, except self-targeting. |

The target-only acknowledgment command does not require a staff permission:

```text
/steward warning acknowledge WRN-XXXXXXXX
```

Steward verifies that the command sender owns the warning and that the warning
is still active.

## Persistence

Warning records are stored under Steward's server data directory and restored
on startup. Status, acknowledgment, revocation, escalation, staff notes, and
evidence references are preserved. Expiration is refreshed when records are
restored or queried.
