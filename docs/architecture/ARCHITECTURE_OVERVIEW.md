# Architecture Overview

> **Status:** Living document. Captures the target architecture and the known gaps between it and the current codebase.
> **Last updated:** 2026-04-21

## Purpose

SpaceLaunchNow is a Kotlin Multiplatform Compose application (Android, iOS, Desktop, Wear OS) that consumes The Space Devs Launch Library (LL2) and SNAPI APIs. The codebase has grown organically and now shows symptoms of architectural debt that are worth documenting so that future feature work can converge on a single target state.

## Target Architecture

```
┌───────────────────────────────────────────────────────────────┐
│  UI (Compose screens + ViewModels)                            │
│  - Screens consume domain types only                          │
│  - ViewModels orchestrate use cases                           │
└──────────────────────────┬────────────────────────────────────┘
                           │ domain types
┌──────────────────────────▼────────────────────────────────────┐
│  Domain layer                                                 │
│  - domain/model/   → @Immutable data classes (Launch, Event…) │
│  - domain/usecase/ → Single-responsibility interactors        │
│  - domain/mapper/  → API → domain conversion (the only layer  │
│                      allowed to import api.*.models)          │
└──────────────────────────┬────────────────────────────────────┘
                           │ domain types
┌──────────────────────────▼────────────────────────────────────┐
│  Repository layer                                             │
│  - Returns Result<RepositoryResult<T>> (domain types only)    │
│  - Owns stale-while-revalidate / cache policy                 │
└────────────┬─────────────────────────────────┬────────────────┘
             │                                 │
┌────────────▼──────────────┐      ┌───────────▼────────────────┐
│ LocalDataSource (SQLDel.) │      │ Generated API client       │
│ - Stores domain JSON      │      │ - me.calebjones…api.*      │
│ - schema_version column   │      │ - OpenAPI extension fns    │
└───────────────────────────┘      └────────────────────────────┘
```

**Golden rules:**

1. `me.calebjones.spacelaunchnow.api.*.models.*` may be imported **only** by `api/extensions/` and `domain/mapper/`.
2. `LocalDataSource` serializes **domain** types, never API types. Each cached blob has a schema version.
3. ViewModels depend on use cases or repositories; they never call mappers or API extensions directly.
4. No single repository, ViewModel, or Composable exceeds ~500 lines without an ADR exception.

## Current State vs Target

| Concern                        | Target                                         | Current                                                      | Gap ADR |
| ------------------------------ | ---------------------------------------------- | ------------------------------------------------------------ | ------- |
| Domain model layer             | Complete, UI only sees domain types            | In progress ([domain-model-layer/tasks.md](../../specs/dev/domain-model-layer/tasks.md)); VMs still call `.toDomain()` | [ADR-0001](adr/0001-domain-boundary.md) |
| Use-case layer                 | `domain/usecase/` per feature                  | None                                                         | [ADR-0002](adr/0002-usecase-layer.md) |
| Repository size                | ≤500 lines, split by concern                   | `LaunchRepositoryImpl` = 1,426 lines; `ScheduleFilterRepositoryImpl` = 787 | [ADR-0003](adr/0003-repository-decomposition.md) |
| Cache schema coupling          | Domain JSON + `schema_version`                 | API JSON stored directly; spec bumps silently invalidate DB  | [ADR-0004](adr/0004-cache-schema-versioning.md) |
| Modularization                 | `:data`, `:domain`, `:feature-*`, `:api-*`     | Single `:composeApp` module                                  | [ADR-0005](adr/0005-modularization.md) |
| Repository return type         | `Flow<RepositoryResult<T>>` (single wrapper)   | `Result<DataResult<PaginatedResult<T>>>` (triple-nested)     | [ADR-0006](adr/0006-repository-result-type.md) |
| ViewModel decomposition        | One VM per screen, ≤400 lines                  | 29 VMs with overlapping ownership; `HomeViewModel` = 792     | Tracked in backlog |
| Screen Composable size         | ≤500 lines per file                            | `DebugSettingsScreen` 1,909, `SupportUsScreen` 1,710, `ThemeCustomizationScreen` 1,167 | Tracked in backlog |
| DI module decomposition        | Per-feature Koin modules                       | Single 390-line `AppModule.kt`                               | Follows ADR-0005 |

## Known Architectural Debt (summary)

1. **API types leak everywhere.** 60+ files import `me.calebjones.spacelaunchnow.api.launchlibrary.models`. The domain layer exists but the UI and cache tiers still depend on generated types.
2. **God repositories.** `LaunchRepositoryImpl` handles upcoming, previous, featured, in-flight, starship, history, detail, filtered, day-and-month, stats, and agency-detail — with inlined stale-while-revalidate.
3. **No use-case layer.** Multi-source orchestration (e.g., `HomeViewModel` composing featured + 24h + week + month + today-in-history + updates) lives in ViewModels.
4. **Cache-to-API coupling.** LocalDataSource classes deserialize API types straight from SQLDelight blobs. An API spec evolution = silent cache corruption.
5. **Dual OpenAPI specs, no module boundary.** `openapi-config-ll.yaml` and `openapi-config-snapi.yaml` both generate into `:composeApp`, and every API regen recompiles the world.
6. **Triple-nested result wrapper.** `Result<DataResult<PaginatedResult<T>>>` requires three unwraps at every call site.
7. **Monolithic UI files.** Several screens exceed 1,000 lines of Compose code.
8. **Overlapping ViewModels.** `LaunchesViewModel`, `LaunchViewModel`, `LaunchCarouselViewModel`, `NextUpViewModel`, `FeaturedLaunchViewModel` have unclear ownership boundaries.

## Related Documentation

- [LAUNCH_DETAIL_ARCHITECTURE.md](LAUNCH_DETAIL_ARCHITECTURE.md) — Launch detail screen composition
- [DEBUG_MENU_SECURITY.md](DEBUG_MENU_SECURITY.md) — Debug menu gating
- [DATA_PERSISTENCE.md](../DATA_PERSISTENCE.md) — Preferences and persistence contract
- [specs/dev/domain-model-layer/](../../specs/dev/domain-model-layer/) — In-flight domain layer migration
- Caching architecture notes — see repo memory `caching-architecture.md`

## How to use ADRs

New architectural decisions must be recorded as an ADR under `docs/architecture/adr/` using the numbered format `NNNN-kebab-case-title.md`. Status values: `Proposed`, `Accepted`, `Superseded by ADR-XXXX`, `Deprecated`. Do not edit an accepted ADR — supersede it with a new one.
