# 實作計畫：EZRetrofit 程式碼品質與安全性修復

**分支**: `fix/ezretrofit-code-quality` | **日期**: 2026-05-30 | **規格書**: [spec.md](file:///C:/Users/laiis/pg_workspaces/projects/EZRetrofit/specs/001-fix-ezretrofit-code-quality/spec.md)

**輸入**: 來自 `/specs/001-fix-ezretrofit-code-quality/spec.md` 的功能規格書。

**說明**: 本範本由 `/speckit-plan` 命令填寫。執行工作流程請參閱 `.specify/templates/plan-template.md`。

## 摘要

依據 `build/reports/code_review.md` 修復 EZRetrofit 函式庫的安全性（強制 SHA-256 Pinning、防護 DefaultTestingTrustManager 誤用）、執行緒安全（EZRetrofitHelper 並行建立 Retrofit 服務）、邏輯錯誤（過期 Cookie 移除異常、join 尾端逗號）及程式碼品質問題，並確保向後相容性與可見的例外處理。

## 技術上下文

**開發語言/版本**: Java / Android

**主要依賴項**: Retrofit, OkHttp, `com.github.gmazzo.buildconfig`

**儲存機制**: SharedPreferences (Cookie 持久化)

**測試框架**: JUnit

**目標平台**: Android

**專案類型**: Android 函式庫 (Android Library)

**效能目標**: 在高並行環境下，確保執行緒安全的初始化與 Retrofit 服務實例建立。

**限制條件**: 執行緒安全、向後相容、不允許靜默失敗。

**規模/範圍**: EZRetrofit 函式庫的核心元件。

## 憲法檢驗

*關卡：必須在階段 0 研究前通過。在階段 1 設計後重新檢查。*

- **I. 執行緒安全優先**: 函式庫核心元件（如 `EZRetrofitHelper` 與 `CallManager`）必須為執行緒安全，以避免競爭危害 (race conditions)。嚴禁在服務建立時使用具備可變狀態的單例模式。所有共用的集合物件必須具備執行緒安全性。
- **II. 預設安全**: 所有 TLS 驗證、憑證綁定與密碼套件必須符合現代安全標準（例如：綁定使用 SHA-256，密碼套件採用 `ConnectionSpec.MODERN_TLS`）。測試用途的繞過機制（如 `DefaultTestingTrustManager`）必須明確限制只能在非正式環境中執行（例如透過 `BuildConfig.DEBUG` 判斷）。
- **III. 測試優先 (絕不妥協)**: 強制執行測試驅動開發 (TDD)。實作前必須確保單元測試 100% 覆蓋錯誤修復及並行壓力測試。嚴格遵守紅燈-綠燈-重構 (Red-Green-Refactor) 的循環。
- **IV. 透明的錯誤處理**: 例外狀況與設定錯誤必須具備快速失敗 (fail fast) 的特性，並明確拋出具描述性的錯誤（如 `IllegalStateException`）。嚴禁靜默失敗 (silent failures) 或空的 catch 區塊。任何可復原的內部錯誤必須透過可抽換的日誌介面（`EZLogger`）記錄下來，絕不可直接吞沒。
- **V. 向下相容**: 公開 API 的變更絕不可破壞既有的整合。修正錯字或更新方法時，必須保留舊的方法簽章並加上 `@Deprecated` 標註，確保現有使用者能無縫升級。

## 專案結構

### 文件結構 (此功能)

```text
specs/001-fix-ezretrofit-code-quality/
├── plan.md              # 本檔案 (/speckit-plan 命令輸出)
├── research.md          # 階段 0 輸出 (/speckit-plan 命令)
├── data-model.md        # 階段 1 輸出 (/speckit-plan 命令)
├── quickstart.md        # 階段 1 輸出 (/speckit-plan command)
├── contracts/           # 階段 1 輸出 (/speckit-plan command)
│   └── EZLogger.md      # API 介面合約
└── tasks.md             # 階段 2 輸出 (/speckit-tasks command)
```

### 原始碼結構 (儲存庫根目錄)

```text
src/
├── main/java/
└── test/java/
```

**結構決策**: 單一專案 (Android 函式庫)

## 實作決策與檢驗清單決議

### 1. 安全性與憑證綁定 (Certificate Pinning)
- **Pin 碼格式**: SHA-256 pin 碼必須提供標準 Base64 編碼字串（RFC 4648，非 URL 安全，不換行），並帶有 `sha256/` 前綴。
- **空 Pin 碼處理**: 若 `mPins` 為 null 或空值，`validateCertificatePin()` 應立即返回（通過驗證），不應拋出 `NullPointerException`。
- **密碼套件**: `SupportAllTlsSocketFactory` 將僅使用 OkHttp 的 `ConnectionSpec.MODERN_TLS.cipherSuites()` 中定義的密碼套件。

### 2. 執行緒安全與並行
- **EZRetrofit.create()**: 必須使用 `ConcurrentHashMap` 進行服務快取。在實例化過程中必須使用 `synchronized` 雙重鎖定檢測 (Double-checked locking) 與 `volatile` 快取變數來防止競爭危害。
- **cancelAll()**: 呼叫清單必須使用執行緒安全的集合（如 `ConcurrentLinkedQueue` 或 `CopyOnWriteArrayList`）進行管理，以避免在同時修改時發生 `ConcurrentModificationException`。
- **壓力測試非功能性需求**: 10 個執行緒、1000 次反覆建立的壓力測試必須在沒有死鎖阻塞的情況下執行，且必須在 5000 毫秒（5 秒）內完成。

### 3. API 與向後相容性
- **API 拼寫修正**: 拼寫錯誤的方法（例如 `getCertficatePinner`）將被標記為 `@Deprecated`。它們將作為代理方法，將呼叫轉發給拼寫正確的對等方法（`getCertificatePinner`）。
- **驗證**: 透過為已棄用的方法保留完全相同的方法簽章，確保二進位向後相容性。

### 4. 錯誤處理與邊界情況
- **SSLFactoryManager**: `build()` 將在缺少屬性時拋出 `IllegalStateException` 並附帶特定訊息（例如 "TlsVersion cannot be null"）。
- **測試信任管理器**: `DefaultTestingTrustManager` 將檢查 `BuildConfig.DEBUG`。如果為 false，它將拋出 `SecurityException`，並附帶訊息 "DefaultTestingTrustManager must not be used in production"。
- **EZLogger 回退機制**: 如果沒有透過 `EZRetrofit.setLogger()` 注入自訂日誌記錄器，預設實作將把警告路由到標準 Java 的 `System.err`，以符合跨平台純 Java 函式庫的規範。
- **Cookie 持久化**: 
  - 過期的 Cookie 定義為 `cookie.expiresAt() < System.currentTimeMillis()`。
  - 在解碼過程中拋出 `Exception` 的損毀 SharedPreferences 項目將被捕獲、透過 `EZLogger.warn()` 記錄警告，並跳過。
  - 若在 `remove()` 過程中 Cookie 的網域對照表不包含指定的網域，此操作將安全返回而不拋出例外。

