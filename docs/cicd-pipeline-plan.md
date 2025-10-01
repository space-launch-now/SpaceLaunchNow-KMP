# CI/CD Pipeline Plan for Space Launch Now KMP

## Overview

This document outlines the complete CI/CD pipeline strategy for the Space Launch Now Kotlin
Multiplatform project. The pipeline will automatically build, test, sign, and deploy Android
releases to the Google Play Console Alpha track when changes are merged to the master branch.

## Version Management

The app uses a semantic versioning scheme: `major.minor.patch-buildNumber`

- Version properties are stored in `version.properties`
- Version code is calculated as:
  `(major * 1000000) + (minor * 100000) + (patch * 10000) + buildNumber`
- Starting version: 4.0.0-b1

## Pipeline Architecture

### 1. Branch Strategy

- **master**: Production branch, triggers automatic deployment to Play Console Alpha
- **develop**: Development branch for feature integration
- **feature/\***: Feature branches for individual development

### 2. Pipeline Triggers

#### Pull Request Pipeline (`pr-validation.yml`)

- **Triggers**: On PR to master or develop
- **Purpose**: Validate code quality and prevent broken builds

#### Main Pipeline (`deploy-android.yml`)

- **Triggers**: On push/merge to master
- **Purpose**: Build, sign, and deploy to Play Console Alpha

#### Release Pipeline (`release-production.yml`)

- **Triggers**: Manual trigger or version tag (v*.*.*)
- **Purpose**: Promote from Alpha to Production

## Required Secrets and Setup

### GitHub Secrets

1. **KEYSTORE_BASE64**: Base64 encoded Android release keystore
2. **KEYSTORE_PASSWORD**: Password for the keystore
3. **KEY_ALIAS**: Alias for the signing key
4. **KEY_PASSWORD**: Password for the key
5. **PLAY_CONSOLE_SERVICE_ACCOUNT_JSON**: Service account JSON for Play Console API
6. **API_KEY**: The Space Devs API key
7. **FIREBASE_GOOGLE_SERVICES_JSON**: Base64 encoded google-services.json

### Google Play Console Setup

1. Create a service account in Google Cloud Console
2. Enable Google Play Android Developer API
3. Grant the service account permissions in Play Console:
    - Release Manager
    - Production Manager (for later promotion)

## Pipeline Implementation

### Phase 1: PR Validation Pipeline

```yaml
# .github/workflows/pr-validation.yml
name: PR Validation

on:
  pull_request:
    branches: [ master, develop ]

jobs:
  validate:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Setup JDK 21
      - Setup Gradle cache
      - Create .env file with API_KEY
      - Run ktlint check
      - Run unit tests
      - Build debug APK
      - Upload test reports
```

### Phase 2: Android Deploy Pipeline

```yaml
# .github/workflows/deploy-android.yml
name: Deploy to Play Console Alpha

on:
  push:
    branches: [ master ]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - Checkout code
      - Setup JDK 21
      - Auto-increment build number in version.properties
      - Create .env file
      - Decode and setup signing configuration
      - Build release APK/AAB
      - Sign the bundle
      - Upload to Play Console Alpha track
      - Create GitHub release with artifacts
      - Notify team via Slack/Discord (optional)
```

### Phase 3: Production Release Pipeline

```yaml
# .github/workflows/release-production.yml
name: Promote to Production

on:
  workflow_dispatch:
    inputs:
      version:
        description: 'Version to promote'
        required: true

jobs:
  promote:
    runs-on: ubuntu-latest
    steps:
      - Promote Alpha to Beta (optional)
      - Promote to Production track
      - Create GitHub release tag
      - Generate release notes
```

## Detailed Step Implementations

### 1. Version Management

```gradle
// Auto-increment build number on master push
task incrementBuildNumber {
    doLast {
        def props = new Properties()
        file("version.properties").withInputStream { props.load(it) }
        def buildNumber = props.getProperty("versionBuildNumber").toInteger()
        props.setProperty("versionBuildNumber", (buildNumber + 1).toString())
        file("version.properties").withOutputStream { props.store(it, null) }
    }
}
```

