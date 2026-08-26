---
description: Sweep open issues labelled `triage` and run the engineering team over the highest-impact ones that haven't been investigated yet.
argument-hint: [how many to investigate — default 2]
---

Work the triage queue. Investigate **$ARGUMENTS** issues (default **2** if no number given).

This is the same sweep the daily `triage` skill performs in its step 7, split out so you can
run it on its own — after labelling issues by hand, or to drain a backlog without re-fetching
Crashlytics.

## 1. Fetch the queue

```
mcp__github__list_issues(owner: "space-launch-now", repo: "SpaceLaunchNow-KMP",
                         labels: ["triage"], state: "OPEN")
```

Drop anything already carrying `investigated`.

## 2. Rank what remains

1. Impacted users in the last 24h (from the issue's Impact table, where it has one)
2. Whether it affects the current release — `version.properties` is the source of truth
3. Age, oldest first, as the tiebreak so nothing starves

## 3. Skip, don't investigate

- **Diagnostics-only items** — empty stack, no app-code frame. Nothing to implement, and a
  speculative patch is worse than none.
- Anything already labelled `investigated`.
- Issues where a human asked a question in the comments and is waiting on an answer. A bot
  talking over a maintainer is worse than a slow queue.

An issue with no Impact table (a human-filed bug rather than a crash report) is still valid
work — rank it by age and treat the body as the goal.

## 4. Investigate

Run the `/investigate_issue` flow on each selected issue, in rank order. Each one gets the
full team: `manager-engineer` → `implementation-engineer` → `quality-engineer` →
`security-engineer`, then one consolidated comment on the issue and the `investigated` label.

Run them **sequentially, not in parallel** — they share the working tree unless each is given
`isolation: "worktree"`, and a bounded queue is not worth the coordination risk.

## 5. Report

- Which issues were investigated, with links
- Which were skipped and why
- **How many remain queued** — always, so a growing backlog stays visible

## Standing limits

- **Do not open pull requests.** The flow comments a proposed fix; merging is a human call.
- Do not close issues. `investigated` means the team has reported, not that it is fixed.
- To park an issue, remove its `triage` label. That is the only off switch, deliberately —
  there is no second "ignore me" label to forget about.
