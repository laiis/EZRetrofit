<!--
Sync Impact Report:
- Version change: 1.0.0 → 1.1.0
- List of modified principles:
  - 遵循「文件語言：正體中文」約束條款全面翻譯
- Added sections: None
- Removed sections: None
- Templates requiring updates:
  - .specify/templates/plan-template.md (⚠ pending)
  - .specify/templates/spec-template.md (⚠ pending)
  - .specify/templates/tasks-template.md (⚠ pending)
- Follow-up TODOs: 
  - Update templates to reflect the new principles if applicable.
-->

# EZRetrofit 專案憲法

## 核心準則

### I. 執行緒安全優先
函式庫核心元件（如 `EZRetrofitHelper` 與 `CallManager`）必須為執行緒安全，以避免競爭危害 (race conditions)。嚴禁在服務建立時使用具備可變狀態的單例模式。所有共用的集合物件必須具備執行緒安全性。

### II. 預設安全
所有 TLS 驗證、憑證綁定與密碼套件必須符合現代安全標準（例如：綁定使用 SHA-256，密碼套件採用 `ConnectionSpec.MODERN_TLS`）。測試用途的繞過機制（如 `DefaultTestingTrustManager`）必須明確限制只能在非正式環境中執行（例如透過 `BuildConfig.DEBUG` 判斷）。

### III. 測試優先 (絕不妥協)
強制執行測試驅動開發 (TDD)。實作前必須確保單元測試 100% 覆蓋錯誤修復及並行壓力測試。嚴格遵守紅燈-綠燈-重構 (Red-Green-Refactor) 的循環。

### IV. 透明的錯誤處理
例外狀況與設定錯誤必須具備快速失敗 (fail fast) 的特性，並明確拋出具描述性的錯誤（如 `IllegalStateException`）。嚴禁靜默失敗 (silent failures) 或空的 catch 區塊。任何可復原的內部錯誤必須透過可抽換的日誌介面（`EZLogger`）記錄下來，絕不可直接吞沒。

### V. 向下相容
公開 API 的變更絕不可破壞既有的整合。修正錯字或更新方法時，必須保留舊的方法簽章並加上 `@Deprecated` 標註，確保現有使用者能無縫升級。

## 附加約束條件

- **技術堆疊**：Android 的 Java 環境。
- **相依套件**：必須保持更新以避免使用已終止支援 (EOL) 的版本（例如 OkHttp、Retrofit）。不可使用 JCenter 等套件庫（請使用 mavenCentral）。
- **序列化**：字串格式化（如 cookie 序列化）必須經過嚴格驗證（不可有結尾的分隔符號）。

## 開發工作流程與審查流程

- 程式碼審查 (Code reviews) 必須驗證執行緒安全性（並行路徑中不可有可變單例）及安全設定。
- 所有新方法與錯誤修復都必須附帶對應的單元測試。
- 靜態分析與程式碼審查必須明確檢查是否包含空的 catch 區塊，並確保正確使用日誌記錄。
- 前次版本中已廢棄 (Deprecated) 的方法應排定於下一次主版號更新時移除。

## 治理

本憲法凌駕於所有其他實務之上。所有 Pull Requests (PRs) 與程式碼審查必須驗證是否符合這些準則。任何偏離或增加的複雜性皆必須在 `specs/001-fix-ezretrofit-code-quality/research.md` 中提出正當理由。

本憲法的修正案需要文件記錄、團隊核准及遷移計畫。憲法版本號必須依據語意化版本進行升級（破壞性治理變更為 MAJOR，新增準則為 MINOR，文字釐清為 PATCH）。

**版本**: 1.1.0 | **批准日期**: 2026-05-30 | **最後修正日期**: 2026-05-30
