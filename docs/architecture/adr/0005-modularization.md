# ADR-0005: Modularize the Gradle project

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

The project currently ships with two real modules:

- `:composeApp` — everything (API clients, repositories, domain, UI, DI, database, widgets, notifications)
- `:wearApp` — Wear OS app

Two OpenAPI specs are generated into `:composeApp`:

- `composeApp/openapi-config-ll.yaml` (Launch Library 2.4.0)
- `composeApp/openapi-config-snapi.yaml` (Space News API)

`AppModule.kt` is 390 lines and registers every dependency in the app. Any API regeneration recompiles the whole `:composeApp` module including UI. Build-cache invalidation is maximally pessimistic.

The app is also large enough (29 ViewModels, 40+ repositories, multiple feature areas) that team ownership boundaries are not expressible in code.

## Decision

Adopt a layered, feature-sliced module structure. Target shape:

```
:api-launchlibrary   — generated LL2 client + extensions
:api-snapi           — generated SNAPI client + extensions
:data                — repositories, LocalDataSources, SQLDelight schemas
:domain              — domain/model/, domain/mapper/, domain/usecase/
:core-ui             — theme, common Composables, icons
:core-analytics      — analytics abstractions
:core-billing        — RevenueCat / billing
:feature-home
:feature-schedule
:feature-detail
:feature-news-events
:feature-settings
:feature-onboarding
:composeApp          — app shell + DI composition root
:wearApp             — Wear OS (already separate)
```

Each `:feature-*` module owns its ViewModels, screens, and Koin module. The app module wires feature Koin modules together.

Migration is incremental:

1. Extract `:domain` first (lowest coupling, highest reuse).
2. Extract `:api-*` next (generated code is self-contained).
3. Extract `:data`.
4. Extract features one at a time, starting with the smallest (`:feature-onboarding` or `:feature-settings`).

## Consequences

### Positive

- API regeneration recompiles only `:api-*`.
- Feature modules compile in parallel.
- Module boundaries enforce ADR-0001 (API types physically unavailable outside allowed modules).
- Team ownership becomes expressible via `CODEOWNERS` per module.
- Enables Baseline Profile per feature and per-module test scope.

### Negative / Trade-offs

- Non-trivial migration effort (weeks, not days).
- Gradle configuration-time cost grows (mitigated by `org.gradle.configuration-cache`).
- Cross-module navigation requires an abstraction (sealed `Screen` hierarchy lifted into `:core-ui` or dedicated `:navigation` module).

### Follow-up work

- Spike: extract `:domain` behind a feature flag; measure build-time delta.
- Document module conventions (naming, public API surface via `api()` vs `implementation()`).
- Define per-feature Koin module convention (each `:feature-*` exports a `featureXxxModule: Module`).
- Revisit after `:domain` extraction to decide the order of remaining work.

## Alternatives Considered

1. **Package-by-feature within `:composeApp`.** Already partially done (`ui/home/`, `ui/schedule/`). Doesn't fix build scaling or enforce import boundaries.
2. **One giant module split (all features at once).** Rejected — too risky, too slow, too many merge conflicts.
3. **Keep monomodule, use Detekt for boundary enforcement.** Acceptable short-term mitigation for ADR-0001 but doesn't address build-time or ownership.

## References

- `mcp_android_scalability_guide` — modularization topic
- ADR-0001 (domain boundary)
- `gradle/libs.versions.toml` (centralized catalog, already modularization-friendly)
