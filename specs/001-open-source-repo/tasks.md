# Tasks: Open Source Repository Preparation

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02

## Phase 1: Security Remediation — CRITICAL

> Sequential tasks. Must complete before Phase 2.

- [X] **T-001**: Replace real Datadog tokens in `docs/cicd/DATADOG_CICD_SECRETS.md` with placeholders `<YOUR_DATADOG_CLIENT_TOKEN>` and `<YOUR_DATADOG_APP_ID>` (SEC-001)
- [X] **T-002**: Remove `Print google-services.json` steps from `.github/workflows/release-main.yml` lines 213-214 and 354-355 (SEC-002)
- [X] **T-003**: Delete `.github/workflows/backup-secrets.yml` entirely (SEC-003)

## Phase 2: Security Remediation — HIGH

> Sequential tasks. Must complete before Phase 3.

- [X] **T-004**: Remove TOTP default `<REMOVED_DEFAULT_TOTP_SECRET>` from `composeApp/src/commonMain/.../DebugUnlock.kt`; disable debug menu when no secret configured (SEC-004)
- [X] **T-005**: Change TOTP fallback to empty string `""` in `composeApp/build.gradle.kts` (SEC-005)
- [X] **T-006**: Change TOTP fallback to empty string `""` in `composeApp/src/iosMain/.../AppSecrets.kt` (SEC-006)
- [X] **T-007**: Change TOTP fallback to empty string `""` in `scripts/generate-ios-secrets.sh` (SEC-007)
- [X] **T-008**: Replace documented TOTP secret with `<YOUR_TOTP_SECRET>` in `docs/architecture/DEBUG_MENU_SECURITY.md` (SEC-008)
- [X] **T-009**: Remove Apple Team ID fallback `|| "4T4QRN2U5X"` from `iosApp/fastlane/Fastfile` line 74 (SEC-009)
- [X] **T-010**: Remove Apple Team ID fallback `|| "4T4QRN2U5X"` from `iosApp/fastlane/Matchfile` line 12 (SEC-010)

## Phase 3: Repository Hardening

> Parallel tasks [P].

- [X] **T-011** [P]: Add missing patterns to `.gitignore` — `*.jks`, `*.keystore`, `*.p12`, `*.pem`, `*.key`, `service-account.json`, `secrets-backup.txt`, `secrets-inventory.txt` (SEC-011)
- [X] **T-012** [P]: Create `.env.example` template at repo root with placeholder values
- [X] **T-013** [P]: Verify `.gitignore` completeness and project setup ignore files

## Phase 4: Community Files

> Parallel tasks [P].

- [X] **T-014** [P]: Create `LICENSE` file with GPL-3.0 full text (SEC-012)
- [X] **T-015** [P]: Create `CONTRIBUTING.md` with contributor guidelines (SEC-013)
- [X] **T-016** [P]: Create `CODE_OF_CONDUCT.md` with Contributor Covenant v2.1 (SEC-014)
- [X] **T-017** [P]: Create `SECURITY.md` with vulnerability reporting policy (SEC-015)


## Phase 5: Validation

> Sequential tasks.

- [X] **T-018**: Run grep searches to verify no remaining secrets (`<REMOVED_DEFAULT_TOTP_SECRET>`, Datadog tokens)
- [X] **T-019**: Update `data-model.md` remediation checklist to mark all items RESOLVED
- [ ] **T-020**: Verify build still compiles with `./gradlew compileKotlinDesktop`
