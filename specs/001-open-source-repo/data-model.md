# Data Model: Open Source Repository Preparation

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02

## Entity: Security Finding

Tracks each security issue discovered during the audit.

| Field | Type | Description |
|-------|------|-------------|
| id | string | Unique identifier (e.g., "SEC-001") |
| severity | enum | CRITICAL, HIGH, MEDIUM, LOW |
| category | string | Type of finding (secret, workflow, config, missing-file) |
| file_path | string | Relative path to affected file |
| line_numbers | int[] | Affected line numbers |
| description | string | What was found |
| recommendation | string | What action to take |
| status | enum | OPEN, IN_PROGRESS, RESOLVED, ACCEPTED_RISK |

## Remediation Checklist

### CRITICAL — Must resolve before making public

| ID | File | Finding | Action | Status |
|----|------|---------|--------|--------|
| SEC-001 | `docs/cicd/DATADOG_CICD_SECRETS.md` | Real Datadog client token `pub7e50...` and app ID `349dbfe5...` hardcoded | Replace with `<YOUR_DATADOG_CLIENT_TOKEN>` and `<YOUR_DATADOG_APP_ID>` placeholders | RESOLVED |
| SEC-002 | `.github/workflows/release-main.yml` | `Print google-services.json` steps dump Firebase secrets to CI logs (lines 213-214, 354-355) | Remove both `Print google-services.json` steps; keep MD5 hash step | RESOLVED |
| SEC-003 | `.github/workflows/backup-secrets.yml` | Entire workflow dumps ALL GitHub Secrets to downloadable artifact | Delete the entire workflow file | RESOLVED |

### HIGH — Should resolve before making public

| ID | File | Finding | Action | Status |
|----|------|---------|--------|--------|
| SEC-004 | `composeApp/src/commonMain/.../DebugUnlock.kt` | Default TOTP secret `<REMOVED_DEFAULT_TOTP_SECRET>` hardcoded (line 33) | Replace with empty string or null; disable debug menu if no secret configured | RESOLVED |
| SEC-005 | `composeApp/build.gradle.kts` | TOTP fallback `<REMOVED_DEFAULT_TOTP_SECRET>` (line 330) | Change fallback to empty string `""` | RESOLVED |
| SEC-006 | `composeApp/src/iosMain/.../AppSecrets.kt` | TOTP fallback `<REMOVED_DEFAULT_TOTP_SECRET>` (line 19) | Change fallback to empty string `""` | RESOLVED |
| SEC-007 | `scripts/generate-ios-secrets.sh` | TOTP fallback `<REMOVED_DEFAULT_TOTP_SECRET>` (line 101) | Change fallback to empty string `""` | RESOLVED |
| SEC-008 | `docs/architecture/DEBUG_MENU_SECURITY.md` | Documents the default secret `<REMOVED_DEFAULT_TOTP_SECRET>` (line 272) | Replace with `<YOUR_TOTP_SECRET>` placeholder | RESOLVED |
| SEC-009 | `iosApp/fastlane/Fastfile` | Apple Team ID fallback `4T4QRN2U5X` (line 74) | Remove `\|\| "4T4QRN2U5X"` fallback | RESOLVED |
| SEC-010 | `iosApp/fastlane/Matchfile` | Apple Team ID fallback `4T4QRN2U5X` (line 12) | Remove `\|\| "4T4QRN2U5X"` fallback | RESOLVED |

### MEDIUM — Recommended improvements

| ID | File | Finding | Action | Status |
|----|------|---------|--------|--------|
| SEC-011 | `.gitignore` | Missing patterns for certificate/keystore files | Add `*.jks`, `*.keystore`, `*.p12`, `*.pem`, `*.key`, `service-account.json` | RESOLVED |
| SEC-012 | (root) | Missing LICENSE file | Create GPL-3.0 LICENSE file | RESOLVED |
| SEC-013 | (root) | Missing CONTRIBUTING.md | Create contributor guidelines | RESOLVED |
| SEC-014 | (root) | Missing CODE_OF_CONDUCT.md | Create Contributor Covenant v2.1 | RESOLVED |
| SEC-015 | (root) | Missing SECURITY.md | Create vulnerability reporting policy | RESOLVED |

### LOW / ACCEPTED RISK

| ID | File | Finding | Action | Status |
|----|------|---------|--------|--------|
| SEC-016 | `iosApp/iosApp/Info.plist` | AdMob App ID hardcoded | ACCEPTED — IDs are public in published apps | ACCEPTED_RISK |
| SEC-017 | `iosApp/iosApp.xcodeproj/project.pbxproj` | Apple Team ID hardcoded | ACCEPTED — required by Xcode, semi-public | ACCEPTED_RISK |
| SEC-018 | `iosApp/Configuration/Config.xcconfig` | Apple Team ID hardcoded | ACCEPTED — required for build config | ACCEPTED_RISK |

## State Transitions

```
OPEN → IN_PROGRESS → RESOLVED
OPEN → ACCEPTED_RISK (with documented justification)
```

## Validation Rules

- All CRITICAL findings MUST be RESOLVED before repository visibility change
- All HIGH findings SHOULD be RESOLVED before repository visibility change
- ACCEPTED_RISK requires documented justification in this file
- Post-remediation: re-run `grep` searches to verify no secrets remain
