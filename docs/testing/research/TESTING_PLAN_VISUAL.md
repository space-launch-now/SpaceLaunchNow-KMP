# Unit Testing Plan - Visual Overview

```
SpaceLaunchNow-KMP Testing Strategy
══════════════════════════════════════════════════════════════

📊 Current State
┌─────────────────────────────────────────────────────────┐
│ Source Files:        89                                  │
│ Test Files:           6                                  │
│ Coverage:          ~6.7%                                 │
│ Gap:              ~93% needs tests                       │
└─────────────────────────────────────────────────────────┘

🎯 Target State
┌─────────────────────────────────────────────────────────┐
│ Coverage Goal:     >80% for business logic              │
│ Utilities:         100% coverage                        │
│ Repositories:       85% coverage                        │
│ ViewModels:         85% coverage                        │
└─────────────────────────────────────────────────────────┘

📋 Documentation Structure
┌─────────────────────────────────────────────────────────┐
│                                                          │
│  docs/                                                   │
│  ├── TESTING_QUICK_REF.md ......... 30-second start     │
│  ├── TESTING_TASKS.md ............. Task tracking       │
│  ├── TESTING_GUIDE.md ............. Full guide          │
│  └── templates/                                          │
│      ├── README.md ................ Template guide      │
│      ├── BasicTestTemplate.kt ..... For utilities       │
│      ├── RepositoryTestTemplate.kt  For repos          │
│      └── ViewModelTestTemplate.kt   For ViewModels     │
│                                                          │
└─────────────────────────────────────────────────────────┘

🔄 Testing Phases

Phase 1: Utilities (Pure Functions)
┌──────────────────────────────────────────────────────────┐
│ Priority: CRITICAL ⚡                                     │
│ ✅ UserAgentUtil        (done)                           │
│ ✅ DateTimeUtil         (partial)                        │
│ ✅ EnvironmentManager   (done)                           │
│ ⬜ LaunchFormatUtil     15+ tests                        │
│ ⬜ StatusColorUtil       8+ tests                        │
│ ⬜ VideoUtil             6+ tests                        │
│ ⬜ BuildConfig           3+ tests                        │
│ ⬜ DateTimeLocal        10+ tests                        │
│                                                          │
│ Template: BasicTestTemplate.kt                          │
│ Dependencies: None                                       │
│ Can parallelize: YES ✓                                  │
└──────────────────────────────────────────────────────────┘

Phase 2: API Extensions
┌──────────────────────────────────────────────────────────┐
│ Priority: HIGH 🔥                                        │
│ ⬜ LaunchesApiExtensions  10+ tests                      │
│ ⬜ ArticlesApiExtensions   6+ tests                      │
│ ⬜ BlogsApiExtensions      6+ tests                      │
│                                                          │
│ Template: BasicTestTemplate.kt                          │
│ Dependencies: Light (API interfaces)                    │
│ Can parallelize: YES ✓                                  │
└──────────────────────────────────────────────────────────┘

Phase 3: Repositories (Business Logic)
┌──────────────────────────────────────────────────────────┐
│ Priority: CRITICAL ⚡                                     │
│ ⬜ LaunchRepositoryImpl      15+ tests                   │
│ ⬜ EventsRepositoryImpl      12+ tests                   │
│ ⬜ UpdatesRepositoryImpl     10+ tests                   │
│ ⬜ ArticlesRepositoryImpl    10+ tests                   │
│ ⬜ NotificationRepositoryImpl 12+ tests                  │
│ ⬜ SubscriptionProcessor      8+ tests                   │
│                                                          │
│ Template: RepositoryTestTemplate.kt                     │
│ Dependencies: API mocking required                      │
│ Can parallelize: YES ✓ (with mock setup)               │
└──────────────────────────────────────────────────────────┘

Phase 4: ViewModels (State Management)
┌──────────────────────────────────────────────────────────┐
│ Priority: CRITICAL ⚡                                     │
│ ⬜ NextUpViewModel       12+ tests                       │
│ ⬜ HomeViewModel         10+ tests                       │
│ ⬜ LaunchViewModel       12+ tests                       │
│ ⬜ EventViewModel        10+ tests                       │
│ ⬜ UpdatesViewModel      10+ tests                       │
│ ⬜ SettingsViewModel      8+ tests                       │
│ ⬜ DebugSettingsViewModel 6+ tests                       │
│                                                          │
│ Template: ViewModelTestTemplate.kt                      │
│ Dependencies: Repository mocks required                 │
│ Can parallelize: YES ✓ (after mock repos exist)        │
└──────────────────────────────────────────────────────────┘

Phase 5: Storage & Cache
┌──────────────────────────────────────────────────────────┐
│ Priority: MEDIUM 📝                                      │
│ ⬜ DataStoreProvider      8+ tests                       │
│ ⬜ Cache implementations 10+ tests                       │
│                                                          │
│ Template: BasicTestTemplate.kt                          │
│ Dependencies: Storage mocking                           │
│ Can parallelize: YES ✓                                  │
└──────────────────────────────────────────────────────────┘

Phase 6: Navigation & UI
┌──────────────────────────────────────────────────────────┐
│ Priority: LOW 📋                                         │
│ ⬜ Navigation logic       5+ tests                       │
│ ⬜ UI components       (future work)                     │
│                                                          │
│ Template: BasicTestTemplate.kt                          │
│ Dependencies: May need Compose test framework           │
│ Can parallelize: Partially                              │
└──────────────────────────────────────────────────────────┘

📈 Coverage Progression
═══════════════════════════════════════════════════════════

Current:    [█                    ] ~7%

Phase 1:    [███                  ] 15%
Phase 2:    [█████                ] 25%
Phase 3:    [██████████           ] 50%
Phase 4:    [███████████████      ] 75%
Phase 5+:   [████████████████     ] 80%+

🚀 Quick Start for Agents
═══════════════════════════════════════════════════════════

1. Pick a Task
   └─> Open docs/TESTING_TASKS.md
       └─> Find ⬜ (not started) task
           └─> Update to 🚧 (in progress)

2. Copy Template
   └─> docs/templates/[ComponentType]Template.kt
       └─> Copy to commonTest matching package structure

3. Write Tests
   └─> Follow docs/TESTING_GUIDE.md
       └─> Use docs/TESTING_QUICK_REF.md for quick lookups

4. Run Tests
   └─> ./gradlew :composeApp:jvmTest
       └─> Verify all pass

5. Commit
   └─> Use conventional commits (test: add tests for X)
       └─> Update TESTING_TASKS.md (🚧 → ✅)

🎯 Best First Tasks
═══════════════════════════════════════════════════════════

Easiest (5-10 tests, ~15 min):
├─> VideoUtil.kt
├─> BuildConfig.kt
└─> StatusColorUtil.kt

Medium (10-15 tests, ~30 min):
├─> LaunchFormatUtil.kt ⭐ RECOMMENDED
├─> DateTimeLocal.kt
└─> LaunchesApiExtensions.kt

Advanced (15+ tests, ~1 hour):
├─> LaunchRepositoryImpl.kt 🔥 HIGH IMPACT
├─> NextUpViewModel.kt 🔥 HIGH IMPACT
└─> EventsRepositoryImpl.kt

💡 Tips for Success
═══════════════════════════════════════════════════════════

✅ DO:
├─> Start with Phase 1 (utilities)
├─> Follow existing test patterns
├─> Test edge cases and null values
├─> Use descriptive test names
├─> Run tests frequently
└─> Update task list when done

❌ DON'T:
├─> Skip error path testing
├─> Test implementation details
├─> Make real API calls
├─> Forget to test null inputs
└─> Leave tests commented out

📞 Need Help?
═══════════════════════════════════════════════════════════

Check existing tests:
├─> UserAgentUtilTest.kt (simple utility)
├─> DateTimeUtilTimelineTest.kt (parsing)
└─> EnvironmentManagerTest.kt (config)

Read the docs:
├─> TESTING_QUICK_REF.md (fastest)
├─> TESTING_GUIDE.md (comprehensive)
└─> templates/README.md (template guide)

🎉 Success Metrics
═══════════════════════════════════════════════════════════

Minimum Viable:
└─> All Phase 1 + LaunchRepositoryImpl + NextUpViewModel

Good Coverage:
└─> Phases 1-3 complete (utilities + repos)

Excellent Coverage:
└─> Phases 1-4 complete (+ ViewModels)

Production Ready:
└─> 80%+ coverage across all business logic
```
