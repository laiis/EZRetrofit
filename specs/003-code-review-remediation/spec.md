# Feature Specification: Code Review Remediation

**Feature Branch**: `spec/code-review-remediation`

**Created**: 2026-06-08

**Status**: Draft

**Input**: User description: "根據 code review 報告修正高嚴重度項目 (A1, A3, T1, D1, S1, E1, F1) 及中嚴重度項目 (A2, A4)"

## User Scenarios & Testing

### User Story 1 - 開發者能驗證架構拆分不影響既有功能 (Priority: P1)

作為維護此函式庫的開發者，當我重構 God Class（A1 拆分 EZRetrofit、A3 拆分 RetrofitConf）後，我需要確保既有 API 簽章與行為完全向後相容，讓現有呼叫端不需修改任何程式碼。

**Why this priority**: 架構拆分影響範圍最大，且是其他修復的基礎，必須優先確保不破壞既有功能。

**Independent Test**: 在拆分前後分別執行完整測試套件，所有測試皆通過即驗證完成。

**Acceptance Scenarios**:

1. **Given** 原始 `EZRetrofit` 類別的所有 public static 方法，**When** 重構後，**Then** 所有方法的簽章（名稱、參數型別、回傳型別）保持不變
2. **Given** 現有測試案例，**When** 在拆分後的程式碼上執行，**Then** 所有測試通過
3. **Given** `RetrofitConf` 的 public 配置方法，**When** 拆分為領域配置類別後，**Then** 呼叫端不需修改程式碼即可取得相同配置結果

---

### User Story 2 - 開發者能改善並發安全性與異常處理 (Priority: P2)

作為使用者在高並發場景下呼叫 API，我希望 CallManager 的同步策略最佳化（T1）、SSL 驗證的例外處理更安全（E1, S1）、執行緒安全的 MessageDigest 修正（T2），以提升效能與穩定性。

**Why this priority**: 這些問題影響執行時期正確性與安全性，但改動範圍較局部。

**Independent Test**: 可針對各項目撰寫單元測試驗證行為修正。

**Acceptance Scenarios**:

1. **Given** `CallManager` 內部使用 `ConcurrentHashMap`，**When** 移除方法層級 `synchronized`，**Then** 多執行緒並發呼叫時效能優於原始版本
2. **Given** `SupportAllTlsSocketFactory.patch()` 發生例外，**When** 不再空 catch，**Then** 至少輸出 warning 日誌
3. **Given** `MessageDigest` 在多執行緒環境下使用，**When** 每次呼叫重新建立實例，**Then** 不再有執行緒競態

---

### User Story 3 - 開發者能消除設計層級的耦合與浪費 (Priority: P3)

作為架構師，我希望解除 EZCallback 對 CallManager Singleton 的直接依賴（D1）、消除 EZRetrofit 無意義的類別層級泛型（A2）、以及隔離 Helper 的可變共享狀態（A4），讓程式碼更具可測試性與可維護性。

**Why this priority**: 這些是設計品質改善，對執行時期功能影響較小，但對長期維護有益。

**Independent Test**: 可透過單元測試驗證各項解耦與清理。

**Acceptance Scenarios**:

1. **Given** `EZCallback`，**When** 改為回呼介面注入而非直接呼叫 `CallManager.newInstance()`，**Then** 可在不啟動 CallManager 的情況下單獨測試 EZCallback
2. **Given** `public class EZRetrofit<T>`，**When** 移除類別層級 `<T>`，**Then** 編譯通過且既有 static 方法簽章不變
3. **Given** `EZRetrofitHelper` 持有的 `_RetrofitMap`，**When** 改由 EZRetrofit 統一管理快取，**Then** 多個 Helper 實例不再共享可變狀態

---

### Edge Cases

- 當重構 `EZRetrofit` 或 `RetrofitConf` 時，若新類別的 public API 與舊有反射呼叫不符，應提供 Deprecated 別名或遷移路徑
- `DefaultTestingTrustManager` 如果只在 debug 模式使用，需確保 release build 完全移除相關程式碼
- `SafeGzipInterceptor` 與 OkHttp 內建 gzip 解壓的潛在衝突需納入驗證範圍（F1）

## Requirements

### Functional Requirements

#### 架構拆分 (A1, A3)

- **FR-001**: `EZRetrofit` MUST 拆分為 `EZRetrofitConfig`（初始化）、`EZRetrofitClient`（OkHttp/Retrofit 建置）、`EZRetrofitLifecycle`（call/stop/count），每個類別職責單一，並放置於獨立的子套件中（如 `tw.idv.laiis.ezretrofit.config` 與 `tw.idv.laiis.ezretrofit.client`）
- **FR-002**: 拆分後的 public API MUST 保持與原始 API 完全向後相容（方法名稱、參數、回傳值不變）
- **FR-003**: `RetrofitConf` MUST 拆分為 `SslConfig`、`ProxyConfig`、`TimeoutConfig`、`InterceptorConfig` 等領域配置類別，並放置於 `tw.idv.laiis.ezretrofit.config` 子套件中
- **FR-004**: 拆分後的所有領域配置類別 MUST 可透過 Builder 模式組合使用

#### 執行緒安全改善 (T1)

- **FR-005**: `CallManager` MUST 改用 `ConcurrentHashMap` 原子方法（`computeIfAbsent`、`computeIfPresent`）替代方法層級 `synchronized`
- **FR-006**: `CallManager` 的 public 方法 MUST 不再宣告 `synchronized`

