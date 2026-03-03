# Implementation Plan: Open Source Repository Preparation

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/001-open-source-repo/spec.md`

## Summary

Prepare SpaceLaunchNow-KMP for public visibility by conducting a security audit, removing exposed secrets, hardening `.gitignore`, removing dangerous CI/CD workflows, and adding required open-source community files (LICENSE, CONTRIBUTING.md, CODE_OF_CONDUCT.md, SECURITY.md).

## Technical Context

**Language/Version**: Kotlin Multiplatform (JDK 21), Swift 5.9, Gradle  
**Primary Dependencies**: Compose Multiplatform, Ktor, Koin, Firebase, RevenueCat, Datadog, AdMob  
**Storage**: N/A (repository configuration only)  
**Testing**: Gradle test task (`./gradlew test`)  
**Target Platform**: Android, iOS, Desktop (JVM)  
**Project Type**: Mobile (KMP)  
**Performance Goals**: N/A (no runtime changes)  
**Constraints**: CI/CD pipeline must remain functional; no breaking changes to build process  
**Scale/Scope**: ~50 files affected (config, docs, workflows, community files)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. Mobile-First Development | **PASS** | No runtime changes; both platforms unaffected |
| II. Pattern-Based Consistency | **PASS** | No code pattern changes |
| III. Accessibility & UX | **N/A** | No UI changes |
| IV. CI/CD & Conventional Commits | **PASS** | Workflow changes improve security; pipeline remains functional |
| V. Code Generation & API Management | **PASS** | No generated code changes |
| VI. Multiplatform Architecture | **PASS** | No architecture changes |
| VII. Testing Standards | **PASS** | Build/test pipeline verified after changes |

**Gate Result**: PASS — No violations. This feature is infrastructure/security-focused and does not affect runtime code patterns.

## Project Structure

### Documentation (this feature)

```text
specs/001-open-source-repo/
├── plan.md              # This file
├── research.md          # Phase 0: Security audit findings
├── data-model.md        # Phase 1: Remediation checklist data model
├── quickstart.md        # Phase 1: Developer onboarding for secrets setup
├── contracts/           # Phase 1: Community file templates
│   ├── LICENSE.md
│   ├── CONTRIBUTING.md
│   ├── CODE_OF_CONDUCT.md
│   └── SECURITY.md
└── tasks.md             # Phase 2 output (NOT created by /speckit.plan)
```

### Affected Files (repository root)

```text
# Files to MODIFY
.gitignore                                    # Add missing patterns
.github/workflows/release-main.yml            # Remove secret-printing steps
docs/cicd/DATADOG_CICD_SECRETS.md             # Replace real tokens with placeholders
docs/architecture/DEBUG_MENU_SECURITY.md      # Remove hardcoded TOTP default reference
composeApp/build.gradle.kts                   # Remove TOTP fallback default
composeApp/src/commonMain/.../DebugUnlock.kt  # Remove hardcoded TOTP secret
composeApp/src/iosMain/.../AppSecrets.kt      # Remove TOTP fallback
scripts/generate-ios-secrets.sh               # Remove TOTP fallback
iosApp/fastlane/Fastfile                      # Remove Team ID fallback
iosApp/fastlane/Matchfile                     # Remove Team ID fallback

# Files to DELETE
.github/workflows/backup-secrets.yml          # Dangerous secret-dumping workflow

# Files to CREATE (at repo root)
LICENSE                                       # Apache 2.0 or GPL-3.0 (user choice)
CONTRIBUTING.md                               # Contributor guidelines
CODE_OF_CONDUCT.md                            # Community standards
SECURITY.md                                   # Vulnerability reporting
```

**Structure Decision**: This is an infrastructure/security feature that modifies existing files and adds community files at the repository root. No new source directories needed.

## Complexity Tracking

No constitution violations — section not applicable.
