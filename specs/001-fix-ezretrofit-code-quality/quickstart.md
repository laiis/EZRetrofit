# Quickstart

EZRetrofit provides a secure and thread-safe way to manage API calls on Android.

## Setup
Ensure `com.github.gmazzo.buildconfig` plugin is applied in your project to generate `BuildConfig.DEBUG`.

## Usage

```java
// 1. Thread-safe creation of Retrofit services
MyService service = EZRetrofit.create(MyService.class);

// 2. Inject custom logger (optional)
EZRetrofit.setLogger(new EZLogger() {
    @Override
    public void warn(String tag, String message, Throwable t) {
        Log.w(tag, message, t);
    }
});

// 3. SSL Configuration (Requires SHA-256 pin)
SSLFactoryManager sslManager = new SSLFactoryManager.Builder()
    .setTlsVersion("TLSv1.2")
    // ...
    .build();
```
