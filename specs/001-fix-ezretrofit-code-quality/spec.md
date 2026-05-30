# Feature Specification: EZRetrofit 程式碼品質與安全性修復

**Feature Branch**: `fix/ezretrofit-code-quality`

**Created**: 2026-05-30

**Status**: Draft

**Input**: 依據 `build/reports/code_review.md` 修復 EZRetrofit 函式庫的安全性、執行緒安全、邏輯 bug 及程式碼品質問題。

---

## User Scenarios & Testing *(mandatory)*

### User Story 1 — Cookie 管理正確清除過期資料（Priority: P1）

函式庫使用者透過 `PersistentCookieStore` 管理 Cookie。當收到過期的 Cookie 時，庫應能正確地從記憶體與持久化儲存中移除它，而非讓過期 Cookie 繼續留存並被送出。

**Why this priority**: Cookie 邏輯 bug 會導致過期驗證資訊持續送出伺服器，直接影響業務正確性與資安。

**Independent Test**: 可在不依賴其他元件的情況下，單獨測試 `PersistentCookieStore` 收到已過期 Cookie 後，該 Cookie 不再出現於後續 `get()` 回傳值中。

**Acceptance Scenarios**:

1. **Given** 一個已存入的 Cookie，**When** 接收到同名但已過期的 Cookie，**Then** 該 Cookie 從記憶體 map 中移除，且後續請求不再攜帶此 Cookie。
2. **Given** 多個 Cookie 存在同一 domain，**When** 其中一個過期被移除，**Then** 其他 Cookie 不受影響，仍正常回傳。
3. **Given** `join()` 將 cookie 名稱序列化為字串，**When** 序列化後，**Then** 字串格式為 `a,b,c`（不含尾端逗號）。

---

### User Story 2 — 多執行緒環境下安全建立 Retrofit 服務（Priority: P1）

函式庫使用者在 Android/多執行緒環境中，同時從不同執行緒呼叫 `EZRetrofit.create()` 或 `EZRetrofit.create(Class)`。每次呼叫皆應取得正確對應 baseUrl 的 Retrofit 服務實例，不受其他執行緒影響。

**Why this priority**: Race condition 會導致服務實例使用錯誤的 baseUrl，造成不可預期的 API 呼叫失敗，且難以復現與除錯。

**Independent Test**: 可撰寫並行壓力測試，同時呼叫多個 `create(Class)` 並驗證每個服務實例的 baseUrl 正確對應其傳入的 Class。

**Acceptance Scenarios**:

1. **Given** 多個執行緒同時呼叫 `EZRetrofit.create(ClassA)` 與 `EZRetrofit.create(ClassB)`，**When** 各自取得服務實例，**Then** ClassA 的實例 baseUrl 為 ClassA 設定值，ClassB 亦然，兩者不混用。
2. **Given** `EZRetrofit.initial()` 在執行緒 A 執行，**When** 執行緒 B 同時呼叫 `create()`，**Then** 不發生 NullPointerException 或錯誤的 conf 被讀取。

---

### User Story 3 — 安全性：Certificate Pinning 使用強雜湊演算法（Priority: P2）

函式庫使用者透過 `SSLFactoryManager.Builder` 設定憑證 pinning。pinning 驗證過程應使用業界標準的強雜湊演算法，以確保 pinning 機制具備實際防護能力。

**Why this priority**: SHA-1 已被公認為不安全，若攻擊者能偽造符合 SHA-1 的憑證，pinning 機制形同虛設。

**Independent Test**: 可單獨驗證 `EZRetrofitTrustManager` 的 pin 比對邏輯，提供以 SHA-256 計算的 pin 值並確認驗證通過；提供 SHA-1 pin 值則驗證失敗。

**Acceptance Scenarios**:

1. **Given** 使用者提供 SHA-256 格式的 pin，**When** 伺服器回傳符合的憑證，**Then** 連線建立成功。
2. **Given** 使用者提供 SHA-256 格式的 pin，**When** 伺服器回傳不符的憑證，**Then** 拋出 `CertificateException`，連線拒絕。

---

### User Story 4 — 防止測試用 TrustManager 誤用於正式環境（Priority: P2）

函式庫使用者在開發環境使用 `DefaultTestingTrustManager` 跳過 TLS 驗證以方便測試。函式庫應在使用者於非開發環境啟用此元件時，主動發出明確的錯誤提示，阻止其上線。

**Why this priority**: 若 `DefaultTestingTrustManager` 被用於正式環境，任何 TLS 攻擊（如 MITM）皆可得逞，屬高風險安全漏洞。

**Independent Test**: 可單獨測試 `DefaultTestingTrustManager`：在 debug 模式下可正常運作；在 release/非 debug 模式下呼叫 `checkServerTrusted()` 時拋出明確例外。

**Acceptance Scenarios**:

1. **Given** 函式庫在 debug 模式執行，**When** 使用 `DefaultTestingTrustManager`，**Then** TLS 驗證被跳過，連線正常建立（供測試使用）。
2. **Given** 函式庫在非 debug 模式執行，**When** 嘗試使用 `DefaultTestingTrustManager`，**Then** 拋出含明確說明的例外，阻止連線建立。

---

### User Story 5 — 例外處理透明化，不再靜默失敗（Priority: P3）

函式庫使用者設定 SSL 或解碼 Cookie 時若發生錯誤，應收到明確的錯誤訊息，而非靜默失敗或收到 `null` 回傳值。

**Why this priority**: 靜默失敗讓問題難以追蹤，延長除錯時間，影響使用者體驗。

**Independent Test**: 可單獨測試 `SSLFactoryManager.build()` 在設定不完整時拋出明確例外，以及 `decodeCookie()` 在輸入損毀資料時記錄警告而非靜默回傳 null。

**Acceptance Scenarios**:

1. **Given** `SSLFactoryManager.Builder` 設定不完整（如缺少 TlsVersion），**When** 呼叫 `build()`，**Then** 拋出含說明的例外，而非回傳 `null`。
2. **Given** Cookie 持久化資料損毀，**When** 嘗試解碼，**Then** 記錄警告訊息，跳過該 Cookie 繼續運作，不導致整體崩潰。

---

### Edge Cases

- 當 `EZRetrofit.initial()` 尚未被呼叫，使用者即呼叫 `create()` 時，應拋出清楚的說明例外（現有行為，需確保維持正確）。
- 當 `mPins` 為 null 或空陣列時，`validateCertificatePin()` 不應拋出 NullPointerException。
- 當 Cookie 的 domain map 不含指定 domain 時，`remove()` 操作不應拋出 NullPointerException。
- `cancelAll()` 執行中若有執行緒同時新增 call，不應發生 ConcurrentModificationException。

---

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 函式庫必須正確移除過期 Cookie，移除時使用 Cookie 的 `name` 作為 key，而非 `domain`。
- **FR-002**: `join()` 序列化 Cookie 名稱時，輸出結果不得包含尾端分隔符（如 `a,b,c` 而非 `a,b,c,`）。
- **FR-003**: `EZRetrofitHelper` 每次被請求時必須回傳獨立實例，不得共用可變的 Singleton 狀態。
- **FR-004**: Certificate Pinning 驗證必須使用 SHA-256 雜湊演算法；不得接受 SHA-1 格式的 pin 值。
- **FR-005**: `DefaultTestingTrustManager` 必須在非開發環境下拒絕所有連線並拋出明確例外；環境偵測依據為建構工具自動生成的 `BuildConfig.DEBUG` 旗標（透過 `com.github.gmazzo.buildconfig` 插件產生）；現有 `LibConfig` 類別須標記為 `@Deprecated` 並以 `BuildConfig` 取代。
- **FR-006**: `SSLFactoryManager.build()` 在設定不完整或初始化失敗時，必須拋出例外而非回傳 `null`。
- **FR-007**: 函式庫內部發生可復式警告事件（如 Cookie 解碼失敗）時，必須透過可插拔 Logger 介面將訊息輸出；空 catch 區塊需全面移除，不得静默忽略任何可復式錯誤。
- **FR-011**: 函式庫必須提供 `EZLogger` 公開介面，包含至少 `warn(String tag, String message, Throwable t)` 方法；呼叫端可向 `EZRetrofit` 注入自訂實作；未注入時預設行為為輸出至 `System.err`。
- **FR-008**: `CallManager` 中的 tag 空值判斷必須使用正向邏輯（`tag != null && !tag.isEmpty()`）。
- **FR-009**: `SupportAllTlsSocketFactory` 啟用的 cipher suite 必須限定於 OkHttp `ConnectionSpec.MODERN_TLS` 所定義的清單；不得呼叫 `getSupportedCipherSuites()` 全量啟用。
- **FR-010**: 函式庫 API 的公開方法命名中不得包含拼寫錯誤（如 `getCertficatePinner` 應修正為 `getCertificatePinner`）；修正方式為新增正確拼寫的方法，同時保留舊方法名稱並標記 `@Deprecated`，以確保向後相容。

