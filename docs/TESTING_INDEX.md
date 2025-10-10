# Testing Documentation Index

Central hub for all unit testing documentation in SpaceLaunchNow-KMP.

## 🚀 Quick Start (Pick One)

### I want to start testing NOW! (30 seconds)
→ **[TESTING_QUICK_REF.md](TESTING_QUICK_REF.md)**
- Template code to copy-paste
- Common assertions
- Run commands

### I want to pick a specific task
→ **[TESTING_TASKS.md](TESTING_TASKS.md)**
- Complete task breakdown
- Priority levels
- Effort estimates
- Status tracking

### I want to understand the approach
→ **[TESTING_PLAN_VISUAL.md](TESTING_PLAN_VISUAL.md)**
- Visual overview
- Phase breakdown
- Coverage progression
- Success metrics

### I need detailed examples
→ **[TESTING_GUIDE.md](TESTING_GUIDE.md)**
- Comprehensive patterns
- Component-specific guides
- Best practices
- Full code examples

### I need a template to copy
→ **[templates/](templates/)**
- BasicTestTemplate.kt
- RepositoryTestTemplate.kt
- ViewModelTestTemplate.kt

## 📋 Document Purpose Guide

| Document | Use When You... | Reading Time |
|----------|-----------------|--------------|
| [TESTING_QUICK_REF.md](TESTING_QUICK_REF.md) | Want to start coding tests immediately | 2 min |
| [TESTING_TASKS.md](TESTING_TASKS.md) | Need to find available work | 5 min |
| [TESTING_PLAN_VISUAL.md](TESTING_PLAN_VISUAL.md) | Want to see the big picture | 3 min |
| [TESTING_GUIDE.md](TESTING_GUIDE.md) | Need in-depth examples and patterns | 15 min |
| [templates/](templates/) | Want ready-to-use code templates | 1 min |

## 🎯 Common Workflows

### New Contributor Workflow
```
1. Read TESTING_PLAN_VISUAL.md (overview)
   ↓
2. Check TESTING_TASKS.md (find easy task)
   ↓
3. Copy from templates/ (get started)
   ↓
4. Reference TESTING_QUICK_REF.md (while coding)
   ↓
5. Check TESTING_GUIDE.md (when stuck)
```

### Experienced Developer Workflow
```
1. Open TESTING_TASKS.md (pick task)
   ↓
2. Copy template from templates/
   ↓
3. Write tests (use QUICK_REF for lookups)
   ↓
4. Run tests, commit, update status
```

### Task Leader Workflow
```
1. Review TESTING_TASKS.md (see progress)
   ↓
2. Assign work to team members
   ↓
3. Track completion in task list
   ↓
4. Monitor coverage metrics
```

## 📊 Testing Coverage Overview

### Current State
- **89** source files in commonMain
- **6** test files exist
- **~6.7%** coverage
- **~93%** gap to fill

### Target State
- **>80%** coverage for business logic
- **100%** coverage for utilities
- **85%** coverage for repositories
- **85%** coverage for ViewModels

## 🗺️ Testing Phases Map

```
Phase 1: Utilities        [CRITICAL] ⚡ → Start here!
├─ 8 files to test
├─ No dependencies
└─ Can parallelize

Phase 2: API Extensions   [HIGH] 🔥
├─ 3 files to test
├─ Light dependencies
└─ Can parallelize

Phase 3: Repositories     [CRITICAL] ⚡ → High impact!
├─ 6 files to test
├─ Requires mocking
└─ Can parallelize

Phase 4: ViewModels       [CRITICAL] ⚡ → High impact!
├─ 7 files to test
├─ Requires repo mocks
└─ Can parallelize

Phase 5: Storage/Cache    [MEDIUM] 📝
├─ 2+ files to test
├─ Requires storage mocks
└─ Can parallelize

Phase 6: Navigation/UI    [LOW] 📋
├─ 5+ files to test
├─ May need Compose tests
└─ Partially parallel
```

## 🎓 Learning Path

