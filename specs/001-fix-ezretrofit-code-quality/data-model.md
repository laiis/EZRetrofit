# Data Model

## Entities

### EZLogger
- **Type**: Interface
- **Responsibilities**: Pluggable logger interface for library internal events.
- **Methods**: `void warn(String tag, String message, Throwable t)`

### EZRetrofitHelper
- **Type**: Class
- **State**: Non-singleton, thread-safe instance per request.
- **Responsibilities**: Creates and configures Retrofit service instances.

### PersistentCookieStore
- **Type**: Class
- **Responsibilities**: Manages cookie persistence.
- **Rules**: Must remove cookies using cookie `name` as key. `join()` must format strings without trailing commas.

### EZRetrofitTrustManager
- **Type**: Class
- **Responsibilities**: Validates server certificates against SHA-256 pins.

### DefaultTestingTrustManager
- **Type**: Class
- **Responsibilities**: Skips TLS validation for testing.
- **Rules**: Must throw exception if `BuildConfig.DEBUG` is false.

### SSLFactoryManager
- **Type**: Class
- **Responsibilities**: Configures SSL.
- **Rules**: `build()` must throw exception on incomplete config instead of returning null.
