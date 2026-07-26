# Tasks: Code Review Remediation

**Input**: Design documents from `specs/003-code-review-remediation/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: 本特徵開發採用測試優先與測試覆蓋驗證。每個使用者故事包含對應的單元測試與壓力測試，且要求行覆蓋率至少達 80%。

**Organization**: 任務清單按使用者故事階段進行組織，以支援每個故事的獨立實作與獨立驗證。

---

## Format: `[ID] [P?] [Story] Description`

- **[P]**: 可並行開發（修改不同檔案，無未完成依賴）
- **[Story]**: 任務所屬使用者故事標籤 (例如: [US1], [US2], [US3])
- 任務敘述中必須包含明確的檔案相對路徑。

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: 專案目錄結構初始化與子套件環境準備

- [X] T001 建立子套件目錄結構 `src/main/java/tw/idv/laiis/ezretrofit/config/` 與 `src/main/java/tw/idv/laiis/ezretrofit/client/`
- [X] T002 確認 `build.gradle` 中的 dependencies 能正常編譯並解析 OkHttp 4.12.0 與 Retrofit 2.12.0

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: 全局基礎架構，必須先完成才能開始使用者故事實作

- [X] T003 [P] 擴充 `EZLogger` 日誌介面，增加與 `warn` 風格一致的 `info`、`debug` 與 `error` 方法，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/EZLogger.java`
- [X] T004 建立領域配置基礎實體類別（`SslConfig`、`ProxyConfig`、`TimeoutConfig`、`InterceptorConfig`）於目錄 `src/main/java/tw/idv/laiis/ezretrofit/config/`

---

## Phase 3: User Story 1 - 架構拆分與向下相容 (Priority: P1) 🎯 MVP

**Goal**: 將 `EZRetrofit` 過度集中的職責拆分為單一職責類別，同時保留舊 `EZRetrofit` 方法作為相容 facade。

**Independent Test**: 執行既有單元測試（如 `EZRetrofitTest.java`），確保不影響舊呼叫端；並對新拆分出的類別進行 100% 編譯與測試。

### Tests for User Story 1

- [X] T005 [P] [US1] 撰寫新設定類別單元測試，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/config/EZRetrofitConfigTest.java`
- [X] T006 [P] [US1] 撰寫新客戶端單元測試，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/client/EZRetrofitClientTest.java`

### Implementation for User Story 1

- [X] T007 [P] [US1] 實作全局設定中心 `EZRetrofitConfig`，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/config/EZRetrofitConfig.java`
- [X] T008 [P] [US1] 實作 OkHttpClient 與 Retrofit 建置器 `EZRetrofitClient`，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/client/EZRetrofitClient.java`
- [X] T009 [P] [US1] 實作請求計數與生命週期管理類別 `EZRetrofitLifecycle`，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/client/EZRetrofitLifecycle.java`
- [X] T010 [US1] 重構 `EZRetrofit` 將其設為 `@Deprecated` facade，代理呼叫至前述新類別，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java`

**Checkpoint**: 使用者故事 1 的拆分已完成，舊 API 均已橋接且所有新舊測試 100% 通過。

---

## Phase 4: User Story 2 - 並發安全性與例外與安全修復 (Priority: P2)

**Goal**: 最佳化 `CallManager` 同步效能，改善 SSL 異常處理，並為 `DefaultTestingTrustManager` 引入簽章指紋防護。

**Independent Test**: 執行並發壓力測試，確認高並發下效能提升至少 20% 且無執行緒競態；驗證簽章憑證不符時會拋出 `SecurityException`。

### Tests for User Story 2

- [X] T011 [P] [US2] 撰寫並發壓力測試，驗證 `CallManager` 效能與安全性，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/CallManagerConcurrencyTest.java`
- [X] T012 [P] [US2] 撰寫憑證簽章驗證單元測試，驗證異常觸發，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManagerTest.java`

### Implementation for User Story 2

- [X] T013 [US2] 重構 `CallManager` 移除方法級同步鎖，改以 `ConcurrentHashMap` 原生原子操作實作並發，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/CallManager.java`
- [X] T014 [US2] 重構 `SupportAllTlsSocketFactory` 補足例外處理，將其輸出至 `EZLogger.warn`，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java`
- [X] T015 [US2] 實作 `DefaultTestingTrustManager` 執行時期簽章憑證 SHA-256 雜湊驗證與防禦，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManager.java`

**Checkpoint**: 使用者故事 2 實作完成，高並發效能表現達標，安全漏洞已獲防禦且測試通過。

---

## Phase 5: User Story 3 - 設計解耦、無意義泛型與自動解壓處理 (Priority: P3)

**Goal**: 解耦 `EZCallback`，清除 `EZRetrofit` 類別層級泛型，移除 Helper 的快取共享狀態，防止 gzip 雙重解壓縮。

**Independent Test**: 單元測試驗證 `EZCallback` 注入、快取隔離行為與 `SafeGzipInterceptor` 跳過自動解壓的行為。

### Tests for User Story 3

- [X] T016 [P] [US3] 撰寫 `EZCallback` 與 `EZRetrofitHelper` 的快取隔離測試，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/EZCallbackTest.java`
- [X] T017 [P] [US3] 撰寫 `SafeGzipInterceptor` 雙重解壓跳過測試，檔案路徑為 `src/test/java/tw/idv/laiis/ezretrofit/SafeGzipInterceptorTest.java`

