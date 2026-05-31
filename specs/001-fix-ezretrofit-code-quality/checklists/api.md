# 檢驗清單：API 合約需求品質 (api.md)

**目的**: 驗證 API 設計與向下相容性需求的完整性、清晰度與可量測性，作為實作前對「API 需求」的單元測試。
**建立日期**: 2026-05-31
**功能規格書**: [spec.md](../spec.md)
**實作計畫書**: [plan.md](../plan.md)

## 向後相容性與廢棄策略 (Backward Compatibility)

- [x] CHK001 - 是否明確要求所有修正拼寫錯誤的 API（如 `getCertficatePinner` 修正為 `getCertificatePinner`）必須保留舊方法簽章並標記為 `@Deprecated`？ [相容性, 規格書 §FR-010]
- [x] CHK002 - 舊的已廢棄方法是否被定義為「直接代理並轉發呼叫至新拼寫方法」以確保二進位與邏輯行為相容？ [清晰度, 計畫書 §3]
- [x] CHK003 - 是否有說明在下一個主版號（Major Version）更新前，嚴禁移除任何已廢棄的舊公開 API 方法？ [完整性, 計畫書 §3]
- [x] CHK004 - 新增與修改的公開方法是否都具備明確的 Javadoc 說明以保障串接端知情權？ [完整性, 憲法 §V]

## 介面設計與依賴注入 (API Interface & Injection)

- [x] CHK005 - 公開的日誌介面 `EZLogger` 是否有完整定義其方法（例如至少包含 `warn(String tag, String message, Throwable t)`）？ [完整性, 規格書 §FR-011]
- [x] CHK006 - 呼叫端注入自訂 Logger 的 `EZRetrofit.setLogger(EZLogger)` API 介面是否已無歧義定義？ [清晰度, 計畫書 §4]
- [x] CHK007 - 是否明確規定當呼叫端未注入自訂 Logger 時，系統的預設日誌路由回退至 `System.err`（而非依賴 Android 特有的 Log API）？ [清晰度, 計畫書 §4]

## 參數與邊界設計 (Method Arguments & Constraints)

- [x] CHK008 - 當呼叫 `EZRetrofit.create()` 但系統尚未進行 `initial()` 初始化時，API 是否明確指定應拋出的特定例外？ [邊界情況, 規格書 §Edge Cases]
- [x] CHK009 - `CallManager` 中的 tag 檢查 API，其正向邏輯判斷規則（`tag != null && !tag.isEmpty()`）是否已清楚指定？ [清晰度, 規格書 §FR-008]
- [x] CHK010 - Cookie 管理 API 中的 `PersistentCookieStore.remove()` 與 `join()` 介面，其回傳值與參數規格是否已正確定義？ [完整性, 規格書 §US1]
