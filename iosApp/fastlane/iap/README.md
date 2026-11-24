# App Store purchase texts

Display texts for the subscriptions and the in-app purchase, in git (Tasks#1269). This folder is outside `fastlane/metadata/`, so the listing upload never touches it.

## Hold: nothing goes to App Store Connect yet

No localised text goes to App Store Connect before Tasks#1259 closes and Caleb approves the first live run. Until then, run only the check, the tests, `download` and the dry run.

Changed localizations go to App Review. A live upload can start a review of the purchase texts.

## Layout

```
iosApp/fastlane/iap/
  _group/<locale>.json              {"name": "..."}              subscription group display name
  base_plan/<locale>.json           {"name": "...", "description": "..."}   monthly subscription
  yearly/<locale>.json              {"name": "...", "description": "..."}   yearly subscription
  spacelaunchnow_pro/<locale>.json  {"name": "...", "description": "..."}   lifetime purchase
```

The folder names match the product ids. `_group` may also hold an optional `customAppName` (the app name shown in the subscription list). Locales use the App Store Connect codes, the same as the folders in `fastlane/metadata`: `cs`, `de-DE`, `es-ES`, `fr-FR`, `it`, `nl-NL`, `pl`, `pt-BR`, `uk`, `zh-Hans`. `en-US` is allowed. Never `ru`.

There is no `en-US` file for `base_plan` or `yearly`. Their live English text is not in git yet; `download` shows it.

## Limits

| Field | Limit |
|---|---|
| `name` (product and group) | 30 characters |
| `description` | 45 characters |

Counted in Unicode code points. No prices and no review notes: this repo is public.

Style: informal "you", and "All Access" stays in English in every name (see `specs/017-app-text-translation/glossary.md`). The words Monthly, Yearly and Lifetime are translated.

## Steps

All scripts are in `scripts/appstore-iap/`. They need Node 22 and no npm packages.

| Step | Command | Needs credentials |
|---|---|---|
| Check the files | `node scripts/appstore-iap/check-iap-texts.mjs` | no |
| Unit tests | `node --test scripts/appstore-iap/` | no |
| Download the live texts | `node scripts/appstore-iap/download-iap.mjs [--out <folder>] [--check-against-git]` | yes |
| Dry run (prints create, update, unchanged) | `node scripts/appstore-iap/upload-iap.mjs` | yes |
| Live upload | `IAP_UPLOAD_APPROVED=yes node scripts/appstore-iap/upload-iap.mjs --live` | yes |

Credentials are the env vars `APPLE_API_KEY_ID`, `APPLE_API_ISSUER_ID` and `APPLE_API_KEY_CONTENT` (base64 of the `.p8`, a raw PEM also works). Same as `iosApp/fastlane/Fastfile`.

- `download` writes to `iosApp/fastlane/iap_downloaded` by default. With `--check-against-git` it exits 1 on any difference. Differences are normal before the first upload.
- The upload creates missing localizations and updates changed ones. It never deletes. A locale that exists only in App Store Connect is left alone.
- `--live` stops unless `IAP_UPLOAD_APPROVED=yes` is set.

## Workflow

`.github/workflows/ios-iap-texts.yml`:

- On a pull request or push that touches this folder or the scripts: check and tests.
- By hand (`workflow_dispatch`), input `mode`: `download` (the folder is an artifact), `upload-dry-run`, or `upload-live`. `upload-live` also needs the `approved` box ticked. Nothing uploads on push.
