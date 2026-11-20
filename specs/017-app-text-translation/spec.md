# App Text in 10 Languages (Android, iOS, Wear OS) — Plan

**Status:** Draft / proposal (research only, no product code); updated 2026-11-20 with Caleb's rulings (section 16)
**Date:** 2026-11-19 (updated 2026-11-20)
**Owner:** Caleb Jones
**Area:** Localization, Compose Multiplatform resources, Android, iOS widgets, Wear OS
**Task:** [Tasks#1217](https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1217) (parent [Tasks#1214](https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1214))
**Checked against:** KMP `main` at `e98c97e7` (5.52.0-b116), 2026-11-19

---

## 1. Goal

Put the text inside the apps (Android, iOS, Wear OS) in 10 languages. People who do not read English
should be able to use the app, not only find it in the store.

**Non-goals:** store listing text ([Tasks#1215](https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1215),
[Tasks#1216](https://github.com/ItsCalebJones/SpaceLaunchNow-Tasks/issues/1216)); the web site (Tycho);
any change to the Terminus API (a schema change needs Caleb's approval first, see Q8); any product code
in this task.

## 2. Summary of recommendations

| # | Question | Recommendation |
|---|---|---|
| 1 | Inventory | About 1,100 translatable text sites in `commonMain` (pattern count, debug screens excluded), 19 in `androidMain`, 17 in Wear, about 40 in the iOS widgets, 2 in `Info.plist`. Expect 1,300 to 1,500 sites and about 900 to 1,100 unique keys once view models and mappers are counted and repeats merged. |
| 2 | Key system | Compose Multiplatform resources (`values/strings.xml`) for shared UI. Android `res/values` for Android-only surfaces and Wear. A String Catalog (`.xcstrings`) for iOS widgets and `Info.plist`. One English master per scope; one script feeds all of them. |
| 3 | Move plan | Move by feature, one pull request per task (11 move tasks). A script proves each moved string is byte-equal to the literal it replaced. A CI script with a baseline file stops new English text in code. |
| 4 | Translation flow | Crowdin holds the app translations (project `948217`); a language model with a glossary and key comments writes the first fill; people correct it in Crowdin. No paid reviewer now. Missing key falls back to English. |
| 5 | Languages | Fixed by Caleb (2026-11-20): German, Spanish, French, Chinese (Simplified), Dutch, Ukrainian, Italian, Polish, Portuguese (Brazil), Czech. Crowdin targets `de`, `es-ES`, `fr`, `zh-CN`, `nl`, `uk`, `it`, `pl`, `pt-BR`, `cs`. Russian is out by his choice. No right-to-left language. |
| 6 | Layout and format | Dates and numbers already follow the device locale. The relative time, countdown, plural and unit code is English-only and must change (task T7). Test with a pseudo-locale and text-tree snapshots first, screenshots second. |
| 7 | Language setting | Follow the device language and the OS per-app language setting. No in-app picker in the first release. |
| 8 | Server text | API data stays English. Map the finite lists (launch status, mission type, orbit) to keys on the client. Push text stays English first; client-built text is the later option. |
| 9 | Order | 7 phases (1, 1b, 2 to 6; phase 0 is dropped), 27 tasks. The first translated app ships after phase 4 (all shared screens moved), in all 10 languages at once. |
| 10 | Measure | Active users per device language, D1/D7/D30 retention per language, rating per country, premium conversion per language. Take a 28-day baseline before the first release. |

## 3. Current state (verified 2026-11-19)

- **No string keys in shared code.** `composeApp/src/commonMain/composeResources` holds `drawable/` and
  `files/aboutlibraries.json`. No `values/strings.xml`. `compose.components.resources` is a dependency
  (`composeApp/build.gradle.kts:234`), so the library is ready. Compose plugin 1.11.1, Kotlin 2.3.21.
- **Android resources:** 29 strings in two files. `composeApp/src/androidMain/res/values/strings.xml`
  has 14 (app name, debug app name, 3 widget descriptions, 9 notification messages); `wearApp/src/main/res/values/strings.xml`
  has 15. Only 9 of the phone strings are read from Kotlin (`NotificationDisplayHelper.kt:500-513`); the 3
  widget descriptions are read from `res/xml*/…_widget_info.xml`. In Wear, only `app_name` and
  `next_launch_tile_label` are read (from `AndroidManifest.xml`); the other 13 Wear strings have no
  reader, and the Wear UI writes its text in Kotlin.
- **No `values-<lang>` folder** in any module. `android:supportsRtl="true"` is set in both manifests.
  No `locales_config.xml`.
- **iOS:** no `.xcstrings`, no `.lproj`. `knownRegions = (en, Base)` in `iosApp.xcodeproj/project.pbxproj`.
  `Info.plist` has two user-facing strings (`NSLocationWhenInUseUsageDescription`,
  `NSUserTrackingUsageDescription`) and no `CFBundleDisplayName`. The notification service extension
  holds no static text: it copies the server title and body (`NotificationService.swift:182-183`).
- **Dates and numbers follow the device locale.** `DateTimeUtil` and `NumberFormatUtil` have per-platform
  actuals that read `Locale.getDefault()` (Android) and `NSLocale` (iOS) through `LocaleUtil`.
- **Fonts:** `ui/theme/Type.kt` sets no custom font family, so every platform uses its system font.
- **Build:** AAB language splits are on (`composeApp/build.gradle.kts`, `bundle { language { enableSplit = true } }`).
  `MainActivity` extends `ComponentActivity`, not `AppCompatActivity`. `androidx.appcompat` 1.7.1 is in
  the version catalog. `minSdk` 26 (Wear 30), `targetSdk` 36.
- **Test tooling:** `commonTest`, `jvmTest`, `desktopTest` exist; 180 `@Preview` functions exist; no
  screenshot-test library, no detekt or ktlint. CI (`pr-validation.yml`) runs `:composeApp:lint test`
  and already runs one Python check (`scripts/check-encoding.py`).

## 4. Q1 — Inventory

### 4.1 Counts

Method: a regex count over `*.kt` for a string literal that contains a letter and sits in a `Text(…)`
call or in one of these named arguments: `text`, `title`, `subtitle`, `headline`, `label`,
`placeholder`, `supportingText`, `message`, `description`, `summary`, `hint`, `buttonText`,
`actionLabel`, `confirmText`, `dismissText`, `contentDescription`. Files with `ebug` in the path and
test folders are skipped. The script is in Appendix A; run `python -I count_strings.py <repo root>`.

| Source set | Sites (pattern count) | With `$` template | Notes |
|---|---|---|---|
| `composeApp` `commonMain` | 1,095 | 63 | shared Compose UI for Android, iOS, desktop |
| `composeApp` `androidMain` | 19 | 2 | 13 in Glance widgets (`widgets/`) |
| `composeApp` `iosMain` | 3 | 2 | ads, billing, notifications |
| `composeApp` `desktopMain` | 2 | 0 | dev target; low priority |
| `wearApp` | 17 | 1 | 6 in the tile, 5 in settings, 3 launch, 3 premium |
| iOS widgets (`iosApp/LaunchWidget/*.swift`) | 40 `Text("…")` (9 + 11 + 20) | 3 | the issue counted 37; the 3 extra were not matched one by one (some are placeholder text such as "Countdown here") |
| `Info.plist` | 2 | 0 | permission texts |
| Android `res/values` | 29 | 5 `%s` | see section 3 |

The same method over the files that the debug screens use finds about 125 more sites
(`DebugSettingsScreen.kt` alone has about 110). Debug screens are developer tools: do not translate them.

The pattern count misses text that never sits in those arguments. A second pass for multi-word English
literals outside log calls finds about 300 more in `commonMain` (some overlap with the first count):
`ui/detail` 43, `ui/settings` 40, `ui/subscription` 28, `ui/home` 24, `data` 21, `ui/viewmodel` 15,
`ui/astronaut` 14, `ui/components` 14. These are strings in `when` branches, `listOf(…)`, enum display
names, mappers and view models. **Planning number: 1,300 to 1,500 sites.**

Repeats are common (`"Unknown error"` appears 25 times, `"Back"` 23, `"Retry"` 14, `"Show Less"` 10).
Merging them gives about 900 to 1,100 unique keys (estimate; the move tasks will measure it).

### 4.2 By feature (`commonMain`, pattern count)

| Feature folder | Sites | Feature folder | Sites |
|---|---|---|---|
| `ui/settings` (no debug) | 166 | `ui/spacestation` | 60 |
| `ui/detail` (launch detail) | 126 | `ui/schedule` + `ui/event` | 60 |
| `ui/home` | 86 | `ui/compose` (countdown, headers) | 39 |
| `ui/onboarding` | 84 | `ui/starship` | 33 |
| `ui/rockets` | 79 | `ui/explore` | 31 |
| `ui/subscription` (paywall, support) | 69 | `ui/components` | 30 |
| `ui/astronaut` | 68 | `ui/newsevents` | 25 |
| `ui/agencies` | 62 | `ui/roadmap` 21, `ui/about` 20, `ui/layout` 16, `ui/video` 7, `ui/preview` 7, other 6 | 77 |

### 4.3 Surfaces outside Compose

| Surface | What | Count | Note |
|---|---|---|---|
| Android notification channels | `NotificationChannel.kt` enum: name and description | 8 channels, 16 strings | shown in system settings |
| Android notification text | `NotificationDisplayHelper` | 9 strings in `values/strings.xml` | used when the push carries data only; otherwise the server title and body show |
| Android widgets (Glance) | `widgets/LaunchListWidget.kt`, `NextUpWidget.kt` | 13 sites | "Premium Widget", "Upcoming Launches", … |
| Android widget picker | `res/xml*/…_widget_info.xml` | 3 descriptions | already keys |
| Android manifest | app label | 1 | `${appName}` placeholder; debug label `app_name_debug` |
| Wear | `wearApp` Compose, tile, complication | 17 | separate APK; no shared Kotlin with `composeApp` |
| iOS widgets | 3 Swift files | 40 | SwiftUI `Text("…")` |
| iOS `Info.plist` | permission texts | 2 | need `InfoPlist.xcstrings` |
| iOS notification service extension | none | 0 | text comes from the server |
| Paywall | `ui/subscription/SupportUsScreen.kt` | 69 | custom screen (not RevenueCat UI). Product names and prices come from the store and are already localized by it. |
| Error messages | spread over view models and screens | included above | `"Unknown error"` ×25; many come from exceptions (`e.message`) and stay English |
| Ads / consent | AdMob and UMP forms | 0 in code | Google's SDK text; follows the device language |
| Desktop | `desktopMain` | 2 | developer target |

### 4.4 Strings with numbers, dates, plurals or joined parts

- **Templates (`$` in the string):** 63 in `commonMain`. Each becomes a key with positional arguments (`%1$s`).
- **English plural hacks:** `"$totalAttempts Attempt${if (totalAttempts != 1) "s" else ""}"`
  (`CombinedLaunchOverviewCard.kt:229`), `"Free Trial – $daysLeft day${if (daysLeft == 1L) "" else "s"} remaining"`
  (`SupportUsScreen.kt:1317`). A loose `== 1) "…"` search finds 30 lines; at least 2 are strict plural hacks.
  Each needs a `<plurals>` entry.
- **Joined parts:** `"Today at ${time}"`, `"Tomorrow at …"`, `"Yesterday at …"`, `"$dayName at …"`
  (`DateTimeUtil.kt:83-88`); `"${formatQuarter} ${year}"`, `"H1 $year"`, `"NET $year"`, `"TBD"`
  (`DateTimeUtil.kt:252-265`); `"T - 1d 14h"` timeline format (`DateTimeUtil.kt:107-176`); the countdown
  `"…d …h …m …s"` (`CountdownWidget.kt:225-228`) and the `"DAYS"`, `"HOURS"` labels
  (`CountdownWidget.kt:351`); `"${distance} km"` (`LandingDetailsCard.kt:208`).
- **Dates and numbers:** formatted by `DateTimeUtil` and `NumberFormatUtil` with the device locale; not
  strings in the sense of this task, but the surrounding words are.

### 4.5 How to run the count again

Run the script in Appendix A for the Compose sources, then:

```
grep -c '<string ' composeApp/src/androidMain/res/values/strings.xml wearApp/src/main/res/values/strings.xml
grep -c 'Text("' iosApp/LaunchWidget/*.swift
grep -c -A1 'UsageDescription' iosApp/iosApp/Info.plist
```

After the move (Q3) the check changes: the CI script (Q3) prints the number of baseline entries left.
The goal is zero.

## 5. Q2 — Key system

### 5.1 Options compared

| Option | Plurals | Non-composable access | Swift widgets | Wear | Verdict |
|---|---|---|---|---|---|
| **Compose Multiplatform resources** (`composeResources/values/strings.xml`, `values-<lang>/`) | `<plurals>`, `pluralStringResource` and suspend `getPluralString` | suspend `getString(Res.string.x, args)` | no | no (separate module) | **Use for shared UI.** Official, already a dependency, same XML as Android. |
| Android `res/values` | `<plurals>` | `context.getString` | no | yes | **Use for Android-only code** (workers, notifications, widgets, manifest) and for Wear. |
| Apple String Catalog (`.xcstrings`) | built in | `String(localized:)` | yes; SwiftUI `Text("…")` is already a localized key | no | **Use for the iOS widgets and `Info.plist`.** |
| moko-resources | yes | yes | needs its own bridge | no | Reject: a second system next to the one JetBrains ships. |
| Code-based maps (Lyricist and similar) | by code | yes | no | no | Reject: translators cannot edit Kotlin; no tool support. |
| Server-delivered strings (OTA) | yes | yes | needs a client | needs a client | Reject for now: needs a server and offline fallback. |

Source for the Compose resource API (strings, templates, arrays, plurals, suspend `getString` and
`getPluralString`): <https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html>
(page built 2026-11-18, read 2026-11-19). Locale handling:
<https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html>.

### 5.2 How code outside a composable gets a string

- `commonMain` view models, mappers and use cases: **do not hold display text.** Return a typed result
  (an enum, a sealed class, or a `StringResource` plus arguments) and let the composable call
  `stringResource`. Where a plain `String` is unavoidable (a log line, a notification built in common
  code), call `suspend getString(Res.string.key, args)` from a coroutine.
- `androidMain` workers, notification helper and Glance widgets: `context.getString(R.string.key, args)`
  from `androidMain/res`. Glance code cannot suspend inside its composables; resolve strings with
  `LocalContext.current.getString`.
- Swift widgets: `Text("key")` and `String(localized: "key")` against `Localizable.xcstrings`.
- Wear: `stringResource(R.string.key)` from `wearApp/src/main/res`.

### 5.3 One English text, four files: how they stay in step

There are four masters because there are four resource systems: `composeResources/values/strings.xml`,
`androidMain/res/values/strings.xml`, `wearApp/src/main/res/values/strings.xml`, and
`iosApp/LaunchWidget/Localizable.xcstrings` (+ `InfoPlist.xcstrings`). About 40 strings repeat across
them ("No upcoming launches", "Premium", the widget text). Rule: the translation script (Q4) reads all
four English masters, treats a key as shared when its name starts with `shared_`, translates it once,
and writes it to each file. A second small script (task T4) copies the shared English keys from the
Compose master to Wear and to the String Catalog so the English text cannot drift.

`scripts/i18n/sync_shared_keys.py` is that script (T4). It writes each `shared_*` key into a generated block
of the Wear `values/strings.xml` and into the `en` localization of `iosApp/LaunchWidget/Localizable.xcstrings`
(`%1$s` becomes `%1$@`; an apostrophe is escaped for Android). A second run changes nothing, and other
languages and other keys in the catalog are kept. `--check` writes nothing and exits 1 when a file differs;
a CI step (added with the pr-validation changes, not in the T4 PR) runs it so the copies cannot drift. Tests: `python3 -m unittest discover scripts/i18n/tests`.

### 5.4 Plurals and arguments

- Arguments: positional only (`%1$s`, `%2$d`) so word order can change. Never build a sentence from
  pieces in code.
- Plurals: `<plurals>` with CLDR categories. English needs `one` and `other`; Ukrainian and Polish need `one`,
  `few`, `many`, `other`; Czech needs `one`, `few`, `many`, `other`; Chinese needs only `other`. The
  proof-of-concept task (T1) must show that Compose resources pick the correct category for Ukrainian
  and Polish on Android and iOS.
- Numbers inside strings come from `NumberFormatUtil` and are passed as arguments.

### 5.5 Key naming rule

`<surface>_<screen>_<element>[_<qualifier>]`, lower snake case, English words, no text from the string
itself, no number suffix.

- Surface: `app` (Compose shared), `notif`, `widget`, `wear`, `plist`, `shared`.
- Examples: `launch_detail_section_crew`, `settings_notifications_toggle_one_hour`,
  `onboarding_step2_title`, `common_back`, `common_retry`, `error_unknown`.
- Words used in 3 or more places become `common_*` (`common_back`, `common_retry`, `common_show_less`).
- Plurals end in `_count` (`launch_detail_attempts_count`).
- Every key has an XML comment above it: where it shows, the control type, and any length limit
  (`<!-- Button, one line, max 14 chars -->`). The translation script sends the comment to the model.

## 6. Q3 — Move plan: strings into keys with no change on screen

### 6.1 Method

1. A per-feature move task takes one feature folder (Section 4.2). A worker extracts each literal to
   `composeResources/values/strings.xml` (merging repeats into `common_*`) and replaces the literal with
   `stringResource(Res.string.key)`. Strings built in view models and mappers move too; their return
   type changes to a typed result as in 5.2.
2. English text is **not** edited during the move. Fixes to wording come later in separate commits.
3. One pull request per task, about 60 to 130 sites each (T20 is the largest, about 190; the worker may split it).

### 6.2 The check that a screen did not change

Three layers, cheapest first:

1. **Literal-to-key equality script** (new, `scripts/i18n/verify_move.py`, in task T2). For each hunk it
   pairs a removed literal with the added `Res.string.<key>` and checks that the key's English value is
   equal to the literal after XML unescaping and `%1$s`/`$var` mapping. Run in CI on every move PR.
   This proves "same text" for every moved string, not only for the screens a person opens.
2. **Text-tree snapshot per screen.** A `desktopTest` (JVM) renders each screen that already has a
   `@Preview` (180 exist) with `runComposeUiTest`, dumps the semantics tree (text and content
   descriptions) to a file, and compares it with the file stored before the move. The snapshot is taken
   once on `main` (task T1) and checked in each move PR. No new library is needed.
3. **Device smoke check** by the worker on the five busiest screens (home, launch detail, settings,
   onboarding, paywall) on one Android device or emulator; iOS by the Xcode CI build and a manual look
   by Caleb at release time (iOS Kotlin cannot be compiled on the agent host).

Pixel screenshot tests (Roborazzi, Paparazzi, or Compose's `captureToImage`) are a later addition
(9.3 item 3, task T25); they are not needed to prove an English move.

### 6.3 Stop new English text in code

- A CI script `scripts/i18n/check_hardcoded_text.py`, run in `pr-validation.yml` next to
  `check-encoding.py`. It uses the same patterns as Appendix A. It compares the findings with
  `scripts/i18n/hardcoded-baseline.txt` (file and line-independent: file + the literal). A new literal
  not in the baseline fails the build. Move tasks delete lines from the baseline. Debug files are
  exempt. A line comment `// i18n-ignore: <reason>` exempts one literal (for example a log tag).
- Swift: a `SwiftLint` custom rule is not needed. `Text("…")` in SwiftUI already looks up the catalog;
  the CI step checks that the String Catalog has no entry in state `new` or `stale`
  (`xcstrings` has the field).
- Other option: a detekt custom rule. Rejected for now: detekt is not in the build, and adding it
  costs more than the 80-line script.
- Android Lint `HardcodedText` only checks XML layouts, so it does not help here.

## 7. Q4 — Translation flow

### 7.1 Options

| Option | Cost (estimate) | Where it runs | Review | Verdict |
|---|---|---|---|---|
| **Language model with glossary and key comments** | Under US$10 per full pass of 10 languages (about 5,000 English words × 10 languages; assumes 4.5 words per string × 1,100 strings; token prices not checked) | A script in the repo (`scripts/i18n/translate.py`) run by an agent or by Caleb | Back-translation check by a second model call; native-speaker spot checks for the top languages | **Recommend** |
| Community platform (Crowdin, Weblate, and similar) | Both list free plans for open-source projects; terms not checked here. Pay in coordination time. | Hosted web tool; sync through a pull request or a bot | The community. The repo is public, but the app has no community yet; expect long gaps. | **Chosen by Caleb, 2026-11-20 (Tasks#1217): Crowdin holds the strings and the review; the model fills first.** See 7.4. |
| Paid human service | US$0.08 to 0.20 per word is the usual range (not verified here); 50,000 words → about US$4,000 to 10,000 per full pass, and again for each change | Vendor portal | The vendor | Reject for now; revisit for the top 2 languages when data shows revenue |

### 7.2 Recommended flow

_Steps 2 and 5 are superseded by 7.4: people correct the text in Crowdin, with no paid reviewer, and the model fill goes through Crowdin._

1. A new English key is added with its comment (5.5).
2. `translate.py` reads the four English masters, the **glossary** (`specs/017-app-text-translation/glossary.md`
   in the build task: "launch", "scrub", "NET", "T-minus", "payload", "booster", "Go for Launch", agency
   names that stay English, and the app's name) and the key comments. It writes only the missing keys to
   each `values-<lang>/strings.xml`, `.xcstrings` and Wear file. It never rewrites a key a human edited
   (a `<!-- reviewed -->` marker or a stored hash).
3. A check runs placeholders (`%1$s`, `%d`, `\n`, `&amp;`), plural categories per language, and length
   against the comment limit. A failed string is retried once, then listed in a report.
4. A second model call back-translates the result into English and flags meaning drift.
5. The pull request shows the diff. Volunteers from the Discord or Reddit audience may correct
   strings in Crowdin; no paid reviewer.
6. Merge. A user report of a bad translation becomes a one-line fix to the XML.

### 7.4 Crowdin (ruling of 2026-11-20)

Caleb ruled: use Crowdin, pull the strings into resources, and let a language model fill first.
The flow in 7.2 stands with one change: Crowdin is the store of translations and the review tool.
- `crowdin.yml` at the repo root maps each English master to its `values-<lang>` files. It holds no
  secret; the project id and token come from `CROWDIN_PROJECT_ID` and `CROWDIN_PERSONAL_TOKEN`.
- Step 1: the English `strings.xml` goes up (`crowdin upload sources`).
- Step 2: the model fills the missing keys and uploads them as unapproved translations
  (`crowdin upload translations --auto-approve-imported=false`), so people can review them.
- Step 3: `crowdin download` brings the reviewed files back as a pull request.
- The old Android app's Crowdin project (`SpaceLaunchNow-Android`, 597 strings, 26 languages) can seed
  the translation memory. The old project is deleted; T29 loads its translation memory (`406219`) into project `948217`.

### 7.3 When a translation is missing

Android and Compose resources fall back to the English `values/` file for a missing key. The String
Catalog falls back to the English key. So a missing translation shows English, never a blank or a key.
CI reports the coverage per language and fails only if a **placeholder mismatch** exists (that crashes at
runtime); a language under 95% is a release-checklist item for Caleb, not a failed build.

## 8. Q5 — Which 10 languages

### 8.1 The set (ruling of 2026-11-20)

Caleb fixed the set. The first draft planned a data pull (GA4, RevenueCat); it is not needed.

| Language | Crowdin target | Active users (GA4 app property `164090905`, 90 days to 2026-11-19) |
|---|---|---|
| German | `de` | 1,313 |
| Spanish | `es-ES` | 1,070 |
| French | `fr` | 907 |
| Chinese (Simplified) | `zh-CN` | 616 |
| Dutch | `nl` | 453 |
| Italian | `it` | 398 |
| Polish | `pl` | 363 |
| Portuguese (Brazil) | `pt-BR` | 328 |
| Czech | `cs` | 186 |
| Ukrainian | `uk` | 74 |

- Russian (437 users) is out by Caleb's choice ("replace Russian with Ukrainian"), not by the data.
  Do not add it back.
- Ukrainian has the best old material: 574 of 597 strings in the old Android project.
- The count of 10 fits the free Crowdin plan: 60,000 hosted words (source words times target
  languages); the app is estimated at about 50,000.
- Compose resource folders use Android codes: `values-de`, `values-es`, `values-fr`, `values-zh-rCN`,
  `values-nl`, `values-uk`, `values-it`, `values-pl`, `values-pt-rBR`, `values-cs`. `crowdin.yml`
  maps them through `%android_code%`.

### 8.4 What a right-to-left or CJK language adds

Not needed for this set: no language in it is right-to-left, and Chinese (Simplified) is the only CJK
language. The notes stay for a later set.

- **Right-to-left (Arabic, Hebrew, Persian, Urdu):** the layout mirrors. The manifests already set
  `supportsRtl="true"`, but no screen has been checked. Needs: replace `Modifier.padding(left/right)`
  and `Alignment.Left` with start/end; `Icons.Default.ArrowBack`-style icons need `AutoMirrored`
  versions; the countdown digits row and charts must stay left to right; the Wear tile and Glance widgets
  need a mirror check; iOS needs a check of the Compose layout direction. Estimate: 1 extra task of size
  L (about 2 to 3 days) plus 1 test task, and a native reader for review. Arabic also has 6 plural forms.
- **CJK (Chinese, Japanese, Korean):** no spaces between words, so wrapping and `maxLines` behave
  differently; text is often shorter, so fixed-width chips may look empty; system fonts are used
  (`Type.kt` sets no custom font), so no font file is bundled, but every `FontWeight`/`letterSpacing`
  choice and all-caps labels (`"NEXT LAUNCH"`, `"UPCOMING LAUNCHES"`) need a look, because CJK has no
  upper case and wide letter spacing hurts. Chinese is `zh-CN` (Simplified) only. Estimate: no extra build task, one extra QA pass per language.
- **Languages with long words (German, Dutch, Polish, Finnish):** see Q6.

## 9. Q6 — Layout and format

### 9.1 What the code does today

| Area | Today | Must change |
|---|---|---|
| Date and time | `DateTimeUtil` formats with the device locale (Android `Locale.forLanguageTag(LocaleUtil.getLocaleTag())`; iOS `NSDateFormatter` locale). | Nothing for the basic formats. |
| Relative day words | English: `"Today at"`, `"Tomorrow at"`, `"Yesterday at"`, `"$dayName at …"` (`DateTimeUtil.kt:83-88`). | Move to keys; pass the time as an argument. The weekday name is English enum text (`dayOfWeek.name`, `DateTimeUtil.kt:86-87`), so it needs a key or a locale formatter. |
| Date precision words | `"TBD"`, `"H1 2026"`, `"H2"`, `"NET"`, quarter formats (`DateTimeUtil.kt:252-265`). | Keys. Decide whether `NET` stays (it is a term of the field; put it in the glossary). |
| Timeline offsets | `"T - 1d 14h 5m 3s"` built from ISO-8601 durations (`DateTimeUtil.kt:107-176`). | Keys for the unit letters per language (`d h m s`) or a locale-aware duration formatter. |
| Countdown | `"…d …h …m …s"` and `"DAYS"`, `"HOURS"`, `"MINUTES"`, `"SECONDS"` labels (`CountdownWidget.kt:225-228, 351`). The digit row is a fixed layout. | Keys for the four labels and the unit letters; the digit row stays left to right (RTL only matters for a later set). |
| Numbers | `NumberFormatUtil` uses `NumberFormat.getNumberInstance(Locale.getDefault())` / iOS actual. | Nothing. |
| Units | Metric only, English suffixes in code (`"$x km"`, `"N/A"`); no unit setting. | Keys for the suffixes. A units setting (imperial for en-US) is out of scope; note it as a later idea. |
| Plurals | English `if (n == 1)` hacks (4.4). | `<plurals>`. |
| Fonts | System fonts. | Nothing. Check Chinese falls back correctly on iOS and Android (system does this). |
| Layout | Fixed widths in chips, buttons, tabs, the tile and the widgets (not audited). | Audit with the pseudo-locale (9.3). |
| Custom locale source | `LocaleUtil` reads the platform locale. | If an in-app picker is added later (Q7), `LocaleUtil` must read the override too. |

### 9.2 Longer text

German and Dutch run 30% to 40% longer than English; Finnish more. Fixed-width controls, single-line
`maxLines = 1` labels, bottom-bar tabs, chips and the 3 Android widgets and Wear tile are the risk.
Rules for the move tasks: no new fixed `width` on text containers; use `Modifier.weight` or `widthIn`;
allow wrap or ellipsis; keep key comments with the length limit for tight controls.

### 9.3 Test method

1. **Pseudo-locale (first).** A script writes a test-only locale from the English master: every vowel
   accented, text lengthened by 40%, wrapped in markers (`[!! Näxt Lääünch !!]`). It is loaded in debug
   builds, previews and the desktop tests through the locale override pattern in the Compose
   documentation. It shows both untranslated literals (no markers → still hard-coded) and overflow. The
   Android system pseudo-locales `en-XA` and `ar-XB` (`pseudoLocalesEnabled`) also work for
   `androidMain/res` strings and test layout for those surfaces (RTL only if a later set needs it).
2. **Text-tree snapshots** (6.2) in each language: catches a missing key or a wrong plural.
3. **Screenshot tests** for the 8 densest screens (home, launch detail, rocket detail, settings,
   notification settings, onboarding, paywall, schedule) in English, German, and pseudo-locale. Tool choice (Roborazzi on Android JVM, or `captureToImage` in `desktopTest`) is a
   task of its own (T25); it is not needed before the first release of the move.
4. **Device pass** by Caleb or an agent on one phone and one Wear emulator in German and one CJK
   language before the first translated release ships.

## 10. Q7 — Language setting

### 10.1 Options

| Option | What it needs | Notes |
|---|---|---|
| **A. Follow the device language and the OS per-app setting (recommend)** | Android: `res/xml/locales_config.xml` (or `androidResources { generateLocaleConfig = true }` with `resources.properties`; AGP 8.13 supports it) and `android:localeConfig` in the manifest. iOS: add the languages to `knownRegions` and `CFBundleLocalizations`. | Android 13+ and iOS show a per-app language row in system settings. Android 12 and below (`minSdk` 26) follow the device language only. One source of truth for Compose, notifications, widgets and Wear. |
| B. In-app language picker | Compose side: a locale override as in the Compose docs. Android: the app needs `AppCompatDelegate.setApplicationLocales` on `AppCompatActivity` (today `ComponentActivity`) for Android 12 and below; AAB language splits must be turned off or the extra languages are not installed. iOS: set `AppleLanguages` and restart. `LocaleUtil` must read the override. | Notifications, widgets and Wear do not follow a Compose-only override, so users see two languages. Costs about 3 tasks. |

**Decided (Caleb, 2026-11-20): A.** The app follows the device language and the OS per-app setting; there is no in-app picker.

Recommend A for the first release. Add a "Language" row in Settings that opens the OS per-app screen
(`Settings.ACTION_APP_LOCALE_SETTINGS` on Android 13+, `UIApplication.openSettingsURLString` on iOS):
size S. Reconsider B only if support mail shows people who want a language different from the device.

### 10.2 What each platform needs in the build (option A)

- Android: `locales_config.xml` lists the 10 languages plus `en`. Keep AAB language splits on (they
  deliver the device language). Add `values-<lang>` folders in `composeApp/src/androidMain/res` and
  `wearApp/src/main/res`; Compose resources add `composeResources/values-<lang>`.
- iOS: `knownRegions` and `CFBundleLocalizations` list the languages; the String Catalog holds the
  widget and plist text; Compose resources in the shared framework are bundled by the Kotlin plugin.
- Wear: own APK; `values-<lang>` in `wearApp`; the Wear OS language is set on the watch, not on the phone.

## 11. Q8 — Text from the server

| Text | Today | Options | Cost | Recommendation |
|---|---|---|---|---|
| Launch, rocket, mission, agency, astronaut names and bios | API, English | (a) keep English; (b) on-device machine translation (ML Kit; Apple Translation on newer iOS) | (a) none; (b) model download per language and a new dependency on each platform, quality varies for names | (a). Proper nouns should stay. |
| Status, mission type, orbit, country labels (finite lists) | API, English (`"Go for Launch"`, `"Success"`, …) | Map by id or abbreviation to a key on the client; fall back to the API text for an unknown id | 1 task, about 60 keys | Do it (task T13) |
| News titles and summaries | API, English | keep English; link to source | none | Keep English |
| Push notification text | Server writes title and body (`NotificationService.swift:182-183`; Android shows the server title/body or its 9 fallback strings) | (a) keep English; (b) client builds the text from the type and fields the push already carries (Android already does this in `NotificationDisplayHelper`; the iOS extension would do the same); (c) server localizes by device language | (a) none; (b) 2 tasks and a check that the payload has every field (launch name, net, location); (c) a Server/Terminus change and a way to send the language: **needs Caleb's approval first** | (a) for the first release, (b) as phase 6. Do not start (c) without approval. |
| Error text from exceptions (`e.message`) | English from libraries | keep English; show a localized generic line plus the technical text under "details" | small | Generic line first |
| Store product names and prices | App Store / Play | none | none | already localized by the store |
| AdMob / UMP consent text | Google SDK | none | none | follows device language |

## 12. Q9 — Order and size

Sizes: S = under 1 day of worker time, M = about 1 day, L = 2 to 3 days. All tasks are in the KMP repo
unless noted. Not filed: Caleb accepts the plan first. At most 4 workers on the move tasks at once,
because every one of them edits `strings.xml` (merge conflicts in one file: use one file per feature,
`values/strings_<feature>.xml`, so the PRs do not collide).

### Phase 0 — Decisions and data (Tasks repo)

- ~~T0~~ Dropped. Caleb fixed the 10 languages (section 8.1); no data pull is needed.

### Phase 1 — Foundation (no visible change)

| ID | Title | Size | Blocked by |
|---|---|---|---|
| T1 | i18n: enable Compose string resources, move the `ui/about` screen as proof, text-tree snapshot harness, check plural rules for `uk` and `pl` | M | none |
| T2 | i18n: `scripts/i18n/verify_move.py` and the pseudo-locale generator | M | T1 |
| T3 | i18n: CI check for new hard-coded text with baseline | S | T1 |
| T4 | i18n: shared-key sync script (Compose master → Wear and String Catalog) | M | T1 |
| T5 | i18n: translation script (`translate.py`), glossary, placeholder and plural checks | M | T1 |

### Phase 1b — Crowdin (added 2026-11-20)

`crowdin.yml` is merged ([KMP PR #297](https://github.com/space-launch-now/SpaceLaunchNow-KMP/pull/297)). The
Crowdin project (`948217`) exists, and `CROWDIN_PROJECT_ID` and `CROWDIN_PERSONAL_TOKEN` are repo secrets.

| ID | Title | Size | Blocked by |
|---|---|---|---|
| T28 | i18n: first Crowdin sync: CI step for `crowdin upload sources` and `crowdin download` (as a pull request) | M | T1 |
| T29 | i18n: load translation memory `406219` (old project, 606 segments) into project `948217` | S | none |

### Phase 2 — Surfaces outside Compose

| ID | Title | Size | Blocked by |
|---|---|---|---|
| T6 | i18n: move Android notification strings, 8 channels, 13 Glance widget texts, manifest label | M | T1 |
| T7 | i18n: locale-aware relative time, countdown labels, timeline offsets, plurals, unit suffixes (`DateTimeUtil`, `CountdownWidget`) | M | T1 |
| T8 | i18n: iOS widgets to `Localizable.xcstrings`, `InfoPlist.xcstrings`, `knownRegions` (iOS CI only) | M | T4 |
| T9 | i18n: Wear UI, tile and complication strings | M | T4 |

### Phase 3 — Move the shared screens (parallel, up to 4 at a time)

| ID | Feature (sites) | Size | Blocked by |
|---|---|---|---|
| T10 | settings: main, theme, calendar (about 110) | M | T2, T3, T7 |
| T11 | settings: notifications (54) + `ui/event` + `ui/schedule` (60) | M | T2, T3, T7 |
| T12 | launch detail (126) | L | T2, T3, T7 |
| T13 | API enum labels: status, mission type, orbit, country (about 60 keys) | M | T2 |
| T14 | home (86) | M | T2, T3, T7 |
| T15 | onboarding (84) | M | T2, T3 |
| T16 | rockets (79) | M | T2, T3, T7 |
| T17 | subscription and paywall (69) | M | T2, T3 |
| T18 | astronaut (68) + agencies (62) | L | T2, T3 |
| T19 | space station (60) + starship (33) | M | T2, T3, T7 |
| T20 | explore, news and events, roadmap, components, layout, video, `ui/compose`, about, misc (about 190) | L | T2, T3, T7 |

### Phase 4 — First translated app

| ID | Title | Size | Blocked by |
|---|---|---|---|
| T21 | i18n: first translations in all 10 languages (one wave), `locales_config.xml`, device pass | M | T5, T6 to T20, T28 |
| T22 | i18n: "Language" row in Settings opening the OS per-app screen | S | T21 |

**The first release with a translated app is after T21.** Moving only some screens first would show a
mix of English and another language in one app; Phase 3 should be finished first.

### Phase 5 — After the first translated release

| ID | Title | Size | Blocked by |
|---|---|---|---|
| ~~T23~~ | Dropped: the set is one wave of 10 languages (T21). | | |
| ~~T24~~ | Dropped: no right-to-left language is in the set. | | |
| T25 | screenshot tests for the 8 densest screens (Roborazzi or `captureToImage`) | M | T21 |

### Phase 6 — Server text and measuring

| ID | Title | Size | Blocked by |
|---|---|---|---|
| T26 | client-built push text on Android and in the iOS extension (only if the payload carries all fields; otherwise a Server task that needs approval) | M | T21 |
| T27 | analytics: `app_language` user property and a GA4 report per language | S | T21 |

Total 27 tasks (S 4, M 20, L 3; T0, T23 and T24 dropped, T28 and T29 added); a rough sum is
30 to 40 worker days. Phase 3 is the long part; 4 parallel workers finish it in about 4 rounds.

## 13. Q10 — Measure

Take the baseline before T21 ships: the 28 days before the release, per platform. Compare with 28
days after the first translated release, for the new languages only, against the same languages' earlier numbers.

| Number | Source | Why |
|---|---|---|
| Active users per device language | GA4 `language` × `activeUsers` | is the audience in this language growing |
| New users per language and per country | GA4 acquisition; Play Console and App Store Connect by country | pairs with the store listing work (Tasks#1215, #1216) |
| Retention D1, D7, D30 per language | GA4 cohort exploration | a translated app should retain better than the same language in English |
| Rating and review text per country | Play Console and App Store Connect | catches wrong translations |
| Premium conversion and trial to paid per language | RevenueCat (`locale` attribute) + GA4 funnel events (spec 014) | revenue effect |
| Crash-free users and layout complaints per language | Firebase Crashlytics, support mail | overflow bugs |
| Missing-key and fallback rate | a debug log or event when a key falls back to English | translation coverage |

T27 adds the `app_language` user property so the funnel in spec 014 can be cut by language.

## 14. Decisions taken in this plan

- Key system: Compose resources for shared UI, native files for Android-only, Wear and iOS widgets (not
  one library for all).
- Move rule: English text is not edited during the move.
- Translation: model plus glossary first; Crowdin free plan plus a model for the first fill; no paid reviewer.
- Server text: no API change is proposed.

## 15. Open questions (for Caleb)

All answered by Caleb on 2026-11-20; see section 16.

## Appendix A — `count_strings.py`

```python
"""Count English UI string literals in KMP Kotlin sources. Run: python -I count_strings.py <repo-root>"""
import re, os, sys, collections
root = sys.argv[1]
LIT = r'"((?:[^"\\\n]|\\.)*[A-Za-z][^"\n]*?)"'
KEYS = r'(?:text|title|subtitle|headline|label|placeholder|supportingText|message|description|summary|hint|buttonText|actionLabel|confirmText|dismissText|contentDescription)'
RX = [re.compile(r'\bText\(\s*(?:text\s*=\s*)?' + LIT), re.compile(r'\b' + KEYS + r'\s*=\s*' + LIT)]
SKIP_DIRS = ('/build', '/openApi', '/.git', 'Test/', '/test/')
count = collections.Counter(); templ = collections.Counter(); feat = collections.defaultdict(collections.Counter)
for d, _, files in os.walk(root):
    dd = d.replace(chr(92), '/')
    if any(s in dd for s in SKIP_DIRS): continue
    for f in files:
        if not f.endswith('.kt'): continue
        rel = os.path.relpath(os.path.join(d, f), root).replace(chr(92), '/')
        if 'ebug' in rel: continue                      # debug screens are not translated
        m = re.match(r'(composeApp|wearApp)/src/([^/]+)/', rel)
        if not m: continue
        ss = m.group(1) + ':' + m.group(2)
        parts = rel.split('/')
        fe = parts[parts.index('ui') + 1] if 'ui' in parts and parts.index('ui') + 1 < len(parts) - 1 else parts[-2]
        txt = open(os.path.join(d, f), encoding='utf-8', errors='ignore').read()
        seen = set()
        for rx in RX:
            for mm in rx.finditer(txt):
                if mm.start(1) in seen: continue
                seen.add(mm.start(1))
                s = mm.group(1)
                if re.fullmatch(r'[a-z_]+', s) and not mm.group(0).startswith('Text'): continue   # identifiers
                count[ss] += 1; feat[ss][fe] += 1
                if '$' in s: templ[ss] += 1
print(dict(count), 'templates:', dict(templ))
for ss in feat: print(ss, feat[ss].most_common())
```

Result on `main` `e98c97e7`: `commonMain` 1,095 (63 templates), `androidMain` 19, `iosMain` 3,
`desktopMain` 2, `wearApp` 17.

## Appendix B — References (read 2026-11-19)

- Compose Multiplatform resources, strings, plurals, `getString`: <https://kotlinlang.org/docs/multiplatform/compose-multiplatform-resources-usage.html>
- Compose Multiplatform resource environment and in-app locale: <https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html>
- Android per-app language preferences: <https://developer.android.com/guide/topics/resources/app-languages>
- Apple localization and String Catalogs: <https://developer.apple.com/documentation/xcode/localization>
- Repo files named in this plan: `composeApp/build.gradle.kts`, `composeApp/src/androidMain/res/values/strings.xml`,
  `wearApp/src/main/res/values/strings.xml`, `composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/util/DateTimeUtil.kt`,
  `…/ui/compose/CountdownWidget.kt`, `composeApp/src/androidMain/kotlin/me/calebjones/spacelaunchnow/data/notifications/NotificationDisplayHelper.kt`,
  `iosApp/iosApp/Info.plist`, `iosApp/LaunchWidget/`, `iosApp/NotificationServiceExtension/NotificationService.swift`.
- Links in this list were not all opened by the author: the Compose pages were read; the Android and
  Apple pages are cited from memory and must be re-checked in task T1 and T8.

## 16. Caleb's rulings (chat, 2026-11-20)

1. App language picker: none. The app follows the device language and the OS per-app setting.
2. First translated release: waits until all shared screens are moved (T10 to T20), so no screen mixes
   two languages.
3. Build tasks: filed from this spec, T1 to T5 first.
4. Store listings: a listing gets a language when the app ships that language.
5. Store text: a model writes the first draft with the glossary; the text stays in git and out of
   Crowdin (the free plan holds 60,000 hosted words and the app is estimated at 50,000). No paid reviewer.
6. Store screenshots stay English.
7. Languages: the 10 in section 8.1. No right-to-left language. Russian is out.
8. Tools: Crowdin for the app text (project `948217`), a model for the first fill.

## Appendix C — Text-tree snapshot harness and plural check (task T1)

Built in Tasks#1238. Both live in `composeApp/src/desktopTest` and run with `:composeApp:desktopTest`.

**Snapshot harness**
- `i18n/TextTreeSnapshot.kt` dumps the semantics tree of a `runComposeUiTest` run: each node's text,
  content description and a `click` flag, indented by depth. It compares the dump with
  `composeApp/src/desktopTest/resources/snapshots/<name>.txt`.
- First snapshot: `about_screen.txt`, taken on the code before the move and still equal after it
  (`ui/about/AboutLibrariesScreenSnapshotTest`). The About screen now reads every text from
  `composeResources/values/strings.xml`.
- To add a screen: write a test like the About one, run it once with `I18N_SNAPSHOT_UPDATE=true` on the
  code before the move, commit the `.txt`, then move the strings and run it again without the variable.
  A change in any text or content description fails the test and shows both versions.
- Limits: a lazy list dumps only the rows it composes (the About list shows the first library only);
  the screen runs in the English default locale; it checks text, not layout or colour. The desktop
  `BuildConfig` gives `Version: unknown (0)`.
- To run it in another language later, set `Locale.setDefault` before `setContent` and store the file
  under another name (tasks T2 and T25).

**Plural check**
- `i18n/PluralCategoryTest.kt` reads `<plurals>` from test-only resources in
  `src/desktopTest/composeResources/values`, `values-uk` and `values-pl`. Each translated item shows
  its own category, so the test proves which one Compose picked: `uk` 1, 21 give `one`; 2 to 4, 22 give
  `few`; 0, 5 to 20, 25 give `many`. `pl` 1 gives `one`; 2 to 4, 22, 24 give `few`; 0, 5, 12, 21, 25 give
  `many`. English gives `one` and `other`.
- Result: Compose resources use the CLDR rules for both languages on desktop. Android runs the same
  common code; iOS is checked by the Xcode CI build. Fractions are not tested.
