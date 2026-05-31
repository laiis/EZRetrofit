# 檢驗清單：安全需求品質 (security.md)

**目的**: 驗證安全設計與防護需求的完整性、清晰度與可量測性，作為實作前對「安全需求」的單元測試。
**建立日期**: 2026-05-31
**功能規格書**: [spec.md](../spec.md)
**實作計畫書**: [plan.md](../plan.md)

## 憑證綁定與加密演算法 (Certificate Pinning & Encryption)

- [x] CHK001 - 是否明確要求必須移除並拒絕 SHA-1 格式的 pin 值，且強制限制使用 SHA-256 雜湊演算法？ [完整性, 規格書 §FR-004]
- [x] CHK002 - SHA-256 pin 的 Base64 標準格式與 "sha256/" 前綴要求是否已無歧義定義？ [清晰度, 計畫書 §1]
- [x] CHK003 - 是否明確指定當憑證綁定驗證失敗時，必須拋出 `CertificateException` 並拒絕連線？ [清晰度, 規格書 §US3]
- [x] CHK004 - 密碼套件（Cipher Suites）是否明確限制只允許使用 OkHttp `ConnectionSpec.MODERN_TLS` 定義的強金鑰清單，嚴禁使用 `getSupportedCipherSuites()` 全量啟用？ [完整性, 規格書 §FR-009]

## 環境防護與誤用防範 (Environment Isolation)

- [x] CHK005 - 測試用途的繞過機制（`DefaultTestingTrustManager`）是否明確指定必須在非開發環境拒絕連線並拋出明確例外？ [安全性, 憲法 §II]
- [x] CHK006 - 判斷開發與非開發環境的依據是否已具體指定為 `BuildConfig.DEBUG` 旗標？ [清晰度, 規格書 §FR-005]
- [x] CHK007 - 是否定義了當 `DefaultTestingTrustManager` 在生產環境被啟動時，必須拋出的特定例外類型（如 `SecurityException`）與明確的警示內容？ [清晰度, 計畫書 §4]
- [x] CHK008 - 是否要求廢除原有的 `LibConfig` 全域除錯變數，以 BuildConfig 統一控管以防配置被惡意篡改？ [一致性, 計畫書 §4]

## 錯誤處理與資訊洩露 (Error Handling & Leak Prevention)

- [x] CHK009 - 是否有明確要求移除所有空的 catch 區塊，以防資安例外事件被吞沒（靜默失敗）？ [完整性, 憲法 §IV]
- [x] CHK010 - 是否指定了 SSL 建立失敗或設定不完整時，系統必須拋出 `IllegalStateException` 而非回傳 null（避免 null 呼叫導致意外洩漏或崩潰）？ [完整性, 規格書 §FR-006]
