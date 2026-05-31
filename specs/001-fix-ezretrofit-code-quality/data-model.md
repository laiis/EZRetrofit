# 資料模型

## 實體 (Entities)

### EZLogger
- **類型**: 介面 (Interface)
- **職責**: 用於函式庫內部事件的可插拔日誌記錄器介面。
- **方法**: `void warn(String tag, String message, Throwable t)`

### EZRetrofitHelper
- **類型**: 類別 (Class)
- **狀態**: 非單例 (Non-singleton)，每次請求皆回傳獨立且執行緒安全的實例。
- **職責**: 建立並設定 Retrofit 服務實例。

### PersistentCookieStore
- **類型**: 類別 (Class)
- **職責**: 管理 Cookie 的持久化儲存。
- **規則**: 必須使用 Cookie 的 `name` 作為鍵 (key) 來移除 Cookie。`join()` 必須格式化字串且不得包含尾端逗號。

### EZRetrofitTrustManager
- **類型**: 類別 (Class)
- **職責**: 比對 SHA-256 pin 碼以驗證伺服器憑證。

### DefaultTestingTrustManager
- **類型**: 類別 (Class)
- **職責**: 跳過 TLS 驗證以供測試使用。
- **規則**: 當 `BuildConfig.DEBUG` 為 false 時，必須拋出例外。

### SSLFactoryManager
- **類型**: 類別 (Class)
- **職責**: 設定 SSL。
- **規則**: 當設定不完整時，`build()` 必須拋出例外，而非回傳 null。
