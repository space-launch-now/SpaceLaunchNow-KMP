# Conventional Commits Guide for Space Launch Now

This project uses [Conventional Commits](https://www.conventionalcommits.org/) to automatically determine version bumps and generate changelogs.

## Commit Message Format

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

## Types and Version Bumping

### 🔴 BREAKING CHANGE - Major Version Bump (x.0.0)
Use when making incompatible API changes:
```bash
feat!: redesign launch detail API
# OR
feat: redesign launch detail API

BREAKING CHANGE: Launch detail now returns LaunchDetailed instead of LaunchNormal
```

**Example:** `4.0.0` → `5.0.0`

### 🟢 feat - Minor Version Bump (0.x.0)
Use for new features:
```bash
feat: add event detail page
feat(ui): implement tablet layout for launch list
feat(notifications): add Firebase push notifications
feat(widget): create Android widget for upcoming launches
```

**Example:** `4.0.0` → `4.1.0`

### 🟡 fix - Patch Version Bump (0.0.x)
Use for bug fixes:
```bash
fix: correct launch time formatting
fix(ui): resolve crash on empty launch list
fix(api): handle null launch window
```

**Example:** `4.0.0` → `4.0.1`

### ⚪ Other Types - Patch Version Bump (0.0.x)
Other types also increment patch version by default:
- `chore`: Build scripts, dependencies, tooling
- `docs`: Documentation changes
- `style`: Code formatting (no functionality change)
- `refactor`: Code restructuring (no functionality change)
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `ci`: CI/CD configuration changes

```bash
chore: update dependencies
docs: update README with setup instructions
refactor: simplify launch repository logic
perf: optimize launch list rendering
test: add tests for launch detail viewmodel
ci: update GitHub Actions workflow
```

## Scopes (Optional but Recommended)

Use scopes to indicate which part of the app is affected:
- `ui`: User interface changes
- `api`: API client or repository changes
- `notifications`: Firebase notifications
- `widget`: Android widget
- `core`: Core application logic
- `home`: Home page
- `list`: Launch list page
- `detail`: Detail pages

## Examples

### Adding a New Feature
```bash
git commit -m "feat(home): add featured launch section with hero image"
```

### Fixing a Bug
```bash
git commit -m "fix(detail): prevent crash when launch location is null"
```

### Breaking Change with Scope
```bash
git commit -m "feat(api)!: migrate to Launch Library 2.4.0

BREAKING CHANGE: All repository methods now return Result<T> wrapper instead of throwing exceptions"
```

### Multiple Changes in One Commit (Not Recommended)
```bash
git commit -m "feat(ui): implement tablet layout and add dark mode support

- Added responsive layout detection
- Created tablet-optimized launch list
- Implemented dark theme for all screens"
```

## Version Bumping Rules

The CI/CD pipeline analyzes commit messages since the last tag:

1. **Any commit with `BREAKING CHANGE` or `!`** → Major bump (x.0.0)
2. **Any commit starting with `feat:`** → Minor bump (0.x.0)
3. **Any commit starting with `fix:`** → Patch bump (0.0.x)
4. **Any other commit** → Patch bump (0.0.x) by default

The **build number** always increments: `4.0.0-b1` → `4.0.0-b2`

## Changelog Generation

The pipeline automatically generates changelog entries:

### Breaking Changes Section
```markdown
### 💥 Breaking Changes
- 💥 feat(api)!: migrate to Launch Library 2.4.0 (a1b2c3d)
```

### Features Section
```markdown
### ✨ New Features
- ✨ add featured launch section (a1b2c3d)
- ✨ implement tablet layout for launch list (e4f5g6h)
```

### Bug Fixes Section
```markdown
### 🐛 Bug Fixes
- 🐛 prevent crash when launch location is null (i7j8k9l)
- 🐛 correct launch time formatting (m1n2o3p)
```

## Best Practices

1. **Write clear, descriptive commit messages**
   - ❌ `fix: bug`
   - ✅ `fix(detail): prevent crash when launch pad is null`

2. **Use one commit per logical change**
   - Makes it easier to understand history
   - Simplifies reverting changes if needed

3. **Use scopes consistently**
   - Helps understand which areas are changing most
   - Makes changelog more organized

4. **Include BREAKING CHANGE in footer for major changes**
   - Clearly documents what broke and why
   - Helps users understand migration path

5. **Use `[skip ci]` to avoid unnecessary builds**
   - Example: `docs: update README [skip ci]`
   - Pipeline already adds this to version bump commits

## CI/CD Workflow

### Pull Requests (PR Validation)
- ✅ Runs tests
- ✅ Builds debug APK
- ✅ Validates code quality
- ❌ Does NOT bump version
- ❌ Does NOT deploy

### Merges to Master (Master Deploy)
1. **Analyze commits** since last tag
2. **Bump version** based on conventional commits
3. **Generate changelog** with categorized changes
4. **Run tests** to ensure quality
5. **Build signed release** APK and AAB
6. **Deploy to Firebase Distribution** for testers
7. **Create GitHub Release** with artifacts and notes
8. **Update CHANGELOG.md** with new version

## Example Workflow

```bash
# Start working on a feature
git checkout -b feature/event-detail-page

# Make changes and commit with conventional format
git add .
git commit -m "feat(detail): implement event detail page with timeline"

# Fix a bug during development
git add .
git commit -m "fix(detail): handle null event description gracefully"

# Add tests
git add .
git commit -m "test(detail): add unit tests for event detail viewmodel"

# Create PR - triggers validation workflow
gh pr create --title "feat: Event Detail Page" --body "Implements the event detail page as specified in MVP"

# After PR approval and merge to master:
# - CI analyzes: "feat" commit → Minor version bump
# - Bumps: 4.0.0-b5 → 4.1.0-b6
# - Generates changelog with features and fixes
# - Builds signed release
# - Deploys to Firebase Distribution
# - Creates GitHub Release v4.1.0-b6
```

## Troubleshooting

### Version didn't bump correctly?
- Check commit messages follow conventional format
- Verify commits are between last tag and HEAD
- Review the "Bump version based on conventional commits" step in Actions

### Changelog missing entries?
- Ensure commit messages start with valid type (feat, fix, etc.)
- Check that commits are after the last git tag
- Review the "Generate changelog" step output in Actions

### Need to skip CI?
Add `[skip ci]` to commit message:
```bash
git commit -m "docs: fix typo in README [skip ci]"
```

## References

- [Conventional Commits Specification](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [Keep a Changelog](https://keepachangelog.com/)
