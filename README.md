# EZRetrofit

[![](https://jitpack.io/v/laiis/EZRetrofit.svg)](https://jitpack.io/#laiis/EZRetrofit)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

EZRetrofit 是一個基於 [Retrofit2](https://square.github.io/retrofit/) 的 Android / Java 網路請求封裝函式庫，旨在簡化 Retrofit 的初始化配置、多服務端點管理及請求生命週期控制。

---

## 功能特色

- **簡易初始化**：透過 `RetrofitConf.Builder` 以鏈式呼叫完成所有 OkHttp / Retrofit 設定
- **多 Base URL 支援**：可為不同的 Webservice 介面分別設定獨立的 Base URL，並以 `ConcurrentHashMap` 快取 Retrofit 實例
- **請求生命週期管理**：透過 `CallManager` 以 Tag 追蹤、計數、取消個別或全部請求
- **統一回呼介面**：`EZCallback` 將成功、失敗（非 2xx）、例外三種情境拆開，減少業務層的判斷邏輯
- **SSL / TLS 彈性配置**：支援憑證釘選（Certificate Pinning）、自訂 `SSLSocketFactory`、自訂 TrustManager，以及測試用的忽略驗證模式
- **執行緒安全**：核心靜態欄位使用 `volatile` + `synchronized`；集合欄位採用 `Collections.synchronized*` 或 `ConcurrentHashMap`
- **ProGuard 混淆整合**：建置流程自動套用 ProGuard，產出經混淆的 JAR
- **可插拔 Logger**：透過 `EZLogger` 介面自訂日誌輸出，預設輸出至 `System.err`

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

### 1. 初始化

在 `Application.onCreate()` 或任何合適的初始化點呼叫一次：

```java
RetrofitConf conf = new RetrofitConf.Builder()
        .baseUrls(ApiService.class, "https://api.example.com/")
        .timeout(30L)                         // 連線／讀取逾時（秒），預設 15
        .setFollowRedirects(true)
        .setFollowSslRedirects(true)
        .build();

EZRetrofit.initial(conf);
```

### 2. 取得 Webservice 實例

```java
// 方式一：直接取得 Retrofit service 代理
ApiService api = EZRetrofit.create(ApiService.class);

// 方式二：透過 EZRetrofitHelper 鏈式建立（可覆寫部分設定）
EZRetrofitHelper<ApiService> helper = EZRetrofit.create();
ApiService api = helper.webservice(ApiService.class);

// 方式三：傳入獨立 RetrofitConf（適用需要不同設定的場景）
RetrofitConf anotherConf = new RetrofitConf.Builder()
        .baseUrls(AnotherService.class, "https://other.example.com/")
        .timeout(60L)
        .build();
EZRetrofitHelper<AnotherService> helper2 = EZRetrofit.create(anotherConf);
```

### 3. 發送請求（使用 EZCallback）

```java
Call<List<User>> call = api.getUsers();

EZRetrofit.call(call, new EZCallback<List<User>>("userList") {
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

> **Tag**：傳入非空字串可啟用請求追蹤，之後可用 `EZRetrofit.stop("userList")` 取消該請求。

### 4. 請求管理

```java
// 查詢目前進行中的請求總數
int total = EZRetrofit.count();

// 查詢特定 Tag 的請求數
int count = EZRetrofit.count("userList");

// 取消特定 Tag 的請求
EZRetrofit.stop("userList");

// 取消所有進行中的請求
EZRetrofit.stopAll();
```

---

## SSL / TLS 進階設定

### 憑證釘選（Certificate Pinning）

```java
CertificatePinner pinner = new CertificatePinner.Builder()
        .add("api.example.com", "sha256/AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=")
        .build();

RetrofitConf conf = new RetrofitConf.Builder()
        .baseUrls(ApiService.class, "https://api.example.com/")
        .setCertificatePinner(pinner)
        .setUseCertificatePinning(true)
        .build();
```

### 自訂 SSLSocketFactory（含 TLS 版本限制）

```java
RetrofitConf.SSLFactoryManager sslManager =
        new RetrofitConf.SSLFactoryManager.Builder()
                .setProtocol(TlsVersion.TLS_1_2)
                .setSupportProtocols(new String[]{"TLSv1.2", "TLSv1.3"})
                .setIgnoreVerify(false)          // 正式環境請設 false
                .build();

RetrofitConf conf = new RetrofitConf.Builder()
        .baseUrls(ApiService.class, "https://api.example.com/")
        .setSSLFactoryManager(sslManager)
        .setUseSSLFactoryManager(true)
        .build();
```

> **警告**：`setIgnoreVerify(true)` 僅供開發／測試環境使用，**切勿**用於正式環境。

---

## Cookie 管理

EZRetrofit 提供內建的持久化 Cookie 實作，適用於 Android 環境：

```java
// PersistentCookieStore 需搭配 Android Context 使用
CookieJar cookieJar = new CookieJarManager(
        new PersistentCookieStore(context)
);

RetrofitConf conf = new RetrofitConf.Builder()
        .baseUrls(ApiService.class, "https://api.example.com/")
        .setCookieJar(cookieJar)
        .build();
```

---

## 自訂 Logger

```java
EZRetrofit.setLogger(new EZLogger() {
    @Override
    public void warn(String tag, String message, Throwable t) {
        Log.w(tag, message, t); // 使用 Android Log 輸出
    }
});
```

---

## 專案結構

```
src/main/java/tw/idv/laiis/ezretrofit/
├── EZRetrofit.java              # 公開靜態入口，初始化與請求發送
├── EZRetrofitHelper.java        # Retrofit 實例建立與快取（Double-Checked Locking）
├── RetrofitConf.java            # 所有 OkHttp / Retrofit 設定的 Builder 類別
├── CallManager.java             # 請求追蹤、計數、取消（Singleton + ConcurrentHashMap）
├── EZCallback.java              # 統一回呼抽象類別（success / fail / exception）
├── EZLogger.java                # 日誌介面
├── LibConfig.java               # 建置時期常數（DEBUG flag 等）
├── ParamCreator.java            # 請求參數工具
├── SupportAllTlsSocketFactory.java # 自訂 SSLSocketFactory，強制指定 TLS 協定
├── cookies/
│   ├── CookieJarManager.java    # OkHttp CookieJar 實作
│   ├── CookieStoreRepo.java     # Cookie 儲存庫介面
│   ├── PersistentCookieStore.java  # Android SharedPreferences 持久化實作
│   └── SerializableHttpCookie.java # 可序列化的 HttpCookie 包裝
└── managers/
    ├── DefaultTestingTrustManager.java  # 測試用：接受所有憑證（勿用於正式環境）
    └── EZRetrofitTrustManager.java      # 自訂 X509TrustManager，支援憑證釘選
```

---

## 建置

需求：
- JDK 8+（執行相容性）；建置環境建議 JDK 21
- Gradle（Wrapper 已包含於專案中）

```bash
# Windows
.\gradlew.bat jar

# Linux / macOS
./gradlew jar
```

建置產物位於 `build/libs/EZRetrofit-0.2.0.jar`（已套用 ProGuard 混淆）。

---

## 授權

本專案採用 [Apache License 2.0](LICENSE) 授權。