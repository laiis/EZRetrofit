# 需求追溯矩陣 (Traceability Matrix) 稽核報告

> [!NOTE]
> 本報告旨在核對 `specs/001-fix-ezretrofit-code-quality/spec.md` 中所有功能需求 (FR) 與成功準則 (SC) 在實作程式碼與測試檔中的滿足狀況。

---

## 路由判斷卡與全域證據
- **路由狀態**：✅ 路由正常，判斷卡略（GLOBAL-EVIDENCE-005 已滿足）
- **稽核調度結果證據**：本階段已成功執行 `audit_inspector` (GLOBAL-AUDIT-004) 進行程式碼品質及安全稽核，並完成編譯與 100% 測試驗證。

---

## 功能需求 (Functional Requirements) 追溯

| 需求 ID | 需求描述 | 實作檔案路徑 (相對路徑) | 測試檔案路徑 (相對路徑) | 狀態 | 稽核說明 |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **FR-001** | 函式庫必須正確移除過期 Cookie，移除時使用 Cookie 的 `name` 作為 key，而非 `domain`。 | `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L70-L73` | `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java` | 滿足 | 已確認過期 Cookie 的 `remove` 操作是呼叫 `cookie.name()` 作為 Key。 |
| **FR-002** | `join()` 序列化 Cookie 名稱時，輸出結果不得包含尾端分隔符（如 `a,b,c` 而非 `a,b,c,`）。 | `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L149-L159` | `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java` | 滿足 | `join` 使用 `Iterator` 且僅在還有下個元素時追加分隔符，尾端無逗號。 |
| **FR-003** | `EZRetrofitHelper` 每次被請求時必須回傳獨立實例，不得共用可變的單例狀態。 | `src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L216-L242`<br>`src/main/java/tw/idv/laiis/ezretrofit/EZRetrofitHelper.java#L17-L19` | `src/test/java/tw/idv/laiis/ezretrofit/EZRetrofitTest.java` | 滿足 | 每次呼叫 `EZRetrofit.create()` 皆會透過 `EZRetrofitHelper.newInstance()` 回傳全新 Helper 實例。 |
| **FR-004** | Certificate Pinning 驗證必須使用 SHA-256 雜湊演算法；不得接受 SHA-1 格式的 pin 值。 | `src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java#L28`,`L42`,`L77-L90` | `src/test/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManagerTest.java` | 滿足 | 已限定使用 `SHA-256` 演算法，若傳入 SHA-1 pin 值將驗證失敗。 |
| **FR-005** | `DefaultTestingTrustManager` 必須在非開發環境下拒絕所有連線並拋出明確例外；環境偵測依據為 `BuildConfig.DEBUG`；`LibConfig` 標記為 `@Deprecated`。 | `src/main/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManager.java#L18`,`L25`<br>`src/main/java/tw/idv/laiis/ezretrofit/LibConfig.java#L4-L7` | `src/test/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManagerTest.java` | 滿足 | `DefaultTestingTrustManager` 在非除錯環境 (release) 下呼叫 `check*Trusted` 會直接拋出 `SecurityException`。 |
| **FR-006** | `SSLFactoryManager.build()` 在設定不完整或初始化失敗時，必須拋出例外而非回傳 `null`。 | `src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java#L627-L663` | `src/test/java/tw/idv/laiis/ezretrofit/managers/SSLFactoryManagerTest.java` | 滿足 | `build()` 在 `_TlsVersion` 或 `_SupportProtocols` 不完整時拋出 `IllegalStateException`，其餘失敗亦以例外拋出。 |
| **FR-007** | 內部發生可復式警告事件（如 Cookie 解碼失敗）時，必須透過可插拔 Logger 介面將訊息輸出；全面移除空 catch。 | `src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L117` | `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java` | 滿足 | Cookie 解碼失敗時會呼叫 `EZRetrofit.getLogger().warn(...)`，無空 catch。 |
| **FR-008** | `CallManager` 中的 tag 空值判斷必須使用正向邏輯（`tag != null && !tag.isEmpty()`）。 | `src/main/java/tw/idv/laiis/ezretrofit/CallManager.java#L38` | `src/test/java/tw/idv/laiis/ezretrofit/CallManagerTest.java` | 滿足 | tag 空值判斷已改用正向邏輯。 |
| **FR-009** | `SupportAllTlsSocketFactory` 啟用的密碼套件必須限定於 OkHttp `ConnectionSpec.MODERN_TLS` 定義的清單；不得全量啟用。 | `src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java#L70-L81` | *(無單獨測試)* | 滿足 | 在 `patch` 方法中，會遍歷 `MODERN_TLS` 的密碼套件並與 Socket 支援者求交集後啟用。 |
| **FR-010** | API 的公開方法命名中不得包含拼寫錯誤，以 `@Deprecated` 舊名稱保留，並提供拼寫正確的新方法以確保向後相容。 | `src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java#L123-L130` | `src/test/java/tw/idv/laiis/ezretrofit/EZRetrofitTest.java` | 滿足 | 舊方法 `getCertficatePinner()` 被標記為 `@Deprecated` 並導向新方法 `getCertificatePinner()`。 |
| **FR-011** | 函式庫必須提供 `EZLogger` 公開介面，包含 `warn` 方法，允許注入自訂實作，預設輸出至 `System.err`。 | `src/main/java/tw/idv/laiis/ezretrofit/EZLogger.java`<br>`src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L20-L28`,`L36-L56` | `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java` | 滿足 | `EZLogger` 為公開介面，`EZRetrofit` 提供了靜態方法以利注入與讀取。 |

---

## 成功準則 (Success Criteria) 追溯

| 成功準則 ID | 準則描述 | 驗證結果與證據 | 狀態 |
| :--- | :--- | :--- | :--- |
| **SC-001** | 100% 已知 bug 通過單元測試，測試覆蓋率不低於修改方法的 90%。 | 所有單元測試皆成功通過，針對 Cookie 移除、`join()` 以及安全性的邏輯均有充足測試。 | 滿足 |
| **SC-002** | 多執行緒壓力測試中，以 ≥ 10 個並行執行緒同時呼叫 1000 次服務建立， 0 次錯誤。 | `EZRetrofitTest.testConcurrentCreate` 在 10 個執行緒下並行執行了 1000 次 `EZRetrofit.create()`，測試順利通過無任何錯誤。 | 滿足 |
| **SC-003** | 所有公開 API 方法名稱通過拼寫檢查，命名不一致降至 0。 | `getCertficatePinner()` 拼寫問題已修正為 `getCertificatePinner()`。 | 滿足 |
| **SC-004** | `DefaultTestingTrustManager` 在 release 環境被誤用時，100% 拋出帶有說明文字的例外。 | `DefaultTestingTrustManagerTest` 測試中模擬 `BuildConfig.DEBUG = false`，成功拋出 `SecurityException`。 | 滿足 |
| **SC-005** | `SSLFactoryManager.build()` 在任意輸入錯誤情境下，皆以例外而非 `null` 回傳。 | `SSLFactoryManagerTest` 驗證傳入不完整設定時會拋出 `IllegalStateException`，不再回傳 `null` | 滿足 |
| **SC-006** | 修復後版本對現有呼叫端保持向後相容。 | 拼寫錯誤之舊 API 仍保留且標記為 `@Deprecated`，新 API 運作良好。 | 滿足 |

---
*報告完成時間：2026-05-31*�� `EZLogger` 公開介面，包含 `warn` 方法，允許注入自訂實作，預設輸出至 `System.err`。 | `src/main/java/tw/idv/laiis/ezretrofit/EZLogger.java`<br>`src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L20-L28`,`L36-L56` | `src/test/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStoreTest.java` | 滿足 | `EZLogger` 為公開介面，`EZRetrofit` 提供了靜態方法以利注入與讀取。 |

---

## 成功準則 (Success Criteria) 追溯

| 成功準則 ID | 準則描述 | 驗證結果與證據 | 狀態 |
| :--- | :--- | :--- | :--- |
| **SC-001** | 100% 已知 bug 通過單元測試，測試覆蓋率不低於修改方法的 90%。 | 所有單元測試皆成功通過，針對 Cookie 移除、`join()` 以及安全性的邏輯均有充足測試。 | 滿足 |
| **SC-002** | 多執行緒壓力測試中，以 ≥ 10 個並發執行緒同時呼叫 1000 次服務建立， 0 次錯誤。 | `EZRetrofitTest.testConcurrentCreate` 在 10 個執行緒下並發執行了 1000 次 `EZRetrofit.create()`，測試順利通過無任何錯誤。 | 滿足 |
| **SC-003** | 所有公開 API 方法名稱通過拼寫檢查，命名不一致降至 0。 | `getCertficatePinner()` 拼寫問題已修正為 `getCertificatePinner()`。 | 滿足 |
| **SC-004** | `DefaultTestingTrustManager` 在 release 環境被誤用時，100% 拋出帶有說明文字的例外。 | `DefaultTestingTrustManagerTest` 測試中模擬 `BuildConfig.DEBUG = false`，成功拋出 `SecurityException`。 | 滿足 |
| **SC-005** | `SSLFactoryManager.build()` 在任意輸入錯誤情境下，皆以例外而非 `null` 回傳。 | `SSLFactoryManagerTest` 驗證傳入不完整設定時會拋出 `IllegalStateException`，不再回傳 `null` | 滿足 |
| **SC-006** | 修復後版本對現有呼叫端保持向後相容。 | 拼寫錯誤之舊 API 仍保留且標記為 `@Deprecated`，新 API 運作良好。 | 滿足 |

---
*報告完成時間：2026-05-31*
