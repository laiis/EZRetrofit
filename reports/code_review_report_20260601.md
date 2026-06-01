# EZRetrofit Code Review 報告

> **日期**：2026-06-01  
> **審查範圍**：`src/main/java`（16 檔案）、`src/test/java`（11 檔案）、`build.gradle`  
> **審查人**：AI_captain + 3 位專門審查代理（安全/Cookie、核心 API、測試品質）

---

## 摘要統計

| 嚴重度 | 數量 | 說明 |
|--------|------|------|
| 🔴 Critical | 5 | 可能導致安全漏洞、資料損壞或執行時崩潰 |
| 🟡 Warning | 16 | 潛在問題，應在下一版修正 |
| 🟢 Suggestion | 11 | 改善建議，非阻塞性 |

---

## 🔴 Critical 發現

### CR-001：`MessageDigest` 非執行緒安全（安全性）

- **檔案**：[EZRetrofitTrustManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java#L33)
- **問題**：`mMessageDigest` 為共用實例欄位，`MessageDigest` 不具備執行緒安全性。並行呼叫 `checkServerTrusted()` 時，`digest()` 的內部狀態會被破壞，導致 PIN 驗證結果錯誤。
- **風險**：憑證固定（Certificate Pinning）在並行環境下形同虛設。
- **建議**：每次呼叫 `validateCertificatePin()` 時以 `MessageDigest.getInstance("SHA-256")` 取得新實例，或使用 `ThreadLocal<MessageDigest>`。

### CR-002：`BuildConfig.DEBUG` 永遠為 `true`（安全性）

- **檔案**：[build.gradle](file:///D:/pg_workspaces/EZRetrofit/build.gradle#L28)、[DefaultTestingTrustManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/managers/DefaultTestingTrustManager.java#L18)
- **問題**：`buildConfigField("boolean", "DEBUG", "true")` 硬編碼為 `true`，而 `DefaultTestingTrustManager` 的安全護欄依賴此值。在正式發佈的 JAR 中，此「僅限測試用」的信任管理員會跳過所有 SSL 憑證驗證。
- **風險**：正式環境中間人攻擊（MITM）。
- **建議**：改為 `"false"` 作為預設值，或在 ProGuard 規則中排除 `DefaultTestingTrustManager`。

### CR-003：`Retrofit.Builder` 共享導致競態條件

- **檔案**：[EZRetrofit.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L19)
- **問題**：`sBuilder`（`Retrofit.Builder`）是有狀態的可變物件。在 `EZRetrofitHelper.webservice()` 中呼叫 `_Builder.baseUrl(...)` 會修改 builder 的內部狀態。若多執行緒同時使用不同的 webservice class 呼叫 `create()`，`baseUrl` 會被互相覆蓋。
- **風險**：請求可能被送往錯誤的 base URL。
- **建議**：每次 `create()` 產生新的 `Retrofit.Builder` 實例，或在 `webservice()` 中複製 builder。

### CR-004：Java 反序列化漏洞

- **檔案**：[PersistentCookieStore.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L114)
- **問題**：`decodeCookie()` 使用 `ObjectInputStream.readObject()` 直接反序列化 Cookie 資料。若 Cookie 儲存來源被竄改，可執行任意程式碼（Deserialization RCE）。
- **風險**：遠端程式碼執行（若儲存層被攻擊者控制）。
- **建議**：改用安全的序列化格式（如 JSON），或使用 `ObjectInputFilter`（Java 9+）限制可反序列化的類別。

### CR-005：建構子吞噬 `CertificateException` 導致 NPE

- **檔案**：[EZRetrofitTrustManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java#L48-L54)
- **問題**：建構子在 L49 丟出 `CertificateException` 但被 L52 的 `catch (Exception e)` 捕獲後僅印出堆疊追蹤。之後 `mX509TrustManager` 保持為 `null`，在 `getAcceptedIssuers()` 或 `checkClientTrusted()` 呼叫時拋出 `NullPointerException`。
- **風險**：執行時期不明原因的 NPE，真正原因（憑證設定錯誤）被隱藏。
- **建議**：將例外向上傳播，或在建構子失敗時提供明確的初始化錯誤。

---

## 🟡 Warning 發現

### WR-001：`CallManager.enqueue()` 同 tag 呼叫遺漏追蹤

- **檔案**：[CallManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/CallManager.java#L39-L50)
- **問題**：若同一 tag 已存在於 `mCallMap`，新的 call 仍透過 `call.enqueue(callback)` 發送（L50），但未放入 map。`cancel(tag)` 無法取消此未追蹤的 call。
- **建議**：決定是拒絕重複 tag 還是覆蓋舊 call（並取消之）。

### WR-002：`EZCallback` 預設建構子導致 `mTag` 為 null

- **檔案**：[EZCallback.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZCallback.java#L15-L16)
- **問題**：使用預設建構子時 `mTag` 為 `null`，但 `dequeue(null)` 呼叫 `ConcurrentHashMap` 的操作會拋出 `NullPointerException`。
- **建議**：在 `dequeue()` 加入 null 檢查，或移除無參建構子。

### WR-003：Cookie 域名匹配過於寬鬆

- **檔案**：[PersistentCookieStore.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L84)
- **問題**：`uri.host().contains(key)` — 例如 `evil.com` 包含 `il.com`，會錯誤匹配。
- **建議**：改為精確匹配或 RFC 6265 相容的域名後綴匹配。

### WR-004：`mCookies` 非執行緒安全

- **檔案**：[PersistentCookieStore.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L22)
- **問題**：外層 `mCookies` 是普通 `HashMap`，但內層 map 使用 `Collections.synchronizedMap` — 不一致的執行緒安全策略。
- **建議**：統一使用 `ConcurrentHashMap`。

### WR-005：setter 語意不一致（append vs. replace）

- **檔案**：[RetrofitConf.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java#L141-L149)
- **問題**：`setProtocols()` 和 `setInterceptors()` 內部使用 `addAll()` — 多次呼叫會累積而非取代，但命名為 `set*`。
- **建議**：改為先 `clear()` 再 `addAll()`，或改名為 `addProtocols()` / `addInterceptors()`。

### WR-006：Builder 方法拼字錯誤

- **檔案**：[RetrofitConf.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java#L460)
- **問題**：`setProxyAuthencator` 少了一個 `i`（應為 `setProxyAuthenticator`）。另有 `getCertficatePinner`（L124）拼字錯誤。
- **建議**：新增正確拼字的方法，將舊方法標記 `@Deprecated`。

### WR-007：`isInitial()` 無同步保護

- **檔案**：[EZRetrofit.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L182-L184)
- **問題**：讀取 `sRetrofitConf` 未進入 `synchronized (obj)` 區塊，可能看到過時的值。
- **建議**：加入同步或使用 `volatile`（已標記但讀取路徑不一致）。

### WR-008：`ParamCreator.putValue()` null 鍵值不一致

- **檔案**：[ParamCreator.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/ParamCreator.java#L48-L51)
- **問題**：驗證只在 key 與 value 皆非 null 時執行，但 L51 仍將 null key/value 放入 map。
- **建議**：統一處理 null 輸入（拒絕或接受）。

### WR-009：`showParams()` 遍歷 synchronizedMap 無外部同步

- **檔案**：[ParamCreator.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/ParamCreator.java#L62)
- **問題**：根據 `Collections.synchronizedMap` 文件，遍歷時必須手動同步。
- **建議**：加上 `synchronized (mParams)` 區塊。

### WR-010：Gzip 解壓無大小限制

- **檔案**：[SafeGzipInterceptor.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/SafeGzipInterceptor.java#L27)
- **問題**：解壓縮的 GZIP 資料無大小上限，可遭受「gzip bomb」阻斷服務攻擊。
- **建議**：新增最大解壓大小限制。

### WR-011：串流未關閉

- **檔案**：[PersistentCookieStore.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L96-L103)
- **問題**：`encodeCookie()` 和 `decodeCookie()` 中的 `ObjectOutputStream` / `ObjectInputStream` 未使用 try-with-resources 關閉。
- **建議**：使用 try-with-resources 包裝。

### WR-012：`StringBuffer` 應改為 `StringBuilder`

- **檔案**：[CallManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/CallManager.java#L96)
- **問題**：在已同步的區塊內使用 `StringBuffer`（本身是執行緒安全的），造成不必要的效能開銷。
- **建議**：改用 `StringBuilder`。

### WR-013：`SupportAllTlsSocketFactory` 效能問題

- **檔案**：[SupportAllTlsSocketFactory.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java#L74)
- **問題**：`Arrays.asList()` 建立的 List 進行 `contains()` 查詢為 O(n)，每次建立 socket 都重複執行。
- **建議**：改用 `HashSet`。

### WR-014：`EZRetrofitTest` 僅測試快樂路徑

- **檔案**：`src/test/java/.../EZRetrofitTest.java`
- **問題**：缺少 double-init、null config、並行存取等測試案例。
- **建議**：補充邊界條件與並行測試。

### WR-015：壓力測試缺少超時設定

- **檔案**：`GzipConcurrencyStressTest.java`、`HttpsConcurrencyStressTest.java`
- **問題**：無 `@Timeout` 註解，死鎖時測試會無限掛起。
- **建議**：加入 JUnit 5 `@Timeout` 註解。

### WR-016：`currentSupportTls` 未防禦性複製

- **檔案**：[SupportAllTlsSocketFactory.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java#L29)
- **問題**：外部呼叫者可在建構後修改傳入的陣列，影響 socket 建立行為。
- **建議**：在建構子中使用 `Arrays.copyOf()`。

---

## 🟢 Suggestion

| ID | 檔案 | 建議 |
|----|------|------|
| SG-001 | `RetrofitConf.java` | Builder 欄位名稱 `_PascalCase` 不符 Java 慣例，建議改為 `camelCase` |
| SG-002 | `RetrofitConf.java` | `mCertPins`、`mProtocol` 標記為未使用但仍有 public getter/setter — 易造成 API 混淆 |
| SG-003 | `EZRetrofit.java` | `build()` 方法 113 行過長，建議拆分為 `configureOkHttp()` 與 `configureRetrofit()` |
| SG-004 | `EZCallback.java` | 建議將 `mTag` 設為 `final`，強制透過建構子傳入 |
| SG-005 | `EZRetrofitTrustManager.java` | `getSubjectDN()` / `getIssuerDN()` 已棄用，建議改用 `getSubjectX500Principal()` / `getIssuerX500Principal()` |
| SG-006 | `CallManager.java` | `InnerHelper.sCallManager` 使用 holder pattern 但加了不必要的 `volatile` |
| SG-007 | `SerializableHttpCookie.java` | `serialVersionUID` 固定值，Cookie 欄位變更時會靜默破壞反序列化 |
| SG-008 | `SafeGzipInterceptor.java` | gzip 分支的 body source 未明確關閉 |
| SG-009 | `build.gradle` | Java source/target 1.8 已過時，建議升級至 11+ 以獲得更好的 TLS/安全 API |
| SG-010 | 測試 | 缺少 `EZRetrofitHelper`、`ParamCreator`、`CookieJarManager`、`SerializableHttpCookie`、`SupportAllTlsSocketFactory` 的單元測試 |
| SG-011 | 測試 | `PersistentCookieStoreTest` 未測試損壞/惡意資料的反序列化行為 |

---

## 測試覆蓋率分析

| 原始碼檔案 | 有對應測試 | 備註 |
|------------|:----------:|------|
| EZRetrofit.java | ✅ | 僅快樂路徑 |
| RetrofitConf.java | ⚠️ | 透過其他測試間接測試 |
| CallManager.java | ✅ | |
| EZCallback.java | ⚠️ | 透過 CallManagerTest 間接測試 |
| EZRetrofitHelper.java | ❌ | 無直接測試 |
| ParamCreator.java | ❌ | 無直接測試 |
| SafeGzipInterceptor.java | ✅ | 含壓力測試 |
| SupportAllTlsSocketFactory.java | ❌ | 無直接測試 |
| EZLogger.java | ❌ | 介面，可略 |
| LibConfig.java | ❌ | 常數類，可略 |
| CookieJarManager.java | ❌ | 無直接測試 |
| CookieStoreRepo.java | ❌ | 介面，可略 |
| PersistentCookieStore.java | ✅ | 缺邊界案例 |
| SerializableHttpCookie.java | ❌ | 無直接測試 |
| DefaultTestingTrustManager.java | ✅ | DEBUG=true 致失效 |
| EZRetrofitTrustManager.java | ✅ | 缺並行測試 |

---

## 優先修復建議

1. **立即**：修正 `BuildConfig.DEBUG` 硬編碼（CR-002）— 安全性直接影響
2. **立即**：修正 `MessageDigest` 執行緒安全（CR-001）— 並行環境下 PIN 驗證無效
3. **高優先**：修正 `Retrofit.Builder` 共享競態（CR-003）— 多執行緒使用時 base URL 錯亂
4. **高優先**：消除反序列化漏洞（CR-004）— 潛在 RCE
5. **高優先**：修正建構子異常吞噬（CR-005）— 隱藏真實錯誤
6. **中優先**：修正 `CallManager` 同 tag 追蹤遺漏（WR-001）
7. **中優先**：修正 Cookie 域名匹配（WR-003）
8. **低優先**：其餘 Warning 與 Suggestion

---

## 路由判斷卡

✅ 路由正常，判斷卡略（GLOBAL-EVIDENCE-005 已滿足）

**🔄 調度代理**：`workflow_navigator` → `audit_inspector`

### 稽核結果

- `workflow_navigator`：✅ 已調度
- `audit_inspector`：✅ 已調度
- `GLOBAL-EVIDENCE-005`：✅ 已留存

---

## 附錄 A：補充發現（子代理回報整合）

### 追加 Critical

| ID | 檔案 | 問題 |
|----|------|------|
| CR-006 | [CookieJarManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/CookieJarManager.java#L21) | `cookiePolicy` 為 **static** 欄位但在實例建構子中賦值，多實例會互相覆蓋產生競態 |
| CR-007 | [EZRetrofitHelper.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofitHelper.java#L40-L48) | DCL 使用共享的 `_RetrofitMap` 作為鎖，多個 Helper 實例使用不同 Builder 但共享同一 map 時，先進入鎖的 Builder 會決定快取結果，導致後續不同配置的請求取得錯誤的 Retrofit 實例 |
| CR-008 | [EZRetrofitHelper.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofitHelper.java#L43) | `getBaseUrl(clsWebservice)` 若 baseUrl 未註冊回傳 null，`baseUrl(null)` 拋出 `IllegalArgumentException`，缺乏有意義的錯誤訊息 |

### 追加 Warning

| ID | 檔案 | 問題 |
|----|------|------|
| WR-017 | [EZRetrofit.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L15) | `EZRetrofit<T>` 宣告泛型 `T` 但從未在實例層級使用（全為 static 方法），應移除 |
| WR-018 | [EZRetrofit.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L173-L174) | `callFactory` 設定會覆蓋先前構建的 `client`，前面所有 OkHttpClient 配置將失效 |
| WR-019 | [EZRetrofit.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/EZRetrofit.java#L192) | `call(Call call, ...)` 使用 raw type，應為 `Call<?>` |
| WR-020 | [RetrofitConf.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/RetrofitConf.java#L107-L108) | `getCertPins()` 直接回傳內部陣列引用，外部可任意修改，破壞封裝 |
| WR-021 | [EZRetrofitTrustManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java#L125) | `setRevocationEnabled(false)` 停用憑證撤銷檢查，降低安全性 |
| WR-022 | [EZRetrofitTrustManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/managers/EZRetrofitTrustManager.java#L110-L116) | Pin 驗證僅在 catch 分支內，正常信任的憑證不做 pin 驗證——可繞過 pinning |
| WR-023 | [CallManager.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/CallManager.java#L143-L144) | `decrease()` 無下界保護，可能產生負數計數 |
| WR-024 | [PersistentCookieStore.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/cookies/PersistentCookieStore.java#L140-L146) | `hexStringToByteArray` 未驗證奇數長度或非法字元，可能產生 `ArrayIndexOutOfBoundsException` |
| WR-025 | [SupportAllTlsSocketFactory.java](file:///D:/pg_workspaces/EZRetrofit/src/main/java/tw/idv/laiis/ezretrofit/SupportAllTlsSocketFactory.java#L81) | `enabledList` 可能為空（無交集），導致 `setEnabledCipherSuites(empty)` 拋出 `IllegalArgumentException` |

---

## 附錄 B：build.gradle 問題

| 嚴重度 | 問題 |
|--------|------|
| 🔴 | `DEBUG` 硬編碼為 `true`，ProGuard 後的 jar 包含 debug 行為 |
| 🟡 | Java 8（`sourceCompatibility = 1.8`）已無官方安全更新，建議升級至 11+ |
| 🟡 | `repositories` 在頂層、`buildscript`、`allprojects` 三處重複宣告 |
| 🟡 | 未鎖定依賴版本（無 `dependencyLocking`），建置不可重複 |
| 🟡 | 使用 Groovy DSL 內嵌 Plugin class（L98-117），不利於維護 |
| 🟡 | 無測試覆蓋率報告外掛（如 JaCoCo） |

---

## 附錄 C：測試反模式

| 問題 | 位置 | 說明 |
|------|------|------|
| 🔴 反射測試私有方法 | `EZRetrofitTrustManagerTest` | 透過反射呼叫 `validateCertificatePin`，耦合實作細節 |
| 🔴 反射修改 static final | `DefaultTestingTrustManagerTest` | 嘗試反射修改 `BuildConfig.DEBUG`，JDK 12+ 必定失敗 |
| 🟡 手動 mock Chain | `GzipRobustnessTest` / `GzipConcurrencyStressTest` | 重複實作 `Interceptor.Chain` 匿名類（約 40 行重複碼），應抽取共用 |
| 🟡 try/catch + fail | `SSLFactoryManagerTest` | 應改用 `assertThrows` |
| 🟡 Windows 分支無斷言 | `TemporaryFolderSecurityTest` | Windows 分支僅設定權限無任何斷言 |
| 🟡 非 volatile 欄位 | `CallManagerTest` L74 | `DummyCall.cancelled` 多執行緒下有可見性問題 |
| 🟡 依賴 `System.gc()` | 壓力測試 | `System.gc()` + `Thread.sleep(200)` 不可靠 |

---

## 更新後摘要統計

| 嚴重度 | 數量 |
|--------|------|
| 🔴 Critical | 8 |
| 🟡 Warning | 25 |
| 🟢 Suggestion | 11 |
| 🟡 測試反模式 | 7 |
| 🟡 Build 問題 | 6 |
