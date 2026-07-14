# Implementation Plan: Code Review Remediation

**Branch**: `spec/code-review-remediation` | **Date**: 2026-07-15 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `specs/003-code-review-remediation/spec.md`

## Summary

本計畫旨在針對 `EZRetrofit` 函式庫進行架構重構與安全性修復。核心設計包含：
1. 將原本職責過度集中的 `EZRetrofit` 類別拆分為單一職責之類別（`EZRetrofitConfig`、`EZRetrofitClient`、`EZRetrofitLifecycle`），並將配置類別 `RetrofitConf` 拆分為領域配置物件，統一放置於子套件 `tw.idv.laiis.ezretrofit.config` 與 `tw.idv.laiis.ezretrofit.client` 中。
2. 最佳化 `CallManager` 的並發控制，移除方法級同步鎖，改採執行緒安全的原子操作以提升效能 ≥ 20%。
3. 解除 `EZCallback` 與 `CallManager` 單例的直接依賴，提升單體可測試性。
4. 引入執行時期簽章驗證（SHA-256 雜湊比對）防禦 `DefaultTestingTrustManager` 被反射繞過的漏洞。
5. 在 `SafeGzipInterceptor` 引入對 OkHttp 自動透明解壓縮狀態的偵測，防止雙重解壓縮。
6. 擴充 `EZLogger` 日誌層級。

## Technical Context

- **Language/Version**: Java 8 (sourceCompatibility = 1.8, targetCompatibility = 1.8)
- **Primary Dependencies**: OkHttp 4.12.0, Retrofit 2.12.0, Okio 3.4.0
- **Storage**: N/A (僅有記憶體快取)
- **Testing**: JUnit 5 (junit-jupiter 5.11.0)
- **Target Platform**: JRE 8+ / Android 相容環境
- **Project Type**: Java 8 Class Library
- **Performance Goals**: `CallManager` 在 100 個執行緒同時呼叫的並發高負載場景下，整體執行時間必須比重構前減少至少 20% (SC-003)。
- **Constraints**: 必須維持原有 `EZRetrofit` public static 方法簽章的完全向後相容（作為已標記為 `@Deprecated` 的 facade），在維持現有使用者程式碼完全不需修改的前提下，轉移呼叫至新結構。

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| 憲法準則 | 符合性驗證設計 | 狀態 |
| :--- | :--- | :--- |
| **I. 執行緒安全優先** | 1. `CallManager` 移除 `synchronized` 方法鎖，改以 `ConcurrentHashMap` 的 `computeIfAbsent` 原生原子操作防範 Race Conditions。<br>2. 新增多執行緒並發壓力測試驗證安全性與效能。 | 通過 |
| **II. 預設安全** | `DefaultTestingTrustManager` 除檢測 `BuildConfig.DEBUG` 外，在無 R8/ProGuard 常數內聯時，改以執行時期簽章驗證（取得簽署憑證 SHA-256 雜湊並與開發團隊預設測試金鑰比對），不符則拋出 `SecurityException` | 通過 |
| **III. 測試優先** | 每個新拆分出的類別（`EZRetrofitConfig` 等）均需建立對應的單元測試，行覆蓋率至少達 80% (SC-005)。實作前先撰寫紅燈測試。 | 通過 |
| **IV. 透明的錯誤處理** | 1. 移除 `SupportAllTlsSocketFactory` 的空 catch 區塊，加入 `EZLogger.warn` 的日誌輸出。<br>2. 任何初始狀態錯誤顯式拋出 `IllegalStateException`。 | 通過 |
| **V. 向下相容** | `EZRetrofit` 保留全部既有 API 簽章，新增 `@Deprecated` 註解並在內部代理呼叫至 `EZRetrofitConfig` 與 `EZRetrofitClient`。 | 通過 |
| **VI. Clarification-Driven Development** | 已透過 3 項互動釐清完成所有模糊點，並整合至 `spec.md` 之中。 | 通過 |

## Project Structure

### Documentation (this feature)

```text
specs/003-code-review-remediation/
├── plan.md              # Technical plan (This file)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── api-compatibility.md
└── tasks.md             # Phase 2 output (tasks list)
```

### Source Code (repository root)

```text
src/main/java/tw/idv/laiis/ezretrofit/
├── EZRetrofit.java                        # [Deprecated Facade]
├── EZRetrofitHelper.java                  # [Refactored]
├── EZLogger.java                          # [Refactored]
├── EZCallback.java                        # [Refactored]
├── CallManager.java                       # [Refactored]
├── SafeGzipInterceptor.java               # [Refactored]
├── config/
│   ├── EZRetrofitConfig.java              # [New]
│   ├── SslConfig.java                     # [New]
│   ├── ProxyConfig.java                   # [New]
│   ├── TimeoutConfig.java                 # [New]
│   └── InterceptorConfig.java             # [New]
├── client/
│   ├── EZRetrofitClient.java              # [New]
│   └── EZRetrofitLifecycle.java           # [New]
└── managers/
    └── DefaultTestingTrustManager.java    # [Refactored]
```

**Structure Decision**: 採用專案原有的單一專案結構 (Option 1)。為了避免 God Class 並滿足高內聚原則，在主套件下新增 `config` 與 `client` 子套件以隔離設定模型與通訊客戶端邏輯。

---

## 方案比較與架構評估 (Design Alternatives)

針對類別拆分後的套件歸屬，評估了以下兩個方案：

### 方案 A：放置於獨立子套件 (Chosen)
將拆分出的 `EZRetrofitConfig`、`SslConfig` 等放置於 `tw.idv.laiis.ezretrofit.config`，`EZRetrofitClient` 等放置於 `tw.idv.laiis.ezretrofit.client`。
- **優點**：架構職責邊界清晰，避免主套件目錄過度膨脹，提升可維護性。
- **缺點**：外部直接使用新物件時需新增 `import`。
- **評估結論**：此方案結構化最優，對向下相容的負面影響已由 `EZRetrofit` 的 deprecated facade 徹底消除。

### 方案 B：全部放置於相同主套件
將所有新拆分出的類別維持在 `tw.idv.laiis.ezretrofit` 主套件目錄下。
- **優點**：套件內部類別可直接使用 package-private 存取權限，無須新增 import。
- **缺點**：主套件檔案數量急劇增加，日後架構演進容易失控。
- **評估結論**：不利於長期維護，故捨棄。

---

## Complexity Tracking

*本專案設計完全符合憲法約束，無任何違反或妥協項目。*

| Violation | Why Needed | Simpler Alternative Rejected Because |
| :--- | :--- | :--- |
| 無 | N/A | N/A |
