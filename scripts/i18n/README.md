# scripts/i18n

Helpers for app text translation (spec 017).

## Load translation memory 406219 into Crowdin project 948217 (T29)

The old Android project is deleted, but its translation memory (`406219`, 606 segments) lives on
in the account. The project must use it. The Crowdin token exists only as the repo secret
`CROWDIN_PERSONAL_TOKEN`, so the step runs in CI:

1. GitHub, Actions, **Crowdin Assign TM**, **Run workflow** (leave `assign` ticked).
2. The job adds `406219` to the project's assigned TMs (`PATCH /projects/948217`, path `/assignedTms`).
   It leaves the default TM and every other project setting alone.
3. The job reads the project and the TM back and fails unless the project uses the TM and the TM
   holds about 606 segments. Read the log lines `Read back: ...`.

To check later without a change, run the workflow with `assign` unticked, or run
`CROWDIN_PERSONAL_TOKEN=... python3 scripts/i18n/crowdin_assign_tm.py`.

Web UI fallback, if the API refuses: Crowdin, project 948217, **Settings**, **Translation Memory**,
tick `SpaceLaunchNow-Android` (`406219`), save.
