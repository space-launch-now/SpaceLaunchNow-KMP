# Research: Domain Model Layer Migration

**Feature**: Introduce domain models to decouple UI/ViewModel from API response types  
**Date**: 2026-04-19 | **Status**: Complete

---

## R-001: Unified Launch Model vs. Tiered Models

**Decision**: Single `Launch` domain model with nullable detail-only fields.

**Rationale**: The API returns three tiers (LaunchBasic, LaunchNormal, LaunchDetailed) as a transport optimization — not a domain concept. A single `Launch` data class with nullable fields for detail-only data eliminates the `LaunchCardData` sealed interface, removes `when` branches from every composable, and lets any UI component work with the same type. Fields absent at lower API tiers are simply null.

**Alternatives Considered**:
- **Mirror API tiers** (`LaunchSummary` + `LaunchDetail`): Rejected — recreates the multi-type problem. Every composable still needs branching.
- **Inheritance** (`LaunchDetail extends Launch`): Rejected — Kotlin data classes cannot inherit from data classes.
- **Single model + `DetailLevel` enum**: Rejected — nullable fields already communicate availability without extra ceremony.

**Mapping Strategy**:
- `LaunchBasic` → `Launch` with rocket/mission/pad/program = null (only id, name, net, status, image, provider populated)
- `LaunchNormal` → `Launch` with most fields populated; detail-only fields (updates, vidUrls, infoUrls, crew, stages) = null
- `LaunchDetailed` → `Launch` fully populated

---

## R-002: SQLDelight Cache Strategy

**Decision**: Keep JSON blob storage; map to domain model on read.

**Rationale**: Current SQLDelight tables store full serialized JSON (`json_data TEXT`) and deserialize to API types. Introducing a mapper layer after deserialization avoids any DB migration. The local data source returns domain `Launch` instead of API types. The mapper is the single conversion point.

**Alternatives Considered**:
- **Store domain model JSON**: Rejected — requires DB migration, breaks existing caches
- **Normalized tables**: Rejected — massive schema redesign, out of scope
- **Dual return types during transition**: Rejected — increases complexity

---

## R-003: Mapper Architecture

**Decision**: Top-level extension functions (not interfaces/classes).

**Rationale**: Only one API backend exists (Launch Library) with no concrete plan for another. Interface + DI registration for pure stateless data mapping is over-engineering. Extension functions are idiomatic Kotlin and testable without mocking. If a second backend is ever needed, extracting to an interface is trivial.

**Pattern**:
```kotlin
// domain/mapper/LaunchMappers.kt
fun LaunchBasic.toDomain(): Launch = Launch(...)
fun LaunchNormal.toDomain(): Launch = Launch(...)
fun LaunchDetailed.toDomain(): Launch = Launch(...)
fun PaginatedLaunchBasicList.toDomain(): PaginatedResult<Launch> = ...
fun PaginatedLaunchNormalList.toDomain(): PaginatedResult<Launch> = ...
```

**Alternatives Considered**:
- **Interface + DI (`LaunchMapper` / `LLLaunchMapper`)**: Rejected — YAGNI, no second implementation
- **Mapper classes**: Rejected — stateless mapping doesn't need instances
- **Companion object factories**: Rejected — couples domain model to API types

---

## R-004: Event Model Approach

**Decision**: Single `Event` domain model, same strategy as Launch.

**Rationale**: Events have the same Normal/Detailed split. One `Event` data class with nullable fields for detail-only data (agencies, launches, expeditions, spacestations, programs, astronauts).

---

## R-005: Migration Strategy

**Decision**: Phased incremental — Launch + Event first (3A), then secondary entities (3B).

**Rationale**: Launch and Event cover ~80% of UI surfaces (Home, Schedule, Launch Detail, Event Detail). Migrating first validates the pattern before applying to 7+ other entity types. Keeps PRs reviewable.

**Migration Order**:
1. **Phase 3A**: Launch + Event domain models, mappers, repository updates, ViewModel updates, UI updates
2. **Phase 3B**: Agency, Astronaut, Vehicle, Launcher, Spacecraft, SpaceStation, Program
3. **Phase 3C**: Verification, cleanup `@Deprecated` methods

---

## R-006: LaunchCardData Sealed Interface

**Decision**: Eliminate entirely; replace with direct `Launch` parameter.

**Rationale**: `LaunchCardData` exists solely because composables handle 3 API types. With unified `Launch`, the sealed interface is unnecessary. `LaunchCardHeaderOverlay(launch: Launch, ...)` replaces `LaunchCardHeaderOverlay(launchData: LaunchCardData, ...)`.

---

## R-007: Image and Media Handling

**Decision**: Flatten `Image` to URLs on domain model; keep `DomainImage` value type for full metadata.

**Rationale**: Most UI usage only needs URL strings. The domain model carries `imageUrl: String?` and `thumbnailUrl: String?` flattened from the API `Image` object. For screens needing credit/license, access the full object via `images: List<DomainImage>` on the detail variant.

---

## R-008: VidURL Simplification

**Decision**: Map to `VideoLink` domain value type.

**Rationale**: API `VidURL` has 11 fields. Most UI needs: url, title, source, isLive. Domain model:
```kotlin
data class VideoLink(val url: String, val title: String?, val source: String?, val isLive: Boolean)
```

---

## R-009: LaunchFormatUtil

**Decision**: Add `Launch` (domain) overload, deprecate API-type overloads.

**Rationale**: The manual-parameter overload stays. Add `formatLaunchTitle(launch: Launch): String`. Mark `LaunchBasic`/`LaunchNormal`/`LaunchDetailed` overloads as `@Deprecated` with `ReplaceWith`.

---

## R-010: LaunchCache Simplification

**Decision**: Collapse to single `Launch` cache map.

**Rationale**: Currently maintains separate `normalCache` and `detailedCache`. With unified domain model, becomes `launchCache: Map<String, Launch>` with same TTL logic. A launch mapped from `LaunchDetailed` has more populated fields but is the same type.
