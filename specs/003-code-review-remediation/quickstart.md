# Quickstart Guide: Code Review Remediation

**Branch**: `spec/code-review-remediation` | **Date**: 2026-07-15

## Summary

本手冊提供重構後的快速入門範例，分別展示相容舊 API 的「相容模式」與採用全新領域拆分架構的「流式配置模式」。

---

## 1. 舊 API 相容模式 (Legacy Compatibility Mode)

現有使用者**無須更改任何程式碼**，以下程式碼可直接正常編譯並在重構後正確執行：

```java
import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.RetrofitConf;

// 1. 使用原本的 RetrofitConf 配置
RetrofitConf conf = new RetrofitConf();
conf.setTimeout(15L);
conf.setFollowRedirects(true);

// 2. 初始化 (呼叫已標記為 @Deprecated 的 static 方法)
EZRetrofit.initial(conf);

// 3. 建立 Webservice 實例
MyApiService service = EZRetrofit.create(MyApiService.class);
```

---

## 2. 新 API 流式配置模式 (New Fluent API Mode)

新專案建議採用拆分後的專屬套件配置，這更符合單一職責與流式界面 (Fluent Interface) 的最佳實踐：

```java
import tw.idv.laiis.ezretrofit.config.EZRetrofitConfig;
import tw.idv.laiis.ezretrofit.config.TimeoutConfig;
import tw.idv.laiis.ezretrofit.config.ProxyConfig;
import tw.idv.laiis.ezretrofit.client.EZRetrofitClient;

// 1. 獨立配置 Timeout
TimeoutConfig timeoutConfig = new TimeoutConfig(30L); // 30 秒

// 2. 獨立配置 Proxy
ProxyConfig proxyConfig = new ProxyConfig.Builder()
    .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.0.0.1", 8080)))
    .build();

// 3. 組裝 EZRetrofitConfig 總設定
EZRetrofitConfig config = new EZRetrofitConfig.Builder()
    .timeoutConfig(timeoutConfig)
    .proxyConfig(proxyConfig)
    .followRedirects(true)
    .build();

// 4. 初始化客戶端並建立業務接口實例
EZRetrofitClient client = new EZRetrofitClient(config);
MyApiService service = client.create(MyApiService.class);
```

---

## 3. 生命週期追蹤與取消 (Request Lifecycle Management)

當需要追蹤或手動停止特定網路請求時，呼叫生命週期控制類別 `EZRetrofitLifecycle`：

```java
import tw.idv.laiis.ezretrofit.client.EZRetrofitLifecycle;
import tw.idv.laiis.ezretrofit.EZCallback;
import retrofit2.Call;

// 1. 初始化生命週期管理實體
EZRetrofitLifecycle lifecycle = new EZRetrofitLifecycle();

// 2. 非同步發送請求
Call<ResponseBody> call = apiService.getUserData("user_123");
lifecycle.call(call, new EZCallback() {
    @Override
    public void onResponse(Call call, Response response) {
        // 成功處理
    }

    @Override
    public void onFailure(Call call, Throwable t) {
        // 失敗處理
    }
});

// 3. 取得當前進行中的請求計數
int activeCount = lifecycle.count();

// 4. 取消指定 Tag 請求或取消所有請求
lifecycle.stop("user_tag");
lifecycle.stopAll();
```
