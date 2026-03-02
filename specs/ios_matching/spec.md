# iOS Code Signing with Fastlane Match Specification

**Branch**: `dev`  
**Date**: 2026-03-02  
**Author**: Copilot Agent

## Overview

Replace automatic code signing with fastlane Match to centrally manage certificates and provisioning profiles in a private GitHub repository. This eliminates certificate pollution in App Store Connect and ensures consistent signing across CI runs.

### Problem Statement

Current automatic signing + App Store Connect API on fresh CI runners leads to:
- New certificate creation on every build (runners lack existing private keys)
- Orphaned certificates accumulating in App Store Connect
- Manual cleanup required after each build
- "Revoke certificate" and "No profiles found" errors

### Solution

Implement fastlane Match to:
- Store certificates/profiles encrypted in a private git repository
- Sync signing assets consistently across all CI runs
- Eliminate certificate creation during builds
- Provide single source of truth for code signing

## Requirements

### Functional Requirements

1. **FR-1**: Store Distribution certificate and provisioning profiles in encrypted private repository
2. **FR-2**: CI workflow must sync certificates from Match before building
3. **FR-3**: Support all 3 targets requiring signing:
   - Main app: `me.spacelaunchnow.spacelaunchnow`
   - NotificationServiceExtension: `me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension`
   - LaunchWidgetExtension: `me.spacelaunchnow.spacelaunchnow.LaunchWidget`
4. **FR-4**: Maintain local development workflow with automatic signing for Debug builds
5. **FR-5**: Support certificate rotation without workflow changes

### Non-Functional Requirements

1. **NFR-1**: No certificate creation during CI builds
2. **NFR-2**: Build time impact < 2 minutes for certificate sync
3. **NFR-3**: Match repo must be private with encrypted storage
4. **NFR-4**: Existing TestFlight upload flow must continue working

## Architecture

### Match Storage

| Component | Location |
|-----------|----------|
| **Certificates Repo** | `git@github.com:space-launch-now/ios-certificates.git` (private) |
| **Encryption** | Match password stored as `MATCH_PASSWORD` secret |
| **Access** | SSH deploy key stored as `MATCH_GIT_PRIVATE_KEY` secret |

### Target Configuration

| Target | Bundle ID | Profile Name |
|--------|-----------|--------------|
| **Main App** | `me.spacelaunchnow.spacelaunchnow` | `match AppStore me.spacelaunchnow.spacelaunchnow` |
| **NotificationServiceExtension** | `me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension` | `match AppStore me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension` |
| **LaunchWidgetExtension** | `me.spacelaunchnow.spacelaunchnow.LaunchWidget` | `match AppStore me.spacelaunchnow.spacelaunchnow.LaunchWidget` |

### Entitlements Per Target

| Target | Capabilities |
|--------|-------------|
| **Main App** | Push Notifications, App Groups |
| **NotificationServiceExtension** | App Groups |
| **LaunchWidgetExtension** | App Groups |

**App Groups:**
- `group.me.calebjones.spacelaunchnow` (legacy)
- `group.me.spacelaunchnow.spacelaunchnow` (current)

## Implementation Plan

### Phase 1: Repository Setup (Manual)

1. Create private repository `space-launch-now/ios-certificates`
2. Generate SSH deploy key with read/write access
3. Add GitHub secrets to main repository:
   - `MATCH_GIT_URL`: `git@github.com:space-launch-now/ios-certificates.git`
   - `MATCH_PASSWORD`: Strong encryption password
   - `MATCH_GIT_PRIVATE_KEY`: SSH private key for repo access

### Phase 2: Fastlane Configuration

**Create `iosApp/fastlane/Matchfile`:**

```ruby
git_url(ENV["MATCH_GIT_URL"])
storage_mode("git")
type("appstore")
readonly(true)

app_identifier([
  "me.spacelaunchnow.spacelaunchnow",
  "me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension",
  "me.spacelaunchnow.spacelaunchnow.LaunchWidget"
])

team_id(ENV["APPLE_TEAM_ID"])
```


**Create `iosApp/fastlane/Fastfile`:**

```ruby
default_platform(:ios)

platform :ios do
  desc "Sync code signing certificates"
  lane :sync_certificates do
    setup_ci if ENV["CI"]
    
    api_key = app_store_connect_api_key(
      key_id: ENV["APPLE_API_KEY_ID"],
      issuer_id: ENV["APPLE_API_ISSUER_ID"],
      key_content: ENV["APPLE_API_KEY_CONTENT"],
      is_key_content_base64: true
    )
    
    match(
      type: "appstore",
      app_identifier: [
        "me.spacelaunchnow.spacelaunchnow",
        "me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension",
        "me.spacelaunchnow.spacelaunchnow.LaunchWidget"
      ],
      readonly: is_ci,
      api_key: api_key
    )
  end

  desc "Build release archive"
  lane :build_release do
    sync_certificates
    
    build_app(
      project: "iosApp.xcodeproj",
      scheme: "iosApp",
      configuration: "Release",
      export_method: "app-store",
      output_directory: ENV["RUNNER_TEMP"] || "./build",
      output_name: "SpaceLaunchNow.ipa",
      export_options: {
        signingStyle: "manual",
        provisioningProfiles: {
          "me.spacelaunchnow.spacelaunchnow" => "match AppStore me.spacelaunchnow.spacelaunchnow",
          "me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension" => "match AppStore me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension",
          "me.spacelaunchnow.spacelaunchnow.LaunchWidget" => "match AppStore me.spacelaunchnow.spacelaunchnow.LaunchWidget"
        }
      }
    )
  end
end
```

**Create `iosApp/Gemfile`:**

```ruby
source "https://rubygems.org"

gem "fastlane", "~> 2.220"
```

