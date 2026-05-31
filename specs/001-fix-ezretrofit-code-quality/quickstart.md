# 快速上手 (Quickstart)

EZRetrofit 提供了一種安全且執行緒安全的方法來管理 Android 上的 API 呼叫。

## 設定

確保您的專案中已套用 `com.github.gmazzo.buildconfig` 插件，以自動生成 `BuildConfig.DEBUG` 旗標。

## 使用方式

```java
// 1. 執行緒安全地建立 Retrofit 服務
MyService service = EZRetrofit.create(MyService.class);

// 2. 注入自訂日誌記錄器 (選填)
EZRetrofit.setLogger(new EZLogger() {
    @Override
    public void warn(String tag, String message, Throwable t) {
        Log.w(tag, message, t);
    }
});

// 3. SSL 設定 (需要 SHA-256 pin 碼)
RetrofitConf.SSLFactoryManager sslManager = new RetrofitConf.SSLFactoryManager.Builder()
    .setProtocol(okhttp3.TlsVersion.TLS_1_2)
    .setSupportProtocols(new String[]{"TLSv1.2"})
    .build();
```
