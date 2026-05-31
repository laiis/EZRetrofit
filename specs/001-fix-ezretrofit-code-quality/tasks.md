---
description: "Task list for EZRetrofit Code Quality and Security Fixes"
---

# Tasks: EZRetrofit 程式碼品質與安全性修復

**Input**: Design documents from `/specs/001-fix-ezretrofit-code-quality/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/EZLogger.md, quickstart.md

**Tests**: Tests are requested via Constitution Check (III. Test-First) and are required for 100% bug fix coverage and concurrent stress tests.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [X] T001 Setup `com.github.gmazzo.buildconfig` plugin in build.gradle to generate `BuildConfig.DEBUG`
- [X] T002 [P] Create `EZLogger` interface contract in `src/main/java/tw/idv/laiis/ezretrofit/EZLogger.java`
- [X] T003 [P] Deprecate `LibConfig` class in `src/main/java/tw/idv/laiis/ezretrofit/LibConfig.java`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

- [X] T004 Create `EZRetrofitHelper` class in `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofitHelper.java` to support thread-safe instantiation
- [X] T005 Add `setLogger(EZLogger)` method and standard Java-based `System.err` logging fallback (avoiding Android platform-specific dependencies) in `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java`

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Cookie 管理正確清除過期資料 (Priority: P1) 🎯 MVP

**Goal**: Ensure expired cookies are correctly removed from memory and persistent storage, and serialization works flawlessly.

**Independent Test**: Verify `PersistentCookieStore` receiving an expired cookie removes it, and doesn't impact other cookies.

### Tests for User Story 1 ⚠️

> **NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [X] T006 [P] [US1] Unit test for Cookie remove by name, join without trailing comma, and corrupt decode handling in `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java`

### Implementation for User Story 1

- [X] T007 [US1] Fix `PersistentCookieStore.remove()` to use cookie `name` as key instead of domain in `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java`
- [X] T008 [US1] Fix `PersistentCookieStore.join()` to serialize without trailing comma in `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java`
- [X] T009 [US1] Handle corrupted cookie decoding by logging warning via `EZLogger` instead of returning null in `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java`

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - 多執行緒環境下安全建立 Retrofit 服務 (Priority: P1)

**Goal**: Thread-safe Retrofit service creation using `EZRetrofit.create()` with standard Java double-checked locking and `volatile` cache variables to avoid baseUrl mismatch.

**Independent Test**: Concurrent stress test with 10 threads, 1000 iterations for correct baseUrl.

### Tests for User Story 2 ⚠️

- [X] T010 [P] [US2] Concurrent stress test for `EZRetrofit.create(Class)` in `src/test/java/tw/idv/laiis/ezretrofit/EZRetrofitTest.java`
- [X] T011 [P] [US2] Unit test for `CallManager.cancelAll()` thread safety in `src/test/java/tw/idv/laiis/ezretrofit/CallManagerTest.java`

### Implementation for User Story 2

- [X] T012 [US2] Update `EZRetrofit.create()` cache variable with `volatile` and implement double-checked locking in `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java`
- [X] T013 [US2] Refactor `CallManager` to use thread-safe collections (`ConcurrentLinkedQueue`) for call lists in `src/main/java/tw/idv/laiis/ezretrofit/CallManager.java`

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - 安全性：Certificate Pinning 使用強雜湊演算法 (Priority: P2)

**Goal**: Enforce SHA-256 standard for certificate pinning, drop SHA-1 support, and secure cipher suites.

**Independent Test**: Verify connection with valid SHA-256 pin succeeds, while SHA-1 pin fails.

### Tests for User Story 3 ⚠️

- [X] T014 [P] [US3] Unit test for SHA-256 vs SHA-1 pin validation in `src/test/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManagerTest.java`

### Implementation for User Story 3

- [X] T015 [US3] Update `EZRetrofitTrustManager` to enforce SHA-256 hashing and handle empty pins gracefully without NPE in `src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java`
- [X] T016 [US3] Restrict `SupportAllTlsSocketFactory` to OkHttp `ConnectionSpec.MODERN_TLS.cipherSuites()` in `src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java`

**Checkpoint**: All user stories up to US3 should be independently functional

---

## Phase 6: User Story 4 - 防止測試用 TrustManager 誤用於正式環境 (Priority: P2)

**Goal**: Ensure `DefaultTestingTrustManager` cannot bypass TLS in production.

**Independent Test**: Verify exception is thrown when testing trust manager is used with `BuildConfig.DEBUG == false`.

### Tests for User Story 4 ⚠️

- [X] T017 [P] [US4] Unit test for environment check based on `BuildConfig.DEBUG` in `src/test/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManagerTest.java`

### Implementation for User Story 4

- [X] T018 [US4] Update `DefaultTestingTrustManager` to check `BuildConfig.DEBUG` and throw exception in production in `src/main/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManager.java`

---

## Phase 7: User Story 5 - 例外處理透明化，不再靜默失敗 (Priority: P3)

**Goal**: Remove silent failures and ensure clear exception messaging when configurations are incorrect.

**Independent Test**: Verify incomplete SSL configurations throw `IllegalStateException`.

### Tests for User Story 5 ⚠️

- [X] T019 [P] [US5] Unit test for `SSLFactoryManager.build()` missing properties in `src/test/java/tw/idv/laiis/ezretrofit/managers/SSLFactoryManagerTest.java`

### Implementation for User Story 5

- [X] T020 [US5] Throw `IllegalStateException` on incomplete configuration in `SSLFactoryManager.build()` in `src/main/java/tw/idv/laiis/ezretrofit/managers/SSLFactoryManager.java`
- [X] T021 [US5] Fix tag validation in `CallManager` to use positive logic (`tag != null && !tag.isEmpty()`) in `src/main/java/tw/idv/laiis/ezretrofit/CallManager.java`
- [X] T022 [US5] Throw explicit exception in `EZRetrofit.create()` if `initial()` hasn't been called in `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java`

---

## Phase 8: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories and code quality

- [X] T023 [P] Fix API misspellings (e.g., `getCertficatePinner` -> `getCertificatePinner`) and deprecate old methods in `src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java` and related classes.
- [X] T024 [P] Add comments for unused `mProtocol` and `mCertPins` fields indicating they are unused.
- [X] T025 Run quickstart.md validation to verify all code samples.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3-7)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (US1 → US2 → US3 → US4 → US5)
- **Polish (Final Phase)**: Depends on all user stories being complete

### Parallel Opportunities

- All tests for a user story marked [P] can run in parallel
- Different user stories can be worked on in parallel by different team members once Phase 2 is complete.
