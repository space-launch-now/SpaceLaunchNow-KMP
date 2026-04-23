# ADR-0003: Decompose god repositories by concern

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

`LaunchRepositoryImpl` is 1,426 lines and owns:

- Upcoming, previous, featured, in-flight, starship, history, detail, filtered, day-and-month launches
- Stats counts
- Agency details
- In-memory + SQLDelight cache coordination
- Stale-while-revalidate policy (hand-coded per method)

`ScheduleFilterRepositoryImpl` is 787 lines with similar breadth on the filter side. The interfaces are already dual (`xxx` and `xxxDomain`) because of the in-flight domain migration.

This makes every change a risk multiplier and discourages adding tests.

## Decision

Split `LaunchRepository` along natural seams:

| New repository              | Responsibility                                           |
| --------------------------- | -------------------------------------------------------- |
| `LaunchListRepository`      | Upcoming, previous, filtered, day-and-month              |
| `LaunchDetailRepository`    | Single launch by id, agency detail                       |
| `LaunchStatsRepository`     | Counts (24h, week, month, stats endpoint)                |
| `LaunchHistoryRepository`   | Today-in-history, historical queries                     |
| `StarshipLaunchRepository`  | Starship upcoming + history (separate program cadence)   |
| `FeaturedLaunchRepository`  | Featured + in-flight (home-screen hero)                  |

Extract cache coordination into a reusable `CachePolicy<T>` / `NetworkBoundResource<T>` helper (see ADR-0006) so each new repository is ~150–300 lines of mapping + delegation.

Similar split for `ScheduleFilterRepositoryImpl`: `ScheduleFilterStateRepository`, `ScheduleFilterOptionsRepository`.

## Consequences

### Positive

- Each repository has a narrow, testable contract.
- Stale-while-revalidate logic written once in `CachePolicy`.
- Reduces merge conflicts on hot repositories.

### Negative / Trade-offs

- More Koin registrations and interfaces.
- Some call sites inject two repositories instead of one (acceptable; often a use case replaces them — see ADR-0002).

### Follow-up work

- Design `CachePolicy` / `NetworkBoundResource` helper — must support: network-first, cache-first, stale-while-revalidate, and return `RepositoryResult<T>` per ADR-0006.
- Migrate one repository as a reference (recommended: `FeaturedLaunchRepository`, smallest surface).
- Update `AppModule.kt` or introduce feature-scoped modules during migration (see ADR-0005).

## Alternatives Considered

1. **Leave as-is; add tests instead.** Rejected — tests of a 1,426-line class are fragile and don't fix change amplification.
2. **Split by data tier (basic/normal/detailed) instead of by feature.** Rejected — couples to API shape, violates ADR-0001.

## References

- ADR-0002 (use-case layer)
- ADR-0006 (repository result type)
- [LaunchRepositoryImpl.kt](../../../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepositoryImpl.kt)
