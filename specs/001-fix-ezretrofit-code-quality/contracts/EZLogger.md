# EZLogger Contract

```java
public interface EZLogger {
    /**
     * Log a warning message.
     * @param tag Logging tag
     * @param message Warning message
     * @param t Optional throwable
     */
    void warn(String tag, String message, Throwable t);
}
```
*Note: Default implementation writes to `System.err` if not provided.*
