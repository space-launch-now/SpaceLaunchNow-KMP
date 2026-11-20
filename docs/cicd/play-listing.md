# Google Play store listing

The listing text is in git: `fastlane/metadata/android/<locale>/` holds `title.txt`, `short_description.txt` and `full_description.txt`. Phone and Wear OS share one `applicationId`, so they share one listing. Release notes are separate (`docs/release-notes/whatsnew/`, sent by `release-main.yml`). Screenshots and graphics stay in Play Console.

Limits (Play): title 30, short description 80, full description 4000 characters. Each file must end without a newline or space, because fastlane sends the bytes as they are.

## Change the text

1. Edit the files. Add a language as a new folder with a Play locale code (`es-419`, `pt-BR`, `de-DE`).
2. `python3 scripts/check-play-listing.py` checks the limits, the three files per locale and that `en-US` exists. The `Play Store Listing` workflow runs it on every pull request that touches the folder.
3. Merge.

## Upload

Run the `Play Store Listing` workflow by hand (Actions, `workflow_dispatch`). Leave `validate_only` on first: Google checks the edit and nothing is published. Turn it off to publish. It uses the `PLAY_CONSOLE_SERVICE_ACCOUNT_JSON` secret that `release-main.yml` already uses.

Locally: put the service account key at `service-account.json` (git-ignored) and run `bundle exec fastlane android play_listing validate_only:true` from the repo root.

The first upload replaces whatever is in Play Console for each locale folder. Compare Play Console with the files before the first real upload.

## Compare before the first upload

Run the `Play Store Listing` workflow by hand with `diff_only` on. It downloads the live title and descriptions (`fastlane supply init` into a temp folder, read only, the edit is never committed), compares them with `fastlane/metadata/android/`, and prints `no diff` or the diff. The job fails when text differs. It changes no file and uploads nothing. Locally: `bundle exec fastlane android play_listing_diff`, or `python3 scripts/diff-play-listing.py <folder>` on a folder you downloaded.

If the diff shows text that Play holds and git lacks, copy it into the files first, then upload.
