---
description: Drive one crash or bug through the engineering team — plan, implement, verify, security-review.
argument-hint: <crashlytics-id | issue # | short description>
---

Work item: **$ARGUMENTS**

Run the engineering team over this item. Do not skip stages, and do not collapse two
stages into one agent.

## 1. Plan

Invoke `manager-engineer` with the work item and whatever trace, issue body, or report you
have. It returns a work breakdown with, per unit: goal, expected files, owner,
`verifiable_on_linux`, dependencies, and a scope guard.

If the breakdown has more than one unit, confirm the ordering with me before dispatching —
independent units should fan out in parallel, dependent ones serialize.

## 2. Implement

For each unit, invoke `implementation-engineer` with the unit's goal, files, and scope
guard. Use `isolation: "worktree"` when units run in parallel or the change is more than a
couple of files — the `SessionStart` hook in `.claude/settings.json` copies gitignored
secrets into the worktree automatically.

Give the agent the full stack trace when there is one. It fixes the nearest app-code frame,
minimally, in the local idiom.

## 3. Verify

Invoke `quality-engineer` on the resulting diff with the unit's stated goal. It builds what
it can, reviews the diff adversarially, and returns PASS / PASS (partial verification) /
FAIL.

A **FAIL** goes back to `implementation-engineer` with the finding — not to me, and not
patched by the quality engineer. Loop until PASS or until the finding turns out to need a
scope decision, which goes to `manager-engineer`.

Treat **PASS (partial verification)** as the normal outcome for anything touching
`iosMain`: this is a Linux container and iOS targets are Kotlin/Native. Carry that caveat
forward into every summary rather than dropping it.

## 4. Security review

Invoke `security-engineer` on the diff whenever it touches secrets, `BuildConfig`, manifest
permissions, consent/UMP or ad loading, Crashlytics/Datadog/analytics payloads, RevenueCat
or billing, notifications, or deep links. When in doubt, run it — it is cheap and read-only.

Findings in the diff block the unit. Pre-existing observations go back to
`manager-engineer` as separate units; do not expand this change to absorb them.

## 5. Integrate

Return to `manager-engineer` for the integration check: goal met, checks green or honestly
caveated, security cleared, diff within scope, Conventional Commit message.

Then report to me:

- what changed, per file
- what was verified and **what could not be**, in plain words
- security verdict
- anything deliberately left out of scope, and where it was routed

Commit only if I asked for it. **Do not open a pull request unless I explicitly ask.**
Never add Claude as a commit co-author.