#### 解耦設計 (D1, A4)

- **FR-007**: `EZCallback` MUST 改為透過回呼介面注入（如 `Runnable onDequeue`）處理 dequeue，而非直接呼叫 `CallManager.newInstance().dequeue()`
- **FR-008**: `EZRetrofitHelper` MUST 不再持有外部傳入的 `_RetrofitMap` 參照，改由 `EZRetrofit` 統一管理快取

#### 無意義泛型清理 (A2)

- **FR-009**: `public class EZRetrofit<T>` MUST 改為 `public final class EZRetrofit`，類別層級泛型 `<T>` 移至方法層級保留

#### 例外與安全修復 (E1, S1, F1)

- **FR-010**: `SupportAllTlsSocketFactory.patch()` MUST 不再使用空 catch 區塊，至少輸出 warning 日誌
- **FR-011**: `DefaultTestingTrustManager` MUST 防止反射繞過 `BuildConfig.DEBUG`：優先透過 ProGuard/R8 內聯常數；若無程式碼壓縮配置，則改以執行時期簽章驗證（比對當前 APK 簽章與開發團隊特定的開發/測試金鑰 SHA-256 是否一致，不符則拋出 `SecurityException`）來保護此類除錯邏輯
- **FR-012**: `SafeGzipInterceptor` MUST 判斷 OkHttp 自動解壓是否已啟用（藉由檢查 Request Headers 是否未包含 `Accept-Encoding` 來判定），若已啟用則跳過自訂 gzip 處理以避免雙重解壓

#### 日誌層級擴充 (F5)

- **FR-013**: `EZLogger` MUST 增加 `info()`、`debug()`、`error()` 三個日誌層級，且與既有 `warn()` 的簽章風格一致

### Key Entities

- **EZRetrofit 系列類別**: 拆分後的 `EZRetrofitConfig`（位於 `tw.idv.laiis.ezretrofit.config`）、`EZRetrofitClient`（位於 `tw.idv.laiis.ezretrofit.client`）、`EZRetrofitLifecycle`（位於 `tw.idv.laiis.ezretrofit.client`）
- **RetrofitConf 領域配置**: 拆分後的 `SslConfig`、`ProxyConfig`、`TimeoutConfig` ,`InterceptorConfig`（均位於 `tw.idv.laiis.ezretrofit.config`）
- **CallManager**: 簡化同步策略，保留單例但移除過度同步
- **EZCallback**: 由靜態依賴改為注入式回呼

## Success Criteria

### Measurable Outcomes

- **SC-001**: 所有 10 項修復（A1, A2, A3, A4, T1, D1, S1, E1, F1, F5）完成後，既有單元測試 100% 通過
- **SC-002**: `EZRetrofit` public static 方法 API 簽章向後相容 — 外部呼叫端不需任何修改
- **SC-003**: `CallManager` 在高並發（100 執行緒同時呼叫）場景下，總執行時間較原始版本提升至少 20%
- **SC-004**: 每個重構後的類別靜態分析（checkstyle/PMD）無 God Class 警告
- **SC-005**: 每個新增類別至少有一個測試案例，且行覆蓋率 ≥ 80%

## Clarifications

### Session 2026-06-08

- Q: 其餘 20 項壞味道是否在 scope 內？ → A: 明確排除，屬於後續規格範圍。
- Q: FR-010 的 warning 日誌沿用 EZLogger.warn() 還是順勢擴充？ → A: 順勢擴充 EZLogger 加入 error()，F5 納入本次 scope。
- Q: 無 ProGuard/R8 時 BuildConfig.DEBUG 的替代防護？ → A: 無 ProGuard/R8 時，改以執行時期簽章驗證確保只有正式簽署的 APK 可執行。
- Q: Deprecated facade 保留多長時間？ → A: 保留一個 minor 版本週期後移除。
- Q: 重構後的新類別是否需要新增測試？ → A: 每個新類別至少一個測試案例，且新類別測試覆蓋率 ≥ 80%。

### Session 2026-07-15

- Q: 拆分後的配置與客戶端類別應放置於何處？ → A: 放置於獨立子套件中（例如 `tw.idv.laiis.ezretrofit.config` 與 `tw.idv.laiis.ezretrofit.client`）。
- Q: DefaultTestingTrustManager 執行時期簽章驗證的具體比對策略為何？ → A: 驗證當前 APK 的簽章雜湊值是否與開發團隊特定的開發/測試金鑰 SHA-256 一致，不一致則拋出 SecurityException。
- Q: SafeGzipInterceptor 如何判斷 OkHttp 自動解壓縮已啟用？ → A: 檢查 Request Headers 中是否未包含 Accept-Encoding 欄位，若是則表示 OkHttp 將執行自動透明解壓縮，應跳過自訂的 gzip 解壓。

## Out of Scope

本規格**不**涵蓋以下 code review 項目，將留待後續規格處理：

- T2、T3（執行緒安全）
- C1–C6（程式碼品質）
- E2、E3（異常處理）
- N1–N4（命名與慣例）
- S3（安全議題）
- F2–F4（未充分分析檔案）
- D2、D3（設計模式與耦合度）

## Assumptions

- 專案已有足夠的單元測試覆蓋率可驗證向後相容性
- 既有 public API 的使用者不願意因重構而修改呼叫端程式碼
- 重構採增量方式，每次 commit 只處理單一職責拆分
- 類別拆分時，舊類別保留為 Deprecated facade 委託給新類別，遷移緩衝期為一個 minor 版本週期後移除
