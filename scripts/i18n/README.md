# scripts/i18n

Helpers for app text translation (spec 017).

## Check and fill translations

`check_translations.py` compares every translation with its English master and runs in CI
("Check translations (placeholders)" in `pr-validation.yml`):

- Masters: every `composeApp/src/commonMain/composeResources/values/*.xml` (found by glob, so a new
  `strings_<feature>.xml` is covered), the Android res `values/strings.xml` and the Wear
  `values/strings.xml`. Each is compared with the file of the same name in `values-<lang>`.
- Catalogs: `iosApp/iosApp/InfoPlist.xcstrings` and `iosApp/LaunchWidget/Localizable.xcstrings`.
  Language codes there are `es`, `pt-BR` and `zh-Hans` (the others match the Crowdin code).
- It exits 1 only on a placeholder mismatch. A language with no file yet is "not started", and low
  coverage, a catalog key with no value, a plural category or a length limit are warnings.
  `--strict-plurals` also fails on a missing plural category.

`translate.py` fills missing keys in `values-<lang>` with a language model, into the file of the same
name as the master. It never touches a key that exists, and it does not write the catalogs.

Run the tests: `python3 -m unittest discover -s scripts/i18n/tests`.

## What Crowdin holds (crowdin.yml)

`crowdin.yml` maps three Android XML source sets to project 948217. Each writes back to
`values-<code>/<same file name>` and uses `update_as_unapproved`:

- Compose resources: `composeApp/src/commonMain/composeResources/values/*.xml`.
- Android widgets and notifications: `composeApp/src/androidMain/res/values/strings.xml`.
- Wear OS: `wearApp/src/main/res/values/strings.xml`.

`app_name` and `app_name_debug` carry `translatable="false"`, so Crowdin skips them. A
A top-level `languages_mapping` pins the folder names `es`, `pt-rBR` and `zh-rCN`, because `%android_code%` may
give `es-rES`. The iOS catalogs are not in Crowdin. Crowdin Sync uploads sources on a push to main that
changes `crowdin.yml` or a Compose source file. A change to the Android res or Wear source alone does
not start it: run the workflow by hand. Never add Russian.

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