### Beginner Path
1. Read [TESTING_QUICK_REF.md](TESTING_QUICK_REF.md)
2. Copy [BasicTestTemplate.kt](templates/BasicTestTemplate.kt)
3. Pick simple task: VideoUtil or StatusColorUtil
4. Reference QUICK_REF while coding

### Intermediate Path
1. Read [TESTING_GUIDE.md](TESTING_GUIDE.md) utility section
2. Pick LaunchFormatUtil or DateTimeLocal
3. Use GUIDE for detailed patterns
4. Try a repository test next

### Advanced Path
1. Study [RepositoryTestTemplate.kt](templates/RepositoryTestTemplate.kt)
2. Pick LaunchRepositoryImpl
3. Create mock API implementations
4. Move on to ViewModels

## 🔧 Essential Commands

```bash
# Run all tests
./gradlew :composeApp:allTests

# Run common tests (fastest)
./gradlew :composeApp:jvmTest

# Run specific test file
./gradlew :composeApp:jvmTest --tests "*LaunchFormatUtilTest"

# Run with info logging
./gradlew :composeApp:jvmTest --info
```

## 📚 Additional Resources

### Project Documentation
- [README.md](../README.md) - Main project documentation
- [CI/CD Pipeline](CICD_PIPELINE.md) - Automated deployment
- [Conventional Commits](CONVENTIONAL_COMMITS.md) - Commit message format

### External Resources
- [Kotlin Test Documentation](https://kotlinlang.org/api/latest/kotlin.test/)
- [Kotlinx Coroutines Test](https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-test/)
- [Ktor Client Mock](https://ktor.io/docs/http-client-testing.html)

## 🤝 Contributing

### To Add Tests
1. Pick task from [TESTING_TASKS.md](TESTING_TASKS.md)
2. Update status to 🚧 (in progress)
3. Follow patterns in [TESTING_GUIDE.md](TESTING_GUIDE.md)
4. Write tests using [templates](templates/)
5. Run tests to verify
6. Update status to ✅ (complete)
7. Commit with conventional format

### Commit Message Format
```
test(component): brief description

Examples:
test(util): add comprehensive tests for LaunchFormatUtil
test(repository): add tests for LaunchRepositoryImpl
test(viewmodel): add tests for NextUpViewModel state management
```

## ❓ FAQ

**Q: Where do I start?**
A: Read [TESTING_QUICK_REF.md](TESTING_QUICK_REF.md), then pick a task from [TESTING_TASKS.md](TESTING_TASKS.md)

**Q: Which files are easiest to test?**
A: Phase 1 utilities: VideoUtil, StatusColorUtil, BuildConfig

**Q: What if I get stuck?**
A: Check [TESTING_GUIDE.md](TESTING_GUIDE.md) examples or look at existing tests

**Q: How do I run tests?**
A: `./gradlew :composeApp:jvmTest` (see TESTING_QUICK_REF.md for more)

**Q: Can I work on multiple tasks?**
A: Yes! Update TESTING_TASKS.md status for each

**Q: What's the minimum viable coverage?**
A: All Phase 1 + LaunchRepositoryImpl + NextUpViewModel

## 📞 Getting Help

1. **Check existing tests** in `composeApp/src/commonTest/`
2. **Read the guide** in [TESTING_GUIDE.md](TESTING_GUIDE.md)
3. **Look at templates** in [templates/](templates/)
4. **Review examples** in TESTING_QUICK_REF.md

## 🎯 Success Checklist

For each component you test:
- [ ] Happy path test (normal operation)
- [ ] Null input test
- [ ] Empty value test
- [ ] Error handling test
- [ ] Edge case tests
- [ ] All tests pass locally
- [ ] Task list updated
- [ ] Conventional commit message

## 🌟 Final Notes

This testing initiative aims to:
- ✅ Increase code quality
- ✅ Catch bugs early
- ✅ Enable confident refactoring
- ✅ Document expected behavior
- ✅ Make onboarding easier

Every test you write helps achieve these goals. Thank you for contributing!

---

**Last Updated**: October 2025
**Current Coverage**: ~6.7%
**Target Coverage**: >80%
**Active Contributors**: Check TESTING_TASKS.md

For questions or suggestions about testing documentation, open an issue or PR.
