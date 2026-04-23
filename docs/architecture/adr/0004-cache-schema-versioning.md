# ADR-0004: Cache stores domain JSON with explicit schema versions

- **Status:** Proposed
- **Date:** 2026-04-21
- **Deciders:** Core team

## Context

SQLDelight cache tables (`LaunchBasicCache`, `LaunchNormalCache`, `LaunchDetailedCache`, `EventCache`, `ArticleCache`, `UpdateCache`, `ProgramCache`, `SpacecraftCache`, etc.) store raw JSON blobs of the **generated API types**. Comments in `SpacecraftLocalDataSource.kt` and `SpaceStationLocalDataSource.kt` explicitly acknowledge this.

Problems:

- An API spec change (e.g., field renamed, removed, or retyped in LL2.5) can silently deserialize to nulls or throw at read time.
- The `Json { ignoreUnknownKeys = true }` mitigation masks drift rather than detecting it.
- There is no `schema_version` column on cache tables, only SQLDelight table migrations for DDL.
- This couples the cache layer to the same types that ADR-0001 tries to isolate.

## Decision

1. Cache blobs store `@Serializable` **domain** types, not API types.
2. Every cache table gains a `schema_version INTEGER NOT NULL` column.
3. A central `CacheSchemaVersion` object exposes a `const val` per domain type (e.g., `LAUNCH = 1`, `EVENT = 1`).
4. On read, if `schema_version != current`, the row is treated as a cache miss and evicted. No custom migration path.
5. On any breaking change to a domain type, bump its `CacheSchemaVersion` constant. A CI check can lint for schema bumps alongside domain-type modifications.

This depends on ADR-0001 being complete (domain types fully replace API types in repositories) and on domain types being marked `@Serializable`.

## Consequences

### Positive

- API evolution cannot corrupt the cache; at worst it drops stale rows.
- Cache shape evolves with the domain, which evolves deliberately.
- Enables offline-first confidence: we know what was stored.

### Negative / Trade-offs

- Domain types must be `@Serializable` — constrains them (no function/lambda fields, careful with sealed classes).
- One-time migration: existing cached rows will be invalidated on first run post-rollout (acceptable; data is re-fetchable).
- Schema version discipline becomes a review item.

### Follow-up work

- Add `@Serializable` to domain types (`Launch`, `Event`, common value types).
- Add `schema_version` column to all cache `.sq` files.
- Update all `LocalDataSource` classes to (de)serialize domain types with version check.
- Add a debug-menu action to force-invalidate all caches.
- Document the schema-bump workflow in `CONTRIBUTING.md`.

## Alternatives Considered

1. **Write a per-field migration framework.** Rejected — huge maintenance cost for data that is freely re-fetchable.
2. **Version only at the database level.** Rejected — invalidates the entire DB on any domain change, which is heavier than per-table version checks.
3. **Continue storing API JSON with `ignoreUnknownKeys`.** Rejected — silently masks drift, blocks ADR-0001.

## References

- Repo memory: `caching-architecture.md`
- ADR-0001 (domain boundary)
- SQLDelight schemas under `composeApp/src/commonMain/sqldelight/`
