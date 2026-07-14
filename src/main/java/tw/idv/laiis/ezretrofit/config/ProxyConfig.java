package tw.idv.laiis.ezretrofit.config;

import java.net.Proxy;
import java.net.ProxySelector;
import okhttp3.Authenticator;

public final class ProxyConfig {
    private final Proxy proxy;
    private final Authenticator proxyAuthenticator;
    private final ProxySelector proxySelector;

    private ProxyConfig(Builder builder) {
        this.proxy = builder.proxy;
        this.proxyAuthenticator = builder.proxyAuthenticator;
        this.proxySelector = builder.proxySelector;
    }

    public Proxy getProxy() {
        return proxy;
    }

    public Authenticator getProxyAuthenticator() {
        return proxyAuthenticator;
    }

    public ProxySelector getProxySelector() {
        return proxySelector;
    }

    public static class Builder {
        private Proxy proxy;
        private Authenticator proxyAuthenticator;
        private ProxySelector proxySelector;

        public Builder proxy(Proxy proxy) {
            this.proxy = proxy;
            return this;
        }

        public Builder proxyAuthenticator(Authenticator proxyAuthenticator) {
            this.proxyAuthenticator = proxyAuthenticator;
            return this;
        }

        public Builder proxySelector(ProxySelector proxySelector) {
            this.proxySelector = proxySelector;
            return this;
        }

        public ProxyConfig build() {
            return new ProxyConfig(this);
        }
    }
}
