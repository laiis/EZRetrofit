package tw.idv.laiis.ezretrofit.config;

import okhttp3.*;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import tw.idv.laiis.ezretrofit.EZLogger;
import java.net.Proxy;
import java.net.ProxySelector;
import javax.net.ssl.HostnameVerifier;
import java.util.*;
import java.util.concurrent.Executor;

public final class EZRetrofitConfig {
    private final SslConfig sslConfig;
    private final ProxyConfig proxyConfig;
    private final TimeoutConfig timeoutConfig;
    private final InterceptorConfig interceptorConfig;
    
    private final CookieJar cookieJar;
    private final List<Protocol> protocols;
    private final Authenticator authenticator;
    private final ConnectionPool connectionPool;
    private final boolean retryOnConnectionFailure;
    private final Cache cache;
    private final Dispatcher dispatcher;
    private final Dns dns;
    private final boolean followRedirects;
    private final boolean followSslRedirects;
    private final HostnameVerifier hostnameVerifier;
    private final RetrofitConf.PinInterval pinInterval;
    private final List<CallAdapter.Factory> callAdapterFactoryList;
    private final List<Converter.Factory> converterFactoryList;
    private final boolean isValidateEagerly;
    private final Executor executor;
    private final Call.Factory okHttp3Factory;
    private final List<ConnectionSpec> connectionSpecList;
    private final Map<Class<?>, String> webserviceMap;
    
    private final RetrofitConf originalConf;

    private static volatile EZRetrofitConfig sInstance;
    private static final Object lock = new Object();
    
    private static volatile EZLogger sLogger = new EZLogger() {
        @Override
        public void warn(String tag, String message, Throwable t) {
            System.err.println("[" + tag + "] " + message);
            if (t != null) {
                t.printStackTrace(System.err);
            }
        }
    };

    private EZRetrofitConfig(Builder builder) {
        this.sslConfig = builder.sslConfig != null ? builder.sslConfig : new SslConfig.Builder().build();
        this.proxyConfig = builder.proxyConfig != null ? builder.proxyConfig : new ProxyConfig.Builder().build();
        this.timeoutConfig = builder.timeoutConfig != null ? builder.timeoutConfig : new TimeoutConfig();
        this.interceptorConfig = builder.interceptorConfig != null ? builder.interceptorConfig : new InterceptorConfig.Builder().build();
        
        this.cookieJar = builder.cookieJar;
        this.protocols = builder.protocols != null ? builder.protocols : new ArrayList<>();
        this.authenticator = builder.authenticator;
        this.connectionPool = builder.connectionPool;
        this.retryOnConnectionFailure = builder.retryOnConnectionFailure;
        this.cache = builder.cache;
        this.dispatcher = builder.dispatcher;
        this.dns = builder.dns;
        this.followRedirects = builder.followRedirects;
        this.followSslRedirects = builder.followSslRedirects;
        this.hostnameVerifier = builder.hostnameVerifier;
        this.pinInterval = builder.pinInterval;
        this.callAdapterFactoryList = builder.callAdapterFactoryList != null ? builder.callAdapterFactoryList : new ArrayList<>();
        this.converterFactoryList = builder.converterFactoryList != null ? builder.converterFactoryList : new ArrayList<>();
        this.isValidateEagerly = builder.isValidateEagerly;
        this.executor = builder.executor;
        this.okHttp3Factory = builder.okHttp3Factory;
        this.connectionSpecList = builder.connectionSpecList != null ? builder.connectionSpecList : new ArrayList<>();
        this.webserviceMap = builder.webserviceMap != null ? builder.webserviceMap : new HashMap<>();
        this.originalConf = builder.originalConf;
    }

    public static void setLogger(EZLogger logger) {
        synchronized (lock) {
            if (logger != null) {
                sLogger = logger;
            }
        }
    }

    public static EZLogger getLogger() {
        return sLogger;
    }

    public static void initial(EZRetrofitConfig config) {
        synchronized (lock) {
            sInstance = config;
        }
    }

    public static void reset() {
        synchronized (lock) {
            sInstance = null;
        }
    }

    public static void initial(RetrofitConf retrofitConf) {
        if (retrofitConf == null) {
            throw new IllegalArgumentException("RetrofitConf cannot be null");
        }
        initial(fromRetrofitConf(retrofitConf));
    }

    public static boolean isInitial() {
        return sInstance != null;
    }