### 2. Signing Configuration

```gradle
android {
    signingConfigs {
        release {
            if (System.getenv("CI") == "true") {
                storeFile file(System.getenv("KEYSTORE_FILE"))
                storePassword System.getenv("KEYSTORE_PASSWORD")
                keyAlias System.getenv("KEY_ALIAS")
                keyPassword System.getenv("KEY_PASSWORD")
            }
        }
    }
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### 3. Build Artifacts

- **APK**: For testing and distribution outside Play Store
- **AAB (Android App Bundle)**: Required for Play Console upload
- **Mapping files**: For crash reporting (if using R8/ProGuard)

## Testing Strategy

### Unit Tests

- Run on every PR and master push
- Test ViewModels, repositories, and business logic
- Minimum 70% code coverage target

### UI Tests

- Compose UI tests for critical user flows
- Run on PR to master
- Screenshot testing for visual regression

### Integration Tests

- API integration tests with mock server
- Database migration tests
- Run on master push before deployment

## Monitoring and Rollback

### Success Criteria

- Build passes all tests
- Successful upload to Play Console
- No critical crashes in first 24 hours

### Rollback Strategy

1. Halt rollout in Play Console
2. Fix critical issues
3. Increment patch version
4. Deploy hotfix through pipeline

## Implementation Timeline

### Week 1

- [ ] Set up GitHub secrets
- [ ] Implement PR validation workflow
- [ ] Configure signing and keystore

### Week 2

- [ ] Implement deploy-android workflow
- [ ] Test Alpha track deployment
- [ ] Set up version auto-increment

### Week 3

- [ ] Add comprehensive testing
- [ ] Implement production promotion workflow
- [ ] Documentation and team training

### Week 4

- [ ] Monitor initial deployments
- [ ] Fine-tune pipeline performance
- [ ] Add advanced features (release notes, notifications)

## Best Practices

1. **Never commit sensitive data** - Use GitHub secrets
2. **Cache dependencies** - Speed up builds with Gradle cache
3. **Parallel jobs** - Run tests in parallel when possible
4. **Artifact retention** - Keep APKs/AABs for 30 days
5. **Branch protection** - Require PR reviews before master merge
6. **Semantic commits** - Use conventional commits for auto-changelog
7. **Release notes** - Auto-generate from commit messages

## Advanced Features (Future)

1. **Multi-track deployment** - Internal → Alpha → Beta → Production
2. **A/B testing** - Staged rollouts with monitoring
3. **Crash reporting integration** - Auto-halt on crash spike
4. **Performance monitoring** - Track app startup time, ANRs
5. **iOS deployment** - Extend to TestFlight/App Store
6. **Desktop distribution** - GitHub releases for desktop builds

## Security Considerations

1. **Rotate service account keys** quarterly
2. **Audit GitHub secret access** monthly
3. **Use least-privilege principle** for service accounts
4. **Enable 2FA** for all team members
5. **Review third-party action updates** before upgrading

## Troubleshooting Guide

### Common Issues

1. **Build fails with signing error**
    - Verify keystore is correctly base64 encoded
    - Check key alias matches

2. **Play Console upload fails**
    - Verify service account permissions
    - Check version code is incremented

3. **Tests timeout**
    - Increase timeout limits
    - Check for flaky tests

4. **Out of memory errors**
    - Increase Gradle heap size
    - Use gradle build cache

## Appendix: Complete Workflow Files

The complete workflow files will be created in the `.github/workflows/` directory:

- `pr-validation.yml` - PR validation and testing
- `deploy-android.yml` - Master branch deployment
- `release-production.yml` - Production promotion
- `version-bump.yml` - Automated version management

---

*Last Updated: December 2024*
*Version: 1.0.0*