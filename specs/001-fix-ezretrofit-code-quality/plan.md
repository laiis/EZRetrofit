# Implementation Plan: EZRetrofit 程式碼品質與安全性修復

**Branch**: `fix/ezretrofit-code-quality` | **Date**: 2026-05-30 | **Spec**: [spec.md](file:///C:/Users/laiis/pg_workspaces/projects/EZRetrofit/specs/001-fix-ezretrofit-code-quality/spec.md)

**Input**: Feature specification from `/specs/001-fix-ezretrofit-code-quality/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

依據 code_review.md 修復 EZRetrofit 函式庫的安全性（強制 SHA-256 Pinning、防護 DefaultTestingTrustManager 誤用）、執行緒安全（EZRetrofitHelper 並發建立 Retrofit 服務）、邏輯 bug（過期 Cookie 移除異常、join 尾端逗號）及程式碼品質問題，並確保向後相容性與可見的例外處理。

## Technical Context

**Language/Version**: Java / Android
**Primary Dependencies**: Retrofit, OkHttp, `com.github.gmazzo.buildconfig`
**Storage**: SharedPreferences (implied for Cookie persistence)
**Testing**: JUnit
**Target Platform**: Android
**Project Type**: Android Library
**Performance Goals**: N/A
**Constraints**: Thread-safe, Backward Compatible, No silent failures
**Scale/Scope**: EZRetrofit library core components

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **III. Test-First (NON-NEGOTIABLE)**: Must ensure unit tests cover 100% of bug fixes and concurrent stress tests before implementation.

## Project Structure

### Documentation (this feature)

```text
specs/001-fix-ezretrofit-code-quality/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/
├── main/java/
└── test/java/
```

**Structure Decision**: Option 1: Single project (Android Library)

## Implementation Decisions & Checklist Resolutions

### 1. Security & Certificate Pinning
- **Pin Format**: SHA-256 pins must be provided as standard Base64 encoded strings (RFC 4648, non-URL-safe, no wrapping) prefixed with `sha256/`.
- **Empty Pins**: If `mPins` is null or empty, `validateCertificatePin()` will immediately return (pass validation) without throwing `NullPointerException`.
- **Cipher Suites**: `SupportAllTlsSocketFactory` will exclusively use the cipher suites defined in OkHttp's `ConnectionSpec.MODERN_TLS.cipherSuites()`.

### 2. Thread Safety & Concurrency
- **EZRetrofit.create()**: Must use a `ConcurrentHashMap` for service caching. Double-checked locking with `synchronized` must be used during instantiation to prevent race conditions.
- **cancelAll()**: Call lists must be managed using thread-safe collections (e.g., `ConcurrentLinkedQueue` or `CopyOnWriteArrayList`) to avoid `ConcurrentModificationException` during simultaneous modifications.
- **Stress Test NFR**: The 10-thread, 1000-iteration creation test must execute without blocking deadlocks and complete within strictly 5000 milliseconds (5 seconds).

### 3. API & Backward Compatibility
- **API Spelling Corrections**: Misspelled methods (e.g., `getCertficatePinner`) will be marked `@Deprecated`. They will act as proxy methods forwarding calls to the correctly spelled equivalents (`getCertificatePinner`).
- **Validation**: Binary backward compatibility is ensured by keeping the exact same method signatures for the deprecated methods.

### 4. Error Handling & Edge Cases
- **SSLFactoryManager**: `build()` will throw `IllegalStateException` with a specific message indicating which property is missing (e.g., "TlsVersion cannot be null").
- **Testing TrustManager**: `DefaultTestingTrustManager` will check `BuildConfig.DEBUG`. If false, it throws `SecurityException` with message "DefaultTestingTrustManager must not be used in production".
- **EZLogger Fallback**: If no custom logger is injected via `EZRetrofit.setLogger()`, the default implementation will route warnings to Android's built-in `android.util.Log.w()` to comply with platform guidelines, avoiding `System.err`.
- **Cookie Persistence**: 
  - Expired cookies are those where `cookie.expiresAt() < System.currentTimeMillis()`.
  - Corrupted SharedPreferences entries throwing `Exception` during decode will be caught, logged via `EZLogger.warn()`, and skipped.
  - If a cookie's domain map does not contain the specified domain during `remove()`, the operation will safely return without exceptions.
