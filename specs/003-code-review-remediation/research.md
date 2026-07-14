# Research: Code Review Remediation Decisions

**Branch**: `spec/code-review-remediation` | **Date**: 2026-07-15

## Summary

針對本次規格中三項核心重構技術決策，本文件記錄深度研究成果、設計決策依據及被捨棄的替代方案。

---

## 核心技術研究

### 1. CallManager 並發控制最佳化

* **決策**：改用 `ConcurrentHashMap` 原生原子操作方法。
* **技術依據**：
  在原始實作中，所有對請求對列的存取（`enqueue`、`requestAmount`、`cancel`）均在全域方法層級使用 `synchronized` 關鍵字，這會鎖定整個 `CallManager` 實例，導致 100+ 個執行緒高並發呼叫時，執行緒大量處於 `BLOCKED` 狀態。
  藉由將內部 Map 更改為 `ConcurrentHashMap`，並運用其 `computeIfAbsent` 及 `computeIfPresent` 等原子操作，我們能保證對單一 Key 的存取執行緒安全，且無須鎖定整個對列。
* **替代方案**：
  * **方案 A：全域鎖細粒度化（使用 `ReentrantReadWriteLock`）**
    * *被捨棄理由*：雖然讀寫分離能改善純讀取效能，但在高頻寫入（大量併發請求加入/取消）的場景下，寫鎖競爭依然嚴重，且鎖管理開銷大於 ConcurrentHashMap 原生的分段鎖（Segment Lock）機制。

---

### 2. DefaultTestingTrustManager 執行時期安全驗證

* **決策**：利用 Package 憑證指紋（SHA-256）進行驗證。
* **技術依據**：
  防止惡意程式碼在 Release 產品包中透過 Java 反射技術修改 `BuildConfig.DEBUG` 的常數值，從而啟用不安全的 `DefaultTestingTrustManager`。
  我們在 TrustManager 的建構子中執行以下主動驗證：
  1. 呼叫 Package 經理取得當前執行中 App 的簽署資訊（Signatures）。
  2. 計算簽署憑證的 SHA-256 雜湊指紋。
  3. 比對雜湊指紋是否與開發團隊內置的特定 Debug/測試用憑證雜湊相同。
  4. 若不一致，立即拋出 `SecurityException` 終止程式。
* **替代方案**：
  * **方案 A：單純仰賴 BuildConfig.DEBUG 判斷**
    * *被捨棄理由*：安全防護力極弱。攻擊者極易透過反射、Xposed 框架或 Smali 逆向修改 `BuildConfig.DEBUG` 為 `true`，進而引發 MITM 漏洞。

---

### 3. SafeGzipInterceptor 雙重解壓縮防禦

* **決策**：檢查 Request 是否缺失 `Accept-Encoding` 欄位以判定 OkHttp 自動解壓縮是否啟用。
* **技術依據**：
  當客戶端發出的請求中未顯式設定 `Accept-Encoding` 時，OkHttp 會在應用程式攔截器（Application Interceptor）之後，網路攔截器（Network Interceptor）之前，自動注入 `Accept-Encoding: gzip` 標頭。並在接收到伺服器 Response 後，如果發現 Response 包含 `Content-Encoding: gzip` 且無其他特殊欄位，便會在 BridgeInterceptor 中主動解壓並移除 `Content-Encoding` 和 `Content-Length` 標頭。
  如果 `SafeGzipInterceptor`（作為 Application Interceptor）在 Request 缺失 `Accept-Encoding` 時依然主動在 Request 加上 `Accept-Encoding: gzip` 並在 Response 手動解壓縮，將會與 OkHttp 的自動透明解壓機制衝突，導致嘗試對已被 OkHttp 解壓的 plaintext 再次進行 gzip 解壓（雙重解壓失敗，拋出 `ZipException`）。
  因此，只要發現 Request 中無 `Accept-Encoding` 標頭，即表示 OkHttp 的自動解壓縮處於啟用狀態，`SafeGzipInterceptor` 應跳過手動解壓流程。
* **替代方案**：
  * **方案 A：讀取 Response Body 前導字節（Magic Number）判定**
    * *被捨棄理由*：必須讀取 Response 的二進制串流（InputStream），會產生額外的 I/O 耗費；且若未正確重設串流，會導致後續 JSON 解析器讀取失敗。
