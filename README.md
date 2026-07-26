# EZRetrofit

[![](https://jitpack.io/v/laiis/EZRetrofit.svg)](https://jitpack.io/#laiis/EZRetrofit)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

EZRetrofit 是一個基於 [Retrofit2](https://square.github.io/retrofit/) 的 Android / Java 網路請求封裝函式庫，旨在簡化 Retrofit 的初始化配置、多服務端點管理及請求生命週期控制。

---

## 功能特色

- **領域分層與 Fluent API**：全架構重構拆分為 `config` 全局設定中心與 `client` 建置器，提供高彈性的 Fluent API
- **簡易初始化與模組化配置**：透過 `EZRetrofitConfig` 統一管理 `SslConfig`、`ProxyConfig`、`TimeoutConfig` 與 `InterceptorConfig`
- **多 Base URL 支援**：可為不同的 Webservice 介面分別設定獨立的 Base URL，並以 `ConcurrentHashMap` 高效快取 Retrofit 實例
- **請求生命週期管理**：透過 `EZRetrofitLifecycle` 與 `CallManager` 以 Tag 追蹤、計數、取消個別或全部進行中的請求
- **統一回呼介面**：`EZCallback` 解耦依賴，將成功、失敗（非 2xx）、例外三種情境明確拆開，降低業務邏輯複雜度
- **SSL / TLS 彈性與安全防護**：支援憑證釘選（Certificate Pinning）、自訂 `SSLSocketFactory`、自訂 TrustManager，以及 `DefaultTestingTrustManager` 執行時期憑證簽章指紋防護
- **高並發安全與無鎖化**：核心對列與 `CallManager` 移除方法級同步鎖，採用 `ConcurrentHashMap` 原生原子操作，大幅提升高並發效能
- **向下相容 Facade**：原 `EZRetrofit` 標記為 `@Deprecated` 代理入口，保證既有專案免修改無縫升級
- **可擴充 Logger**：透過 `EZLogger` 介面自訂日誌輸出（支援 `info`、`debug`、`warn`、`error`），預設輸出至 `System.err`

---

## 依賴版本

| 函式庫 | 版本 |
|---|---|
| Retrofit2 | 2.12.0 |
| Converter-Gson | 2.12.0 |
| Adapter-RxJava2 | 2.12.0 |
| OkHttp3 | 4.12.0 |
| OkHttp3 URLConnection | 4.12.0 |
| OkHttp3 Logging Interceptor | 4.12.0 |
| Okio | 3.4.0 |

---

## 安裝

### Step 1：加入 JitPack 倉庫

**Gradle（`settings.gradle` 或根層 `build.gradle`）**

```groovy
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2：加入依賴

```groovy
dependencies {
    implementation 'com.github.laiis:EZRetrofit:0.2.0'
}
```

---

## 快速開始

### 1. 新版 Fluent API 初始化 (推薦)

透過 `EZRetrofitConfig` 與 `EZRetrofitClient` 進行強型別、模組化的 Fluent API 配置：

```java
// 1. 配置全局與模組端點
EZRetrofitConfig config = EZRetrofitConfig.getInstance();
config.registerBaseUrl(ApiService.class, "https://api.example.com/");
config.setTimeoutConfig(new TimeoutConfig(15L, 30L, 30L));

// 2. 透過 EZRetrofitClient 建立 Service 實例
EZRetrofitClient client = new EZRetrofitClient(config);
ApiService api = client.create(ApiService.class);
```

### 2. 向下相容模式 (Deprecated Facade)

若為既有專案，可繼續使用 `EZRetrofit` 舊有介面（內部將自動橋接至 `EZRetrofitConfig` 與 `EZRetrofitClient`）：

```java
// 舊版初始化方式 (已標記 @Deprecated)
RetrofitConf conf = new RetrofitConf.Builder()
        .baseUrls(ApiService.class, "https://api.example.com/")
        .timeout(30L)
        .build();

EZRetrofit.initial(conf);
ApiService api = EZRetrofit.create(ApiService.class);
```

---

## 請求生命週期與追蹤

全新生命週期管理模組 `EZRetrofitLifecycle` 提供更靈活的請求計數與取消機制：

```java
EZRetrofitLifecycle lifecycle = EZRetrofitLifecycle.getInstance();

// 查詢目前進行中的請求總數
int total = lifecycle.getActiveCallCount();

// 查詢特定 Tag 的進行中請求數
int count = lifecycle.getActiveCallCount("userList");

// 取消特定 Tag 的請求
lifecycle.cancelCalls("userList");

// 取消所有進行中的請求
lifecycle.cancelAllCalls();
```

---

## 異步請求（使用 EZCallback）

```java
Call<List<User>> call = api.getUsers();

// 傳入 Tag ("userList") 即可啟用請求追蹤與生命週期管理
call.enqueue(new EZCallback<List<User>>("userList") {
    @Override
    public void success(Call<List<User>> call, Response<List<User>> response) {
        // HTTP 2xx
        List<User> users = response.body();
    }

    @Override
    public void fail(Call<List<User>> call, Response<List<User>> response) {
        // HTTP 非 2xx（如 4xx、5xx）
        int code = response.code();
    }

    @Override
    public void exception(Call<List<User>> call, Throwable t) {
        // 網路例外、逾時等
        t.printStackTrace();
    }
});
```

---

## SSL / TLS 與安全防護

### 憑證與 TrustManager 配置 (`SslConfig`)

```java
SslConfig sslConfig = new SslConfig(
        mySSLSocketFactory,
        myX509TrustManager,
        myHostnameVerifier
);

config.setSslConfig(sslConfig);
```

### 測試環境防護 (`DefaultTestingTrustManager`)

在開發與測試環境使用 `DefaultTestingTrustManager` 時，系統自動針對產出憑證進行 SHA-256 簽章指紋校驗，防止偽造憑證攻擊：

```java
DefaultTestingTrustManager trustManager = DefaultTestingTrustManager.getInstance();
// 若指紋匹配成功始通過驗證，否則拋出 SecurityException
```

> **警告**：測試 TrustManager 僅供開發與測試環境使用，**切勿**用於正式生產環境。

---

## 自訂 Logger

```java
EZRetrofitConfig.getInstance().setLogger(new EZLogger() {
    @Override
    public void info(String tag, String message) {
        Log.i(tag, message);
    }

    @Override
    public void debug(String tag, String message) {
        Log.d(tag, message);
    }

    @Override
    public void warn(String tag, String message, Throwable t) {
        Log.w(tag, message, t);
    }

    @Override
    public void error(String tag, String message, Throwable t) {
        Log.e(tag, message, t);
    }
});
```

---

## 專案結構

```
src/main/java/tw/idv/laiis/ezretrofit/
├── EZRetrofit.java                 # @Deprecated Facade 代理入口（向下相容）
├── EZRetrofitHelper.java           # 核心與快取介面輔助類別
├── RetrofitConf.java               # 舊版設定 Builder (轉導至 EZRetrofitConfig)
├── CallManager.java                # 請求追蹤與並發取消（無鎖 ConcurrentHashMap 實作）
├── EZCallback.java                 # 統一解耦回呼抽象類別 (success / fail / exception)
├── EZLogger.java                   # 多層級日誌介面 (info / debug / warn / error)
├── LibConfig.java                  # 建置時期常數
├── ParamCreator.java               # 請求參數工具
├── SafeGzipInterceptor.java        # 安全解壓攔截器
├── SupportAllTlsSocketFactory.java # SSLSocketFactory 安全包裝
├── config/                         # [NEW] 領域與全局設定中心
│   ├── EZRetrofitConfig.java       # 全局/模組設定管理
│   ├── InterceptorConfig.java      # 攔截器配置
│   ├── ProxyConfig.java            # 代理伺服器配置
│   ├── SslConfig.java              # SSL 配置
│   └── TimeoutConfig.java          # 逾時時間配置
├── client/                         # [NEW] 客戶端建置與生命週期
│   ├── EZRetrofitClient.java       # Fluent API OkHttpClient 與 Retrofit 建置器
│   └── EZRetrofitLifecycle.java    # 請求計數與生命週期管理
├── cookies/                        # Persistent Cookie 管理
└── managers/                       # TrustManager 實作與安全指紋驗證
```

---

## 建置與測試

需求：
- JDK 8+（執行相容性）；建置環境建議 JDK 21
- Gradle（Wrapper 已包含於專案中）

```bash
# Windows
.\gradlew.bat test jar

# Linux / macOS
./gradlew test jar
```

建置產物位於 `build/libs/EZRetrofit-0.2.0.jar`（已套用 ProGuard 混淆）。

---

## 授權

本專案採用 [Apache License 2.0](LICENSE) 授權。