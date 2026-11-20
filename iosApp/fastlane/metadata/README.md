# App Store listing text

One folder per App Store Connect locale (`en-US`, `de-DE`, `ja`, ...). One file per field:

| File | Limit | Changes without a new version |
|---|---|---|
| `name.txt` | 30 characters | no |
| `subtitle.txt` | 30 characters | no |
| `description.txt` | 4000 characters | no |
| `keywords.txt` | 100 bytes, comma-separated, no spaces | no |
| `promotional_text.txt` | 170 characters | yes |

A missing file leaves that field as it is in App Store Connect.

- Check: `node scripts/check-ios-metadata.mjs [--strict]`
- Upload: merge to `main` (workflow `ios-metadata.yml`), or `bundle exec fastlane upload_metadata` from `iosApp/`. Text only: no binary, screenshots or review submission.
- `en-US` `name` and `description` came from the public App Store page (2026-11-20). Subtitle, keywords and promotional text are not public: add them from App Store Connect.