### Phase 3: Xcode Project Updates

Modify `iosApp/iosApp.xcodeproj/project.pbxproj` for Release configurations:

| Setting | Debug | Release |
|---------|-------|---------|
| `CODE_SIGN_STYLE` | Automatic | Manual |
| `PROVISIONING_PROFILE_SPECIFIER` | (empty) | Match profile name |

**Main App Release:**
```
CODE_SIGN_STYLE = Manual
PROVISIONING_PROFILE_SPECIFIER = "match AppStore me.spacelaunchnow.spacelaunchnow"
```

**NotificationServiceExtension Release:**
```
CODE_SIGN_STYLE = Manual
PROVISIONING_PROFILE_SPECIFIER = "match AppStore me.spacelaunchnow.spacelaunchnow.NotificationServiceExtension"
```

**LaunchWidgetExtension Release:**
```
CODE_SIGN_STYLE = Manual
PROVISIONING_PROFILE_SPECIFIER = "match AppStore me.spacelaunchnow.spacelaunchnow.LaunchWidget"
```

### Phase 4: CI Workflow Updates

Update `.github/workflows/release-ios.yml`:

```yaml
# Replace certificate import step with:
- name: Setup SSH for Match
  run: |
    mkdir -p ~/.ssh
    echo "${{ secrets.MATCH_GIT_PRIVATE_KEY }}" > ~/.ssh/match_key
    chmod 600 ~/.ssh/match_key
    ssh-keyscan github.com >> ~/.ssh/known_hosts
    
    cat >> ~/.ssh/config << EOF
    Host github.com
      IdentityFile ~/.ssh/match_key
      IdentitiesOnly yes
    EOF

- name: Install Fastlane
  run: |
    cd iosApp
    bundle install

- name: Build iOS with Fastlane
  env:
    MATCH_GIT_URL: ${{ secrets.MATCH_GIT_URL }}
    MATCH_PASSWORD: ${{ secrets.MATCH_PASSWORD }}
    APPLE_TEAM_ID: ${{ secrets.APPLE_TEAM_ID }}
    APPLE_API_KEY_ID: ${{ secrets.APPLE_API_KEY_ID }}
    APPLE_API_ISSUER_ID: ${{ secrets.APPLE_API_ISSUER_ID }}
    APPLE_API_KEY_CONTENT: ${{ secrets.APPLE_API_KEY_CONTENT }}
  run: |
    cd iosApp
    bundle exec fastlane build_release
```

### Phase 5: Initialize Match (One-Time)

Run locally to generate certificates:

```bash
cd iosApp

# Set environment variables
export MATCH_GIT_URL="git@github.com:space-launch-now/ios-certificates.git"
export MATCH_PASSWORD="your-strong-password"
export APPLE_TEAM_ID="<YOUR_APPLE_TEAM_ID>"

# Clean up App Store Connect first
# (Manually revoke orphaned certificates in App Store Connect portal)

# Generate and upload certificates
bundle exec fastlane match appstore
```

## GitHub Secrets

### New Secrets Required

| Secret | Description | Example |
|--------|-------------|---------|
| `MATCH_GIT_URL` | SSH URL to certificates repo | `git@github.com:space-launch-now/ios-certificates.git` |
| `MATCH_PASSWORD` | Encryption passphrase | Strong random string |
| `MATCH_GIT_PRIVATE_KEY` | SSH private key for repo | `-----BEGIN OPENSSH PRIVATE KEY-----...` |

### Existing Secrets (Keep)

| Secret | Still Used |
|--------|------------|
| `APPLE_TEAM_ID` | ✅ Yes |
| `APPLE_API_KEY_ID` | ✅ Yes (TestFlight upload) |
| `APPLE_API_ISSUER_ID` | ✅ Yes (TestFlight upload) |
| `APPLE_API_KEY_CONTENT` | ✅ Yes (TestFlight upload) |
| `APPLE_CERTIFICATE_BASE64` | ❌ Remove (Match handles this) |
| `APPLE_CERTIFICATE_PASSWORD` | ❌ Remove (Match handles this) |

## Verification

### Pre-Merge Checklist

- [ ] Private certificates repo created and accessible
- [ ] `fastlane match appstore --readonly` succeeds locally
- [ ] All 3 provisioning profiles visible in Match repo
- [ ] Xcode project updated for manual Release signing
- [ ] CI workflow updated to use fastlane

### Post-Merge Verification

- [ ] Trigger iOS workflow with `artifacts-only` option
- [ ] Build completes without certificate errors
- [ ] No new certificates appear in App Store Connect
- [ ] IPA contains correct embedded profiles for all 3 targets
- [ ] TestFlight upload succeeds

## Rollback Plan

If Match implementation fails:

1. Revert workflow to previous automatic signing
2. Restore `APPLE_CERTIFICATE_BASE64` and `APPLE_CERTIFICATE_PASSWORD` secrets
3. Delete Match-related secrets and certificates repo
4. Revert Xcode project to automatic signing

## File Changes Summary

| File | Action |
|------|--------|
| `iosApp/fastlane/Matchfile` | CREATE |
| `iosApp/fastlane/Fastfile` | CREATE |
| `iosApp/Gemfile` | CREATE |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | MODIFY (Manual signing for Release) |
| `.github/workflows/release-ios.yml` | MODIFY (Replace cert import with fastlane) |
| `docs/cicd/IOS_MATCH_SETUP.md` | CREATE (Documentation) |

## References

- [fastlane Match Documentation](https://docs.fastlane.tools/actions/match/)
- [App Store Connect API](https://developer.apple.com/documentation/appstoreconnectapi)
- [Code Signing Guide](https://developer.apple.com/support/code-signing/)
