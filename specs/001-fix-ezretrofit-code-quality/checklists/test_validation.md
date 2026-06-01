# 檢驗清單：測試與實作驗證需求品質 (test_validation.md)

**目的**: 驗證測試規格與驗證計畫的清晰度、完整性與可量測性，作為實作前對「測試需求」的單元測試。
**建立日期**: 2026-05-31
**功能規格書**: [spec.md](../spec.md)
**實作計畫書**: [plan.md](../plan.md)

## 測試需求完整性

- [x] CHK001 - 是否明確規定為所有 Bug Fix（如 Cookie 移除金鑰錯誤、join 尾端逗號）撰寫獨立的單元測試？ [完整性, 規格書 §US1, §SC-001]
- [x] CHK002 - 是否明確要求單元測試覆蓋率必須達到修改方法的 90% 以上？ [完整性, 規格書 §SC-001]
- [x] CHK003 - 是否有需求指定測試案例必須在實作前編寫並驗證其為紅燈（TDD 流程）？ [完整性, 憲法 §III]
- [x] CHK004 - 對於 Cookie 解碼損毀資料的容錯，是否定義了測試必須驗證 EZLogger 收到 warning 且流程不中斷？ [完整性, 規格書 §US5]

## 壓力測試與並行驗證品質

- [x] CHK005 - 並行壓力測試是否定義了明確的執行緒數量（如 $\ge 10$ 個執行緒）與重複次數（如 1,000 次）？ [清晰度, 規格書 §SC-002]
- [x] CHK006 - 壓力測試的成功率指標是否定義為 0 次 baseUrl 錯置且無任何 NullPointerException？ [可量測性, 規格書 §SC-002]
- [x] CHK007 - 壓力測試的執行效率限制是否已量化（例如必須在 5,000 毫秒/5 秒內完成）？ [可量測性, 計畫書 §2]
- [x] CHK008 - 是否指定了 CallManager.cancelAll() 的執行緒安全測試，需在多執行緒同時修改 Call 清單時不發生 ConcurrentModificationException？ [邊界情況, 規格書 §Edge Cases]

## 相容性與安全防護驗證

- [x] CHK009 - 是否有測試規格用以驗證已重新命名的公開 API 代理方法（如舊拼寫）能正確將請求轉發至新方法，且二進位特徵保持相容？ [相容性, 計畫書 §3]
- [x] CHK010 - 是否定義了測試案例以驗證在 BuildConfig.DEBUG 為 false（發行模式）時，DefaultTestingTrustManager 100% 拋出 SecurityException？ [安全性, 計畫書 §4]
- [x] CHK011 - 對於憑證綁定（Certificate Pinning），是否定義了測試驗證「正確 SHA-256 pin 通過連線」與「SHA-1 pin 拒絕連線」的對照場景？ [安全性, 規格書 §US3]
- [x] CHK012 - 是否指定了測試當憑證綁定金鑰陣列（mPins）為 null 或空值時，驗證流程應安全返回而不拋出 NullPointerException？ [邊界情況, 計畫書 §1]
