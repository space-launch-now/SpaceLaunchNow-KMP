# Feature Spec: Open Source Repository Preparation

**Branch**: `001-open-source-repo` | **Date**: 2026-03-02

## Overview

Prepare the SpaceLaunchNow-KMP repository for public visibility by conducting a comprehensive security audit, removing sensitive data, adding open-source community files, and ensuring the repository is safe for public consumption.

## Goals

1. **Security Audit**: Identify and remediate all secrets, API keys, credentials, and sensitive configuration exposed in the repository history or working tree
2. **Sensitive File Cleanup**: Remove/gitignore any files containing credentials, tokens, signing keys, or private configuration
3. **Community Files**: Add standard open-source files (LICENSE, CONTRIBUTING.md, CODE_OF_CONDUCT.md, etc.)
4. **Documentation Review**: Ensure README and docs are appropriate for public audiences
5. **.gitignore Hardening**: Ensure all sensitive file patterns are properly ignored
6. **CI/CD Secrets Review**: Verify GitHub Actions workflows don't leak secrets and use proper secret management

## Non-Goals

- Rewriting git history (too risky; just ensure current state is clean)
- Changing application architecture
- Adding new features

## Requirements

### Functional Requirements

1. No API keys, passwords, tokens, or signing credentials shall be present in tracked files
2. `.env` files, keystore files, and Google services config must be gitignored
3. Repository must have a LICENSE file
4. Repository must have CONTRIBUTING.md with guidelines
5. Repository must have a CODE_OF_CONDUCT.md
6. README must be updated for public audience
7. CI/CD workflows must use GitHub Secrets exclusively for sensitive values
8. Firebase configuration files must not expose sensitive project IDs unnecessarily

### Non-Functional Requirements

1. Existing CI/CD pipeline must continue working after changes
2. Developer onboarding documentation must explain how to configure local secrets
3. No breaking changes to build process
