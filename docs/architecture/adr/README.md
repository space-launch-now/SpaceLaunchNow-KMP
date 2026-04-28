# Architecture Decision Records

This directory contains the architectural decisions that shape the SpaceLaunchNow codebase. Start with [ARCHITECTURE_OVERVIEW.md](../ARCHITECTURE_OVERVIEW.md) for the big picture.

## Index

| #     | Title                                                            | Status   |
| ----- | ---------------------------------------------------------------- | -------- |
| 0000  | [ADR template](0000-template.md)                                 | —        |
| 0001  | [Domain types are the only cross-layer currency](0001-domain-boundary.md) | Proposed |
| 0002  | [Introduce a use-case layer for multi-source orchestration](0002-usecase-layer.md) | Proposed |
| 0003  | [Decompose god repositories by concern](0003-repository-decomposition.md) | Proposed |
| 0004  | [Cache stores domain JSON with explicit schema versions](0004-cache-schema-versioning.md) | Proposed |
| 0005  | [Modularize the Gradle project](0005-modularization.md)          | Proposed |
| 0006  | [Unify repository return type on `RepositoryResult<T>`](0006-repository-result-type.md) | Proposed |

## Conventions

- Filename: `NNNN-kebab-case-title.md` (zero-padded, sequential).
- Status: `Proposed` → `Accepted` → optionally `Superseded by ADR-XXXX` or `Deprecated`.
- Never edit an `Accepted` ADR substantively; supersede it with a new one and update the status.
- Reference related ADRs at the bottom of each file.
