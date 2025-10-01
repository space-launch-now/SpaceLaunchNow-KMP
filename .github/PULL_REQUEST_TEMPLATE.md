## Description
<!-- Provide a brief description of the changes in this PR -->

## Type of Change
<!-- Mark the relevant option with an "x" -->

- [ ] 🐛 Bug fix (fix: ...) - Patch version bump
- [ ] ✨ New feature (feat: ...) - Minor version bump
- [ ] 💥 Breaking change (feat!: ...) - Major version bump
- [ ] 🔧 Chore (chore: ...) - Patch version bump
- [ ] 📝 Documentation (docs: ...) - Patch version bump
- [ ] 🎨 Style/Refactor (style/refactor: ...) - Patch version bump
- [ ] ✅ Tests (test: ...) - Patch version bump

## Checklist
<!-- Mark completed items with an "x" -->

- [ ] My commits follow the [Conventional Commits](../docs/CONVENTIONAL_COMMITS.md) format
- [ ] I have tested my changes locally
- [ ] I have added tests that prove my fix/feature works
- [ ] New and existing tests pass locally
- [ ] My code follows the project's style guidelines
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have updated the documentation (if needed)

## Conventional Commit Format

All commits should follow this format:
```
<type>(<scope>): <subject>

Examples:
feat: add event detail page
feat(ui): implement tablet layout
fix: resolve crash on null launch
fix(detail): handle missing launch pad
chore: update dependencies
docs: update README
```

**Version Bumping:**
- `feat!:` or `BREAKING CHANGE` → Major version (5.0.0)
- `feat:` → Minor version (4.1.0)
- `fix:`, `chore:`, etc. → Patch version (4.0.1)

See [Conventional Commits Guide](../docs/CONVENTIONAL_COMMITS.md) for details.

## Related Issues
<!-- Link related issues here -->
Closes #

## Screenshots (if applicable)
<!-- Add screenshots here -->

## Additional Notes
<!-- Any additional information reviewers should know -->

---

**Note:** When this PR is merged to master, the CI/CD pipeline will:
1. ✅ Analyze commits and bump version
2. ✅ Generate changelog
3. ✅ Run tests
4. ✅ Build signed release
5. ✅ Deploy to Firebase Distribution
6. ✅ Create GitHub Release

See [CI/CD Pipeline](../docs/CICD_PIPELINE.md) for details.