    public static void checkInitialStatus() {
        if (!isInitial()) {
            throw new IllegalStateException("You must initial EZRetrofit before you using it.");
        }
    }

    public static EZRetrofitConfig getInstance() {
        return sInstance;
    }

    public static EZRetrofitConfig fromRetrofitConf(RetrofitConf conf) {
        Builder builder = new Builder();
        builder.originalConf = conf;
        
        SslConfig.Builder sslBuilder = new SslConfig.Builder()
            .useSSLCertificatePinning(conf.isUseSSLCertificatePinning())
            .useSSLFactoryManager(conf.isUseSSLFactoryManager())
            .certificatePinner(conf.getCertificatePinner());
        if (conf.getSSLFactoryManager() != null) {
            sslBuilder.sslSocketFactory(conf.getSSLFactoryManager().getSslSocketFactory())
                      .x509TrustManager(conf.getSSLFactoryManager().getX509TrustManager());
        }
        builder.sslConfig(sslBuilder.build());

        ProxyConfig.Builder proxyBuilder = new ProxyConfig.Builder()
            .proxy(conf.getProxy())
            .proxyAuthenticator(conf.getProxyAuthenticator())
            .proxySelector(conf.getProxySelector());
        builder.proxyConfig(proxyBuilder.build());

        builder.timeout(new TimeoutConfig(conf.getTimeout()));

        InterceptorConfig.Builder intBuilder = new InterceptorConfig.Builder();
        if (conf.getInterceptorList() != null) {
            for (Interceptor i : conf.getInterceptorList()) {
                intBuilder.addInterceptor(i);
            }
        }
        if (conf.getNetworkInterceptorList() != null) {
            for (Interceptor i : conf.getNetworkInterceptorList()) {
                intBuilder.addNetworkInterceptor(i);
            }
        }
        builder.interceptorConfig(intBuilder.build());

        builder.cookieJar(conf.getCookieJar());
        if (conf.getProtocols() != null) {
            builder.protocols(conf.getProtocols());
        }
        builder.authenticator(conf.getAuthenticator());
        builder.connectionPool(conf.getConnectionPool());
        builder.retryOnConnectionFailure(conf.isRetryOnConnectionFailure());
        builder.cache(conf.getCache());
        builder.dispatcher(conf.getDispatcher());
        builder.dns(conf.getDns());
        builder.followRedirects(conf.isFollowRedirects());
        builder.followSslRedirects(conf.isFollowSslRedirects());
        builder.hostnameVerifier(conf.getHostnameVerifier());
        builder.pingInterval(conf.getPinInterval());
        
        if (conf.getCallAdapterFactoryList() != null) {
            for (CallAdapter.Factory f : conf.getCallAdapterFactoryList()) {
                builder.addCallAdapterFactory(f);
            }
        }
        if (conf.getConverterFactoryList() != null) {
            for (Converter.Factory f : conf.getConverterFactoryList()) {
                builder.addConverterFactory(f);
            }
        }
        builder.validateEagerly(conf.isValidateEagerly());
        builder.executor(conf.getExecutor());
        builder.okHttp3Factory(conf.getOKHttp3Factory());
        if (conf.getConnectionSpecList() != null) {
            builder.connectionSpecList(conf.getConnectionSpecList());
        }
        
        if (conf.getWebserviceMap() != null) {
            for (Map.Entry<Class<?>, String> entry : conf.getWebserviceMap().entrySet()) {
                builder.addBaseUrlService(entry.getKey(), entry.getValue());
            }
        }
        
        return builder.build();
    }

