# ADR-0001: Domain types are the only cross-layer currency

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

The OpenAPI-generated types under `me.calebjones.spacelaunchnow.api.launchlibrary.models.*` (and the SNAPI equivalents) are imported by 60+ files across every layer — `api/extensions/`, `database/`, `data/repository/`, `domain/mapper/`, and historically UI. This means:

- An API spec change can ripple into Composables.
- Cache blobs are tied to the current API shape.
- The generated classes (often with 70+ positional parameters) become part of the app's effective public surface.

The in-flight [domain-model-layer migration](../../../specs/dev/domain-model-layer/) introduces `domain/model/` (`Launch`, `Event`, `PaginatedResult`, etc.) and `domain/mapper/` extensions. Phases 1–3 (models, mappers, tests) are complete. Repositories now expose parallel `xxxDomain()` methods but the old API-returning methods are not yet marked `@Deprecated`, and ViewModels still call `.toDomain()` themselves in several places (`HomeViewModel`, `HistoryViewModel`).

## Decision

1. `me.calebjones.spacelaunchnow.api.*.models.*` may be imported **only** from two packages:
   - `me.calebjones.spacelaunchnow.api.extensions` (API call site ergonomics)
   - `me.calebjones.spacelaunchnow.domain.mapper` (conversion to domain types)
2. All repository interfaces return domain types. Any method returning an API type is marked `@Deprecated(level = WARNING)` during migration, `ERROR` once call sites are migrated, and removed thereafter.
3. ViewModels, use cases, Composables, and platform-specific code (widgets, notifications) reference `domain.model.*` exclusively.
4. This rule is enforced by a Detekt/Konsist test (see follow-up work).

## Consequences

### Positive

- Spec changes are localized to `domain/mapper/`.
- Domain types can evolve independently of the API (e.g., `@Immutable`, additional computed fields).
- Enables ADR-0004 (cache schema versioning) by decoupling persistence from API shape.

### Negative / Trade-offs

- One-time migration cost across all repositories, ViewModels, and LocalDataSources.
- Mappers and domain types double the type surface until old API types are deleted.

### Follow-up work

- Finish phases 4–5 of `specs/dev/domain-model-layer/tasks.md` (repository + ViewModel migration).
- Add a Konsist test under `composeApp/src/commonTest/` that fails if forbidden imports appear outside the allow-listed packages.
- Remove all `@Deprecated` API-returning repository methods once call sites are migrated.
- Migrate `LocalDataSource` classes to store domain JSON (prerequisite: make domain types `@Serializable`). See ADR-0004.

## Alternatives Considered

1. **Keep the status quo with a guideline only.** Rejected — 60+ leaking imports proves guidelines without tooling don't hold.
2. **Use typealiases to re-export API types as "domain" types.** Rejected — preserves coupling, blocks field additions, and still fails the cache-schema goal.

## References

- [specs/dev/domain-model-layer/](../../../specs/dev/domain-model-layer/)
- [ARCHITECTURE_OVERVIEW.md](../ARCHITECTURE_OVERVIEW.md)
- ADR-0004 (cache schema versioning)
