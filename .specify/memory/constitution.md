<!--
Sync Impact Report:
- Version change: Initial → 1.0.0
- List of modified principles:
  - Added: I. Thread-Safety First
  - Added: II. Secure by Default
  - Added: III. Test-First (NON-NEGOTIABLE)
  - Added: IV. Transparent Error Handling
  - Added: V. Backward Compatibility
- Added sections: Additional Constraints, Development Workflow & Review Process
- Removed sections: None
- Templates requiring updates:
  - .specify/templates/plan-template.md (⚠ pending)
  - .specify/templates/spec-template.md (⚠ pending)
  - .specify/templates/tasks-template.md (⚠ pending)
- Follow-up TODOs: 
  - Update templates to reflect the new principles if applicable.
-->

# EZRetrofit Constitution

## Core Principles

### I. Thread-Safety First
Library core components (like `EZRetrofitHelper` and `CallManager`) MUST be thread-safe to prevent race conditions. Singleton patterns with mutable states are strictly prohibited for service creation. All shared collections must be thread-safe.

### II. Secure by Default
All TLS validation, certificate pinning, and cipher suites MUST adhere to modern security standards (e.g., SHA-256 for pins, modern cipher suites from `ConnectionSpec.MODERN_TLS`). Testing bypasses (like `DefaultTestingTrustManager`) MUST explicitly prevent execution in production environments using build configurations (e.g., `BuildConfig.DEBUG`).

### III. Test-First (NON-NEGOTIABLE)
TDD is mandatory. Must ensure unit tests cover 100% of bug fixes and concurrent stress tests before implementation. Red-Green-Refactor cycle strictly enforced.

### IV. Transparent Error Handling
Exceptions and misconfigurations MUST fail fast and explicitly throw descriptive errors (e.g., `IllegalStateException`). Silent failures or empty catch blocks are prohibited. Any recoverable internal errors MUST be logged via a pluggable Logger interface (`EZLogger`), never swallowed.

### V. Backward Compatibility
Public API changes MUST NOT break existing integrations. Typo corrections or method updates MUST retain the old method signature with a `@Deprecated` annotation to ensure seamless upgrades for current users.

## Additional Constraints

- **Technology Stack**: Java for Android.
- **Dependencies**: Must be kept updated to avoid EOL versions (e.g., OkHttp, Retrofit). Repositories like JCenter must not be used (use mavenCentral).
- **Serialization**: String formatting (like cookie serialization) must be strictly validated (no trailing delimiters).

## Development Workflow & Review Process

- Code reviews MUST verify thread safety (no mutable singletons for concurrent paths) and security configurations.
- All new methods and bug fixes MUST be accompanied by corresponding unit tests.
- Static analysis and code reviews must explicitly check for empty catch blocks and ensure proper logging is utilized.
- Deprecated methods from previous versions should be scheduled for removal in the next major version bump.

## Governance

This Constitution supersedes all other practices. All PRs and code reviews MUST verify compliance with these principles. Any deviation or complexity MUST be justified in `research.md`.

Amendments to this constitution require documentation, team approval, and a migration plan. The constitution version must be bumped according to semantic versioning (MAJOR for breaking governance changes, MINOR for new principles, PATCH for clarifications).

**Version**: 1.0.0 | **Ratified**: 2026-05-30 | **Last Amended**: 2026-05-30