    public RetrofitConf toRetrofitConf() {
        if (originalConf != null) {
            return originalConf;
        }
        
        RetrofitConf.Builder builder = new RetrofitConf.Builder();
        if (timeoutConfig != null) {
            builder.timeout(timeoutConfig.getTimeoutSeconds());
        }
        if (cookieJar != null) {
            builder.setCookieJar(cookieJar);
        }
        if (protocols != null && !protocols.isEmpty()) {
            builder.setProtocols(protocols);
        }
        if (authenticator != null) {
            builder.setAuthenticator(authenticator);
        }
        if (connectionPool != null) {
            builder.setConnectionPool(connectionPool);
        }
        builder.setRetryOnConnectionFailure(retryOnConnectionFailure);
        if (cache != null) {
            builder.setCache(cache);
        }
        if (dispatcher != null) {
            builder.setDispatcher(dispatcher);
        }
        if (dns != null) {
            builder.setDns(dns);
        }
        builder.setFollowRedirects(followRedirects);
        builder.setFollowSslRedirects(followSslRedirects);
        if (hostnameVerifier != null) {
            builder.setHostnameVerifier(hostnameVerifier);
        }
        if (pinInterval != null) {
            builder.setPinInterval(pinInterval);
        }
        if (proxyConfig != null && proxyConfig.getProxy() != null) {
            builder.setProxy(proxyConfig.getProxy());
        }
        if (proxyConfig != null && proxyConfig.getProxyAuthenticator() != null) {
            builder.setProxyAuthencator(proxyConfig.getProxyAuthenticator());
        }
        if (proxyConfig != null && proxyConfig.getProxySelector() != null) {
            builder.setProxySelector(proxyConfig.getProxySelector());
        }
        if (sslConfig != null) {
            builder.setUseCertificatePinning(sslConfig.isUseSSLCertificatePinning());
            builder.setUseSSLFactoryManager(sslConfig.isUseSSLFactoryManager());
            if (sslConfig.getCertificatePinner() != null) {
                builder.setCertificatePinner(sslConfig.getCertificatePinner());
            }
        }
        
        if (interceptorConfig != null && interceptorConfig.getInterceptorList() != null) {
            builder.setInterceptors(interceptorConfig.getInterceptorList());
        }
        if (interceptorConfig != null && interceptorConfig.getNetworkInterceptorList() != null) {
            builder.setNetworkInterceptorList(interceptorConfig.getNetworkInterceptorList());
        }
        
        if (callAdapterFactoryList != null) {
            for (CallAdapter.Factory f : callAdapterFactoryList) {
                builder.addCallAdapterFactory(f);
            }
        }
        if (converterFactoryList != null) {
            for (Converter.Factory f : converterFactoryList) {
                builder.addConverterFactory(f);
            }
        }
        builder.setValidateEagerly(isValidateEagerly);
        if (executor != null) {
            builder.setExecutor(executor);
        }
        if (okHttp3Factory != null) {
            builder.setOKHttp3Factory(okHttp3Factory);
        }
        if (connectionSpecList != null && !connectionSpecList.isEmpty()) {
            builder.setConnectionSpecList(connectionSpecList);
        }
        
        RetrofitConf conf = builder.build();
        if (webserviceMap != null) {
            for (Map.Entry<Class<?>, String> entry : webserviceMap.entrySet()) {
                conf.addBaseUrlService(entry.getKey(), entry.getValue());
            }
        }
        return conf;
    }

    public SslConfig getSslConfig() { return sslConfig; }
    public ProxyConfig getProxyConfig() { return proxyConfig; }
    public TimeoutConfig getTimeoutConfig() { return timeoutConfig; }
    public InterceptorConfig getInterceptorConfig() { return interceptorConfig; }
    
    public CookieJar getCookieJar() { return cookieJar; }
    public List<Protocol> getProtocols() { return protocols; }
    public Authenticator getAuthenticator() { return authenticator; }
    public ConnectionPool getConnectionPool() { return connectionPool; }
    public boolean isRetryOnConnectionFailure() { return retryOnConnectionFailure; }
    public Cache getCache() { return cache; }
    public Dispatcher getDispatcher() { return dispatcher; }
    public Dns getDns() { return dns; }
    public boolean isFollowRedirects() { return followRedirects; }
    public boolean isFollowSslRedirects() { return followSslRedirects; }
    public HostnameVerifier getHostnameVerifier() { return hostnameVerifier; }
    public RetrofitConf.PinInterval getPinInterval() { return pinInterval; }
    public List<CallAdapter.Factory> getCallAdapterFactoryList() { return callAdapterFactoryList; }
    public List<Converter.Factory> getConverterFactoryList() { return converterFactoryList; }
    public boolean isValidateEagerly() { return isValidateEagerly; }
    public Executor getExecutor() { return executor; }
    public Call.Factory getOKHttp3Factory() { return okHttp3Factory; }
    public List<ConnectionSpec> getConnectionSpecList() { return connectionSpecList; }
    public Map<Class<?>, String> getWebserviceMap() { return webserviceMap; }
    public String getBaseUrl(Class<?> webservice) { return webserviceMap.get(webservice); }

