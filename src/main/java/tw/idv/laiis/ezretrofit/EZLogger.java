package tw.idv.laiis.ezretrofit;

/**
 * EZLogger 介面合約，定義了程式庫內部日誌輸出的行為。
 */
public interface EZLogger {
    /**
     * 記錄警告訊息。
     *
     * @param tag 日誌標記
     * @param message 警告訊息
     * @param t 異常或錯誤物件，可為 null
     */
    void warn(String tag, String message, Throwable t);
}
