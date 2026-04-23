# ADR-0002: Introduce a use-case layer for multi-source orchestration

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

The codebase has no `domain/usecase/` package. Multi-source orchestration lives inside ViewModels:

- `HomeViewModel` (792 lines) composes featured launch + next-24h count + next-week count + next-month count + today-in-history + latest updates.
- `StarshipViewModel` (809 lines) combines upcoming Starship launches + Starship history + program metadata.
- `ScheduleViewModel` (546 lines) combines filter state + upcoming/previous launches + pagination.

Consequences today:

- ViewModels can only be tested with fake repositories, not in isolation from UI state.
- Logic (e.g., "combine upcoming + previous with dedup") is duplicated between `HomeViewModel` and `ScheduleViewModel`.
- Refactoring a repository touches several ViewModels.

## Decision

Introduce `me.calebjones.spacelaunchnow.domain.usecase` containing single-responsibility interactors. Each use case:

- Exposes a single `operator fun invoke(...)` (suspend or `Flow`-returning).
- Depends only on repository interfaces and other use cases.
- Returns domain types.
- Is registered in a feature-scoped Koin module.

ViewModels compose use cases; they do not inject repositories directly once a feature's use cases exist.

New features must land with use cases. Existing ViewModels will be refactored opportunistically, prioritizing `HomeViewModel`, `StarshipViewModel`, and `ScheduleViewModel`.

## Consequences

### Positive

- Use cases are trivially unit-testable (fake repository → assert output).
- ViewModel size drops; responsibility narrows to UI state mapping.
- Logic reuse across screens (e.g., `GetTodayInHistoryUseCase`).
- Clearer layering for new contributors.

### Negative / Trade-offs

- Additional indirection for simple screens. Use cases are optional for pure pass-through (single repository call, no composition).
- More Koin registration boilerplate.

### Follow-up work

- Create `domain/usecase/` and migrate `HomeViewModel` as the reference implementation.
- Document a "when to add a use case" heuristic in `ARCHITECTURE_OVERVIEW.md`: required when a ViewModel calls >1 repository method or composes across repositories.

## Alternatives Considered

1. **Skip use cases; extract helpers into repository methods.** Rejected — bloats repositories further (see ADR-0003) and mixes data-source concerns with orchestration.
2. **Move orchestration into dedicated "facade" repositories.** Rejected — facades become new god objects and don't solve testability.

## References

- ADR-0003 (repository decomposition)
- [HomeViewModel.kt](../../../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/ui/viewmodel/HomeViewModel.kt)
