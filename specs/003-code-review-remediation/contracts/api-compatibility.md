# Interface Contract & API Compatibility

**Branch**: `spec/code-review-remediation` | **Date**: 2026-07-15

## Summary

本文件說明 `EZRetrofit` 重構後公開 API 的向後相容（Backward Compatibility）契約，確保呼叫端不需做任何調整即可無縫升級，同時為新專案提供基於子套件的全新架構 API。

---

## 1. 舊有 API 完全向下相容表 (Legacy API Mapping)

所有舊有 API 均維持在套件 `tw.idv.laiis.ezretrofit.EZRetrofit` 中，標記為 `@Deprecated`，並在內部自動橋接至新實體。

| 原始 API 簽章 | 行為對照說明 | 重構後代理映射對象 |
| :--- | :--- | :--- |
| `public static void initial(RetrofitConf)` | 初始化全局配置，清空快取 Map | `EZRetrofitConfig.initial(RetrofitConf)` |
| `public static void call(Call, EZCallback)` | 發起非同步網路請求並納入生命週期追蹤 | `EZRetrofitLifecycle.call(Call, EZCallback)` |
| `public static int count()` | 取得全局當前進行中之網路請求總數 | `EZRetrofitLifecycle.count()` |
| `public static int count(String tag)` | 取得指定 tag 之網路請求總數 | `EZRetrofitLifecycle.count(tag)` |
| `public static void stop(String tag)` | 取消指定 tag 的網路請求 | `EZRetrofitLifecycle.stop(tag)` |
| `public static void stopAll()` | 取消所有進行中之網路請求 | `EZRetrofitLifecycle.stopAll()` |
| `public static <T> EZRetrofitHelper<T> create(RetrofitConf)` | 建立帶有特定配置的業務網路輔助物件 | `EZRetrofitClient.createHelper(RetrofitConf)` |
| `public static <T> EZRetrofitHelper<T> create()` | 建立帶有全局配置的業務網路輔助物件 | `EZRetrofitClient.createHelper()` |
| `public static <T> T create(Class<T> cls)` | 直接實體化 Retrofit 業務 API 介面 | `EZRetrofitClient.create(cls)` |
| `public static void setLogger(EZLogger)` | 設定全局日誌介面實作 | `EZRetrofitConfig.setLogger(EZLogger)` |
| `public static EZLogger getLogger()` | 取得全局日誌介面實作 | `EZRetrofitConfig.getLogger()` |

---

## 2. 新架構 Fluent API 契約 (New Fluent API)

對於新專案或有重構計畫的呼叫端，建議直接使用子套件下的 Builder 模式及客戶端實例。

### A. 全局配置初始化契約 (`EZRetrofitConfig`)
新版提供分離式領域配置，呼叫端需利用 Builder 完成配置：
```java
// 建立子設定
SslConfig ssl = new SslConfig.Builder()
    .useSSLCertificatePinning(true)
    .certificatePinner(pinner)
    .build();

// 整合為總設定
EZRetrofitConfig config = new EZRetrofitConfig.Builder()
    .sslConfig(ssl)
    .timeout(new TimeoutConfig(15))
    .build();

// 全局初始化
EZRetrofitConfig.initial(config);
```

### B. 業務 API 客戶端宣告契約 (`EZRetrofitClient`)
客戶端可以直接實體化，無須強制綁定靜態單例：
```java
EZRetrofitClient client = new EZRetrofitClient(config);
MyApiService service = client.create(MyApiService.class);
```

---

## 3. 日誌擴充契約 (EZLogger Extension)

`EZLogger` 介面保持向下相容，將既有的 `warn` 作為基底，並順勢新增 `info`、`debug` 與 `error` 方法。預設實作（Default Implementation）輸出至 `System.err` 與 `System.out`。

```java
public interface EZLogger {
    void warn(String tag, String message, Throwable t);
    
    // 新增之日誌層級，簽章與既有 warn() 風格保持完全一致 (FR-013)
    void info(String tag, String message, Throwable t);
    void debug(String tag, String message, Throwable t);
    void error(String tag, String message, Throwable t);
}
```
