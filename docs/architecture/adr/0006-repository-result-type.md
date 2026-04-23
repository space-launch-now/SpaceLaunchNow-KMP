# ADR-0006: Unify repository return type on `RepositoryResult<T>`

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

The in-flight domain migration introduced signatures like:

```kotlin
suspend fun getFeaturedLaunchDomain(
    forceRefresh: Boolean = false,
    agencyIds: List<Int>? = null,
    locationIds: List<Int>? = null
): Result<DataResult<PaginatedResult<Launch>>>
```

Three nested wrappers:

- `kotlin.Result<T>` — success/failure
- `DataResult<T>` — data-source tag (`Network`, `Cache`, `Stale`, `Error`)
- `PaginatedResult<T>` — paging envelope (`count`, `next`, `previous`, `results`)

Every call site unwraps three levels. ViewModels like `HomeViewModel` and `LaunchesViewModel` do this repeatedly, making branching logic opaque and encouraging inconsistent error handling (some paths bail at `Result` failure, others at `DataResult.Error`).

Stale-while-revalidate also can't be modeled well: a single suspend call can only return one of {fresh, stale, error}, but the UI usually wants stale-then-fresh. Today this is worked around with callbacks inside repositories.

## Decision

Introduce a single sealed result type and move to `Flow`-based repository APIs where stale-while-revalidate matters:

```kotlin
sealed interface RepositoryResult<out T> {
    val source: Source   // Network | Cache | Stale
    data class Success<T>(val data: T, override val source: Source) : RepositoryResult<T>
    data class Failure<T>(val throwable: Throwable, val cachedData: T? = null, override val source: Source = Source.Network) : RepositoryResult<T>
    enum class Source { Network, Cache, Stale }
}
```

Repositories expose:

- `fun observeX(...): Flow<RepositoryResult<T>>` for stale-while-revalidate (emits cache → network).
- `suspend fun getX(...): RepositoryResult<T>` for one-shot reads.

`PaginatedResult<T>` remains inside `T` when paging is meaningful (i.e., `RepositoryResult<PaginatedResult<Launch>>` is still legal but the `Result` and `DataResult` wrappers are gone).

ViewModels collect the `Flow` and map directly to `ViewState`.

## Consequences

### Positive

- One unwrap per call site.
- Stale-while-revalidate becomes first-class (emit stale immediately, emit fresh on success, or emit `Failure` with `cachedData`).
- Clearer error policy: failures can carry cached fallback data.

### Negative / Trade-offs

- Wide-ranging refactor; every repository method and every call site changes.
- Cannot land incrementally without adapter functions for the transition period.

### Follow-up work

- Add `RepositoryResult<T>` under `domain/model/`.
- Add `Flow<RepositoryResult<T>>.asState()` helper for Compose state mapping.
- Migrate one repository end-to-end (recommended: `FeaturedLaunchRepository` per ADR-0003) as a reference.
- Provide a temporary adapter `Result<DataResult<T>>.toRepositoryResult()` during migration.

## Alternatives Considered

1. **Keep `Result<DataResult<PaginatedResult<T>>>`.** Rejected — readability cost is paid on every call site forever.
2. **Remove `DataResult` but keep `Result` + `PaginatedResult`.** Loses source-attribution for stale-while-revalidate UX.
3. **Adopt Arrow's `Either` or `Raise` DSL.** Rejected — adds a large dependency and a new idiom the team hasn't agreed on.

## References

- ADR-0003 (repository decomposition — `CachePolicy` helper will return `RepositoryResult`)
- [LaunchRepository.kt](../../../composeApp/src/commonMain/kotlin/me/calebjones/spacelaunchnow/data/repository/LaunchRepository.kt)
