# Requirements Consistency Checklist: Spec vs Plan

**Purpose**: 驗證實作計畫 (plan.md) 與需求規格 (spec.md) 之間的一致性、完整性與向下相容性。
**Created**: 2026-07-15 | **Branch**: `003-code-review-remediation`
**Target Spec**: [spec.md](../spec.md) | **Target Plan**: [plan.md](../plan.md)

---

## 1. 架構拆分一致性 (Architecture Split Consistency)

- [X] CHK001 - 實作計畫中規劃的拆分類別（`EZRetrofitConfig`、`EZRetrofitClient`、`EZRetrofitLifecycle`）是否與規格定義的拆分職責及子套件完全一致？ [Consistency, Spec §FR-001, Plan §Project Structure]
- [X] CHK002 - 領域配置拆分（`SslConfig`、`ProxyConfig` 等）在實作計畫中是否涵蓋規格中列出的所有配置領域？ [Consistency, Spec §FR-003, Plan §Project Structure]
- [X] CHK003 - 實作計畫是否明確繼承了規格中關於保留舊 `EZRetrofit` public static 方法作為 facade 的向下相容規定？ [Consistency, Spec §FR-002, Plan §Constitution Check]

## 2. 執行緒安全與效能指標一致性 (Thread-Safety & Performance Consistency)

- [X] CHK004 - 實作計畫中採用的 `ConcurrentHashMap` 原子操作設計，是否能完全滿足規格中對 `CallManager` 移除方法級同步鎖的硬性約束？ [Consistency, Spec §FR-005, Plan §Summary]
- [X] CHK005 - 實作計畫中的並發壓力測試設計，是否對應規格中「高並發（100 執行緒同時呼叫）下效能提升至少 20%」的可量化指標？ [Measurability, Spec §SC-003, Plan §Technical Context]

## 3. 例外處理與安全防護一致性 (Exception & Security Consistency)

- [X] CHK006 - 實作計畫中有關 `DefaultTestingTrustManager` 執行時期簽章比對的設計，是否完全涵蓋規格中規定的「比對開發團隊特定開發/測試金鑰 SHA-256」安全防護細節？ [Consistency, Spec §FR-011, Plan §Constitution Check]
- [X] CHK007 - `SupportAllTlsSocketFactory.patch()` 的例外處理設計，在實作計畫中是否明確規定輸出警告日誌且無空 catch 區塊，以符合規格？ [Consistency, Spec §FR-010, Plan §Constitution Check]
- [X] CHK008 - 實作計畫中 `SafeGzipInterceptor` 透過檢查 `Accept-Encoding` 缺失來跳過手動解壓的設計，是否完全滿足規格防範雙重解壓的要求？ [Consistency, Spec §FR-012, Plan §Summary]

## 4. 日誌擴充與其他邊界條件一致性 (Logging & Scope Consistency)

- [X] CHK009 - 實作計畫是否明確規劃了 `EZLogger` 的 `info()`、`debug()`、`error()` 日誌層級擴充，以滿足規格中日誌擴充的要求？ [Consistency, Spec §FR-013, Plan §Summary]
- [X] CHK010 - 實作計畫所規劃的修改範圍，是否確實將規格中列為「Out of Scope」的項目排除在實作範圍之外？ [Consistency, Spec §Out of Scope, Plan §Summary]
