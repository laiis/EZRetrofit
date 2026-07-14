package tw.idv.laiis.ezretrofit.config;

public final class TimeoutConfig {
    private final long timeoutSeconds;

    public TimeoutConfig() {
        this.timeoutSeconds = 15L;
    }

    public TimeoutConfig(long timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public long getTimeoutSeconds() {
        return timeoutSeconds;
    }
}