    public static class Builder {
        private SslConfig sslConfig;
        private ProxyConfig proxyConfig;
        private TimeoutConfig timeoutConfig;
        private InterceptorConfig interceptorConfig;
        
        private CookieJar cookieJar;
        private List<Protocol> protocols = new ArrayList<>();
        private Authenticator authenticator;
        private ConnectionPool connectionPool;
        private boolean retryOnConnectionFailure = true;
        private Cache cache;
        private Dispatcher dispatcher;
        private Dns dns;
        private boolean followRedirects = true;
        private boolean followSslRedirects = true;
        private HostnameVerifier hostnameVerifier;
        private RetrofitConf.PinInterval pinInterval;
        private final List<CallAdapter.Factory> callAdapterFactoryList = new ArrayList<>();
        private final List<Converter.Factory> converterFactoryList = new ArrayList<>();
        private boolean isValidateEagerly;
        private Executor executor;
        private Call.Factory okHttp3Factory;
        private List<ConnectionSpec> connectionSpecList = new ArrayList<>();
        private final Map<Class<?>, String> webserviceMap = new HashMap<>();
        private RetrofitConf originalConf;

        public Builder sslConfig(SslConfig sslConfig) {
            this.sslConfig = sslConfig;
            return this;
        }

        public Builder proxyConfig(ProxyConfig proxyConfig) {
            this.proxyConfig = proxyConfig;
            return this;
        }

        public Builder timeout(TimeoutConfig timeoutConfig) {
            this.timeoutConfig = timeoutConfig;
            return this;
        }

        public Builder interceptorConfig(InterceptorConfig interceptorConfig) {
            this.interceptorConfig = interceptorConfig;
            return this;
        }

        public Builder cookieJar(CookieJar cookieJar) {
            this.cookieJar = cookieJar;
            return this;
        }

        public Builder protocols(List<Protocol> protocols) {
            if (protocols != null) {
                this.protocols = new ArrayList<>(protocols);
            }
            return this;
        }

        public Builder authenticator(Authenticator authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public Builder connectionPool(ConnectionPool connectionPool) {
            this.connectionPool = connectionPool;
            return this;
        }

        public Builder retryOnConnectionFailure(boolean retryOnConnectionFailure) {
            this.retryOnConnectionFailure = retryOnConnectionFailure;
            return this;
        }

        public Builder cache(Cache cache) {
            this.cache = cache;
            return this;
        }

        public Builder dispatcher(Dispatcher dispatcher) {
            this.dispatcher = dispatcher;
            return this;
        }

        public Builder dns(Dns dns) {
            this.dns = dns;
            return this;
        }

        public Builder followRedirects(boolean followRedirects) {
            this.followRedirects = followRedirects;
            return this;
        }

        public Builder followSslRedirects(boolean followSslRedirects) {
            this.followSslRedirects = followSslRedirects;
            return this;
        }

        public Builder hostnameVerifier(HostnameVerifier hostnameVerifier) {
            this.hostnameVerifier = hostnameVerifier;
            return this;
        }

        public Builder pingInterval(RetrofitConf.PinInterval pinInterval) {
            this.pinInterval = pinInterval;
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory callAdapterFactory) {
            if (callAdapterFactory != null) {
                this.callAdapterFactoryList.add(callAdapterFactory);
            }
            return this;
        }

        public Builder addConverterFactory(Converter.Factory converterFactory) {
            if (converterFactory != null) {
                this.converterFactoryList.add(converterFactory);
            }
            return this;
        }

        public Builder validateEagerly(boolean validateEagerly) {
            this.isValidateEagerly = validateEagerly;
            return this;
        }

        public Builder executor(Executor executor) {
            this.executor = executor;
            return this;
        }

        public Builder okHttp3Factory(Call.Factory okHttp3Factory) {
            this.okHttp3Factory = okHttp3Factory;
            return this;
        }

        public Builder connectionSpecList(List<ConnectionSpec> connectionSpecList) {
            if (connectionSpecList != null) {
                this.connectionSpecList = new ArrayList<>(connectionSpecList);
            }
            return this;
        }

        public Builder addBaseUrlService(Class<?> webservice, String baseUrl) {
            if (webservice != null && baseUrl != null) {
                this.webserviceMap.put(webservice, baseUrl);
            }
            return this;
        }

        public EZRetrofitConfig build() {
            return new EZRetrofitConfig(this);
        }
    }
}
