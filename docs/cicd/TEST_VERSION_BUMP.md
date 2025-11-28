# Test Version Bump Workflow - Quick Guide

## Purpose

This lightweight workflow tests the conventional commits → semver bumping logic **without running the full build pipeline**. It's fast (~30 seconds) and safe to run repeatedly.

---

## How to Use

### Option 1: Manual Trigger (Easiest)

1. Go to GitHub Actions tab
2. Click "Test Version Bumping" workflow
3. Click "Run workflow" button
4. Select branch: `main` (or any branch)
5. Click green "Run workflow" button
6. Wait ~30 seconds for results

### Option 2: Push to Test Branch

```bash
# Create test branch
git checkout -b test-version-bump

# Make a test commit with conventional format
git commit --allow-empty -m "feat: test version bumping"

# Push to trigger workflow
git push origin test-version-bump
```

---

## What It Tests

✅ **Conventional commit detection** - Checks if commits use proper format  
✅ **Semver calculation** - Verifies `feat:` = minor, `fix:` = patch, etc.  
✅ **Build number logic** - Tests reset vs increment behavior  
✅ **version.properties update** - Shows how file would be updated  
✅ **Action outputs** - Displays all data from conventional-changelog-action  

---

## Reading the Results

### Success Output Example

```
📜 Last 10 commits:
a1b2c3d feat: add new feature
e4f5g6h fix: resolve bug
...

📦 Current version.properties:
version=5.0.0
versionBuildNumber=28

🔍 Conventional Changelog Action Outputs:
  version: '5.1.0'              ← Action detected feat: and bumped minor
  skipped: 'false'              ← Conventional commits found ✅
  
📊 Version Calculation Summary:
  Previous Version: 5.0.0
  New Version:      5.1.0       ← Semver bump worked! ✅
  Previous Build:   b28
  New Build:        b1          ← Reset because version changed ✅
  Final Version:    5.1.0-b1
  Version Code:     5010001

✅ TEST COMPLETE
🎯 Final version: 5.1.0-b1
```

### Warning Output (No Conventional Commits)

```
⚠️  WARNING: Conventional changelog action skipped
This means commits since last release don't follow conventional format
Expected format: 'feat:', 'fix:', 'chore:', etc.

📊 Version Calculation Summary:
  Previous Version: 5.0.0
  New Version:      5.0.0       ← No change (no conventional commits)
  Previous Build:   b28
  New Build:        b29          ← Only build increments
  Final Version:    5.0.0-b29
```

---

## Test Scenarios

### Scenario 1: Test Feature Bump (Minor)

```bash
git commit --allow-empty -m "feat: add launch notifications"
# Expected: 5.0.0 → 5.1.0
```

### Scenario 2: Test Fix Bump (Patch)

```bash
git commit --allow-empty -m "fix: resolve widget crash"
# Expected: 5.0.0 → 5.0.1
```

### Scenario 3: Test Breaking Change (Major)

```bash
git commit --allow-empty -m "feat!: redesign API structure"
# Expected: 5.0.0 → 6.0.0
```

### Scenario 4: Test Multiple Commits

```bash
git commit --allow-empty -m "fix: patch bug 1"
git commit --allow-empty -m "fix: patch bug 2"
git commit --allow-empty -m "feat: add feature"
# Expected: 5.0.0 → 5.1.0 (highest bump wins)
```

### Scenario 5: Test Non-Conventional Commit

```bash
git commit --allow-empty -m "Update some files"
# Expected: No version bump, warning shown
```

---

## Advantages Over Full Workflow

| Test Workflow | Full Release Workflow |
|--------------|---------------------|
| ⚡ ~30 seconds | ⏱️ ~15 minutes |
| 🆓 Free (minimal CPU) | 💰 ~$0.50 per run |
| 🔁 Can run repeatedly | ⚠️ Should run only on merge |
| 📊 Shows debug info | 🏗️ Builds & deploys |
| ✅ Safe to experiment | ⚠️ Creates releases |

---

## Troubleshooting

### Issue: "skipped: 'true'"

**Problem:** No conventional commits detected  
**Solution:** Check commit messages use format: `type: description`  
**Valid types:** feat, fix, chore, docs, refactor, test, ci, perf, build, style

### Issue: "version: ''"

**Problem:** Action didn't output a version  
**Solution:** Check `version.properties` exists and has `version=` line  
**Fallback:** Workflow will use current version from file

### Issue: Version didn't bump

**Problem:** Commits don't follow conventional format  
**Solution:** Rewrite commits or make new ones with proper format:
```bash
git commit --amend -m "feat: your feature description"
# or
git commit --allow-empty -m "feat: test feature"
```

---

## After Testing

Once you verify the version bumping works correctly:

1. **Delete test branch** (if created):
   ```bash
   git push origin --delete test-version-bump
   ```

2. **Use proper conventional commits** on main:
   ```bash
   git commit -m "feat: add event detail page"
   git commit -m "fix: resolve notification crash"
   ```

3. **Push to main** - Full workflow will run with working version bumps

---

## Expected Behavior After Fix

### Before Fix (Broken)
```
All commits → 5.0.0-b28, 5.0.0-b29, 5.0.0-b30...
Only build number increments ❌
```

### After Fix (Working)
```
feat: commit  → 5.1.0-b1  ✅ Minor bump
fix: commit   → 5.1.1-b1  ✅ Patch bump
chore: commit → 5.1.1-b2  ✅ Same version, build increments
feat: commit  → 5.2.0-b1  ✅ Minor bump again
```

---

## Quick Reference

**Run Test:**
- GitHub UI: Actions → Test Version Bumping → Run workflow
- OR push to `test-version-bump` branch

**View Results:**
- Check workflow logs for version calculation
- Download artifacts to see updated `version.properties`

**Verify Success:**
- `version` field should change based on commit type
- `skipped: 'false'` means conventional commits detected
- Final version should follow pattern: `X.Y.Z-bN`

**Clean Up:**
- Test branch can be deleted after verification
- Artifacts auto-delete after 1 day