### Implementation for User Story 3

- [X] T018 [US3] 重構 `EZRetrofit` 去除類別泛型 `<T>`，改為 `public final class`，泛型移至方法級，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java`
- [X] T019 [US3] 重構 `EZRetrofitHelper`，移除快取共享狀態 `_RetrofitMap`，改由配置統一管理，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofitHelper.java`
- [X] T020 [US3] 解耦 `EZCallback` 與 `CallManager` 單例之依賴，改為透過回呼介面注入處理，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/EZCallback.java`
- [X] T021 [US3] 重構 `SafeGzipInterceptor` 判斷 Request 中是否不含 `Accept-Encoding` 標頭以跳過手動解壓，檔案路徑為 `src/main/java/tw/idv/laiis/ezretrofit/SafeGzipInterceptor.java`

**Checkpoint**: 使用者故事 3 實作完成，解耦與設計清理均已通過單元測試。

---

## Phase N: Polish & Cross-Cutting Concerns

**Purpose**: 文件與程式碼品質精雕

- [X] T022 [P] 更新 README 描述新版子套件 Fluent API 與生命週期管理用法，檔案路徑為 `README.md`
- [X] T023 執行靜態分析工具 checkstyle/PMD 確保無 God Class 警告且類別職責度良好
- [X] T024 執行完整單元測試套件，確保所有 24 項任務皆通過測試，且整體測試行覆蓋率至少達 80%

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: 無相依，可立即啟動。
- **Foundational (Phase 2)**: 依賴 Phase 1 完成。阻塞後續所有使用者故事實作。
- **User Stories (Phase 3+)**: 均依賴 Phase 2 完成。
  - 使用者故事間為高獨立性，可同步並行開發，或依優先順序（US1 → US2 → US3）進行。
- **Polish (Phase N)**: 依賴所有使用者故事完成。

### User Story Dependencies

- **User Story 1 (P1)**: 依賴 Foundation (Phase 2) 結束。無其他故事相依，可直接完成 MVP 交付。
- **User Story 2 (P2)**: 依賴 Foundation (Phase 2) 結束。可獨立實作並驗證。
- **User Story 3 (P3)**: 依賴 Foundation (Phase 2) 結束。可獨立實作並驗證。

### Within Each User Story

- 測試案例必須先於實作項目撰寫（測試優先 / TDD 模式）。
- 資料結構模型與設定類別先於業務邏輯服務實作。
- 核心實作先於整合對接。

### Parallel Opportunities

- Phase 1、Phase 2 中標記為 `[P]` 的任務可完全並行（如 T003）。
- 新增套件設定的單元測試（T005、T006 等）可並行撰寫。
- 當 Foundation (Phase 2) 完成後，US1、US2、US3 的實作可指派給多位工程師同時並行開發。

---

## Parallel Example: User Story 1

```bash
# 同時開發獨立的新類別測試與骨架：
Task: "T005 [P] [US1] 撰寫新設定類別單元測試，檔案路徑為 src/test/java/tw/idv/laiis/ezretrofit/config/EZRetrofitConfigTest.java"
Task: "T006 [P] [US1] 撰寫新客戶端單元測試，檔案路徑為 src/test/java/tw/idv/laiis/ezretrofit/client/EZRetrofitClientTest.java"

# 同時開發獨立的設定中心與客戶端：
Task: "T007 [P] [US1] 實作全局設定中心 EZRetrofitConfig，檔案路徑為 src/main/java/tw/idv/laiis/ezretrofit/config/EZRetrofitConfig.java"
Task: "T008 [P] [US1] 實作 OkHttpClient 與 Retrofit 建置器 EZRetrofitClient，檔案路徑為 src/main/java/tw/idv/laiis/ezretrofit/client/EZRetrofitClient.java"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. 完成 Phase 1: Setup。
2. 完成 Phase 2: Foundational（阻塞性基礎建設）。
3. 完成 Phase 3: User Story 1（架構拆分與 deprecated facade 向下相容）。
4. **驗證與交付**：執行 Legacy 與新單元測試確認 API 相容，在此階段即可交付 MVP 版本。

### Incremental Delivery

1. **增量 1 (Foundation)**：完成設定模型子類別與日誌層級擴充，奠定基礎。
2. **增量 2 (MVP/US1)**：架構拆分，確保向下相容，此時呼叫端已可用新架構。
3. **增量 3 (US2)**：並發安全性與簽章安全性防禦。
4. **增量 4 (US3)**：清理泛型、解耦回呼與 SafeGzip。
5. 每次增量結束皆可獨立部署/演示，不影響既有功能。
