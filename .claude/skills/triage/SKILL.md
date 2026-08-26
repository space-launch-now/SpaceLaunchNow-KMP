---
name: triage
description: "Fetch Firebase Crashlytics data for Space Launch Now, identify new or notable crashes, file one GitHub issue per crash with a root-cause analysis, and hand the top items to the engineering team. Use when: running the daily crash triage routine, investigating a spike, or checking what crashed in the last 24h on Android or iOS."
---

# Crash Triage

Daily sweep of Firebase Crashlytics for the Android and iOS production apps, filed as
GitHub issues against `space-launch-now/SpaceLaunchNow-KMP`.

## Firebase details

| | |
|---|---|
| Project ID | `space-launch-now` (number `610310574961`) |
| Android prod app | `1:610310574961:android:4553f5c8ecd77279` |
| iOS prod app | `1:610310574961:ios:000f05cac2dcfe10` |

## 1. Authenticate

`CRASHLYTICS_SA_KEY_B64` holds a base64 service-account JSON key with Crashlytics viewer
access. If it is unset or empty, **stop** — file nothing, and report:

> `CRASHLYTICS_SA_KEY_B64` secret is not configured in the cloud environment — add the
> base64-encoded service account JSON key as an environment variable to enable crash triage.

```bash
echo "$CRASHLYTICS_SA_KEY_B64" | base64 -d > /tmp/sa-key.json   # verify it parses as JSON
pip install --quiet google-auth
```

**Known sandbox bug:** importing `google.auth` may fail with `pyo3_runtime.PanicException`
(`No module named '_cffi_backend'`) because the image ships a broken `cryptography 41`.
Fix, then retry:

```bash
pip install --quiet --ignore-installed "cryptography>=42"
```

```python
from google.oauth2 import service_account
import google.auth.transport.requests
c = service_account.Credentials.from_service_account_file(
    '/tmp/sa-key.json', scopes=['https://www.googleapis.com/auth/cloud-platform'])
c.refresh(google.auth.transport.requests.Request())
print(c.token)
```

## 2. Fetch

Base URL `https://firebasecrashlytics.googleapis.com/v1alpha`. Send **only**
`Authorization: Bearer <token>` — adding `x-goog-user-project` causes a 403 for this
service account; the quota project is implicit.

Per app, over a 24h window ending now:

```
GET /projects/610310574961/apps/{APP_ID}/reports/topIssues
    ?pageSize=20
    &filter.interval.startTime={RFC3339}
    &filter.interval.endTime={RFC3339}
    &filter.issue.errorTypes=FATAL
```

Drop the `errorTypes` filter for the non-fatal pass. Response: `groups[]`, each with
`metrics[]` (`eventsCount`, `impactedUsersCount`) and `issue` (`id`, `title`, `subtitle`,
`errorType`, `sampleEvent`, `uri`, `firstSeenVersion`, `lastSeenVersion`, sometimes
`signals`).

Full stack trace for one issue:

```
GET /projects/610310574961/apps/{APP_ID}/events:batchGet?names={URL-ENCODED sampleEvent}
```

`events[0]` carries `exceptions[]` → `frames[]` (`file`, `line`, `symbol`, `blamed`), plus
`customKeys`, `logs`, `breadcrumbs`, and `version`. **Save responses to files and parse with
Python** — do not dump them to the terminal.

If a call fails unexpectedly, check current shapes against
https://github.com/firebase/firebase-tools/tree/master/src/crashlytics. If the API is
unreachable or auth is rejected, file nothing and report the exact HTTP status and endpoint.

## 3. Select candidates

- **Every FATAL is a candidate, even at 1 event.** Fatal volume in this app is a handful per
  day, so each one matters.
- **NON_FATAL** qualifies only when it points at app code (`me.calebjones.spacelaunchnow`
  or KMP shared code) **and** impacted ≥50 users in 24h.
- Rank by `impactedUsersCount`, then `eventsCount`. **File at most 5 per run**; name any you
  cut in the summary.
- Compare `lastSeenVersion` against `version.properties` (the source of truth for the current
  release). An issue last seen only in older builds still gets filed — note it may already
  be fixed.

### Always skip as noise

`Wearable.API is not available on this device` (API_UNAVAILABLE) · `Firebase Installations
Service is unavailable` · DNS and connectivity errors (`GaiException`,
`UnknownHostException`, `SocketTimeoutException`, `SocketException`, `ConnectException`,
`EOFException`, broken pipe) · Remote Config server-side errors · similar
device/network-environment failures.

## 4. Dedupe

Search existing issues for the Crashlytics issue ID with `mcp__github__search_issues`.

- **Open issue references it** → skip.
- **Closed issue references it and the crash still occurs** → add one comment noting the
  recurrence with fresh stats. Do not open a new issue.
- Otherwise → file.

## 5. Analyse

For each issue you will file: fetch the sample event, find the referenced files in the repo
with Grep/Read, and determine the root cause. Reference **real file paths and line numbers**,
and include a short snippet of the proposed change.

If the trace bottoms out in framework code (Compose internals, Play Services), find the
nearest app-code frame and reason from there — for a duplicate-key Compose crash, grep the
repo for the relevant `key =` usages.

**Be honest about confidence.** Mark speculative diagnoses as speculative. An empty stack
with one affected user is a *diagnostics* item — recommend better capture (dSYM upload,
breadcrumbs, hook coverage) rather than inventing a root cause.

## 6. File

Issue creation works through the GitHub MCP tools (`mcp__github__issue_write`, method
`create`). `GH_TRIAGE_TOKEN` is **not** required.

- **Title:** `[Crashlytics] <ExceptionType> in <Class.method or file> (<short issue id>)`
- **Labels:** `triage`, plus `bug` and the platform (`ios`) where they apply
- **Body sections:** Impact (events + users in 24h, versions firstSeen→lastSeen, platform,
  signals) · Stack trace (trimmed, fenced) · Root cause analysis · Suggested fix (file
  references and snippet) · Links (the issue's `uri`)
- End every issue and comment with:

```
---
_Generated by [Claude Code](https://claude.ai/code)_
```

## 7. Hand off to the team

For the **two highest-impact issues filed this run**, run the `/investigate_issue` flow on
each so the engineering team drafts and verifies a fix while the analysis is fresh. Skip
diagnostics-only items — there is nothing for the team to implement.

Cap it at two per run to keep the routine bounded; the rest wait for the next run or a
manual `/investigate_issue <n>`.

## 8. Report

Notify with `PushNotification` **only** when the run found something worth waking someone
for: new fatal crashes, a spike, issues filed, or the routine itself failing (auth rejected,
API unreachable, no secret configured). A clean run — nothing new, everything healthy —
should end quietly with no notification.

Wrap the notification body in `<routine_summary>` tags; the first sentence becomes the phone
banner, the full text the email.

End the session summary with: crashes reviewed per platform, issues filed with links, how
many skipped as duplicates or noise, what the team investigated, and anything that failed.
If there is nothing new, say `No new crashes in the last 24h` plus the totals observed.

## Standing limits

**Analysis only.** Never commit or push application code from the triage routine itself —
the `/investigate_issue` flow owns branches and diffs, and even it does not open pull
requests without an explicit ask.
