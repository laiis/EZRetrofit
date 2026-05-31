# EZLogger 介面合約 (EZLogger Contract)

```java
public interface EZLogger {
    /**
     * 記錄警告訊息。
     * @param tag 日誌標記 (Logging tag)
     * @param message 警告訊息
     * @param t 選填的異常/錯誤物件 (Throwable)
     */
    void warn(String tag, String message, Throwable t);
}
```
*說明: 若未提供自訂實作，預設實作將寫入至 `System.err`。*