### Key Entities

- **EZLogger**：函式庫內部事件的可插拔 Logger 介面；呼叫端可自行實作並注入，未注入時以 `System.err` 為預設。
- **EZRetrofitHelper**：負責根據設定組裝並提供 Retrofit 服務實例的輔助類別；每次請求需為獨立實例。
- **PersistentCookieStore**：管理 Cookie 的持久化儲存與讀取；需保證 Cookie 的正確增刪查邏輯。
- **EZRetrofitTrustManager**：自訂憑證信任管理器，負責驗證伺服器憑證；需使用 SHA-256 進行 pin 比對。
- **DefaultTestingTrustManager**：僅供測試使用的 TrustManager；需具備環境防護機制。
- **SSLFactoryManager**：封裝 SSL 設定的管理器；`build()` 失敗時需以例外通知呼叫端。

---

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% 已知的邏輯 bug（Cookie `remove` key 錯誤、`join()` 尾端逗號）通過對應單元測試驗證，測試覆蓋率不低於所修改方法的 90%。
- **SC-002**: 多執行緒壓力測試中，以 ≥ 10 個並發執行緒同時呼叫 `EZRetrofit.create(Class)` 共 1,000 次，0 次發生 baseUrl 錯置或 NullPointerException。
- **SC-003**: 所有公開 API 方法名稱通過拼寫檢查，命名不一致問題降至 0 件。
- **SC-004**: `DefaultTestingTrustManager` 在 release 環境下被誤用時，100% 案例拋出帶有說明文字的例外（不靜默通過）。
- **SC-005**: `SSLFactoryManager.build()` 在任意輸入錯誤情境下，皆以例外而非 `null` 回傳（可驗證：呼叫端不再需要對 `build()` 回傳值進行 null 判斷）。
- **SC-006**: 修復後版本對現有呼叫端保持向後相容（Backward Compatible）——已改名的 API 方法以 `@Deprecated` 舊名稱保留，現有使用者升級後無需修改呼叫程式碼；拼寫正確的新方法名稱同步提供。

---

## Assumptions

- 環境偵測機制以 `com.github.gmazzo.buildconfig` Gradle 插件自動生成 `BuildConfig.DEBUG` 旗標為準；`LibConfig` 類別標記 `@Deprecated` 並移除其 debug 旗標職責，由 `BuildConfig.DEBUG` 完全取代。
- Certificate Pinning 升級至 SHA-256 後，現有使用者需更新 pin 值設定；此破壞性變更屬可接受範圍（安全性優先）。
- `SupportAllTlsSocketFactory` 的 cipher suite 白名單以 OkHttp `ConnectionSpec.MODERN_TLS` 內建清單為準，隨 OkHttp 版本自動更新，無需手動維護。
- 移除 `@Deprecated` 死碼（`isUseSSL` 等）的時機為下一個 major 版本，本次修復範圍不包含移除，僅做標記確認。
- `mProtocol` 與 `mCertPins` 欄位的實際應用邏輯補完不在本次範圍內，本次僅標記為未使用並加上說明。

## Clarifications

### Session 2026-05-30

- Q: SC-006 與 FR-010 矛盾—API 命名拼寫修正如何兼顧向後相容？ → A: 保留舊方法名稱（加 `@Deprecated`），同時新增正確拼寫的方法，不破壞現有呼叫端（Option A）。
- Q: FR-005 開發環境偵測機制為何？ → A: 引入 `com.github.gmazzo.buildconfig` 插件生成 `BuildConfig.DEBUG`；廢除 `LibConfig` 的 debug 旗標職責。
- Q: FR-007 警告訊息的記錄機制為何？ → A: 提供 `EZLogger` 可插拔介面，呼叫端自行注入實作；未注入時預設輸出至 `System.err`（Option C）。
- Q: FR-009 Cipher Suite 白名單來源為何？ → A: 沿用 OkHttp `ConnectionSpec.MODERN_TLS` 內建清單，隨版本自動更新（Option B）。
