package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import tw.idv.laiis.ezretrofit.managers.DefaultTestingTrustManager;
import tw.idv.laiis.ezretrofit.managers.EZRetrofitTrustManager;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.net.Proxy;
import java.net.ProxySelector;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by laiis on 2017/4/27.
 */

public class RetrofitConf {

    @Deprecated
    private boolean isUseSSL;
    private String mProtocol;
    private String[] mCertPins;
    private long mTimeout = 15L; // default
    private CookieJar mCookieJar;
    private List<Interceptor> mInterceptorList;
    private List<Interceptor> mNetworkInterceptorList;
    private CertificatePinner mCertificatePinner;
    private Map<Class<?>, String> mWebserviceMap;
    private List<Protocol> mProtocolList;
    private Authenticator mAuthenticator;
    private ConnectionPool mConnectionPool;
    private boolean isRetryOnConnectionFailure = true;
    private Cache mCache;
    private Dispatcher mDispatcher;
    private Dns mDns;
    private boolean followRedirects = true;
    private boolean followSslRedirects = true;
    private HostnameVerifier mHostnameVerifier;
    private PinInterval mPinInterval;
    private Proxy mProxy;
    private Authenticator mProxyAuthenticator;
    private List<CallAdapter.Factory> mCallAdapterFactoryList;
    private ProxySelector mProxySelector;
    private SocketFactory mSocketFactory;
    private SSLFactoryManager mSSLFactoryManager;
    private List<Converter.Factory> mConverterFactoryList;
    private boolean isValidateEagerly;
    private Executor mExecutor;
    private Call.Factory mOKHttp3Factory;
    private List<ConnectionSpec> mConnectionSpecList;
    private boolean isUseSSLCertificatePinning;
    private boolean isUseSSLFactoryManager;

    private RetrofitConf() {
        this.mInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mNetworkInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mWebserviceMap = Collections.synchronizedMap(new HashMap<Class<?>, String>());
        this.mProtocolList = Collections.synchronizedList(new ArrayList<Protocol>());
        this.mCallAdapterFactoryList = Collections.synchronizedList(new ArrayList<CallAdapter.Factory>());
        this.mConverterFactoryList = Collections.synchronizedList(new ArrayList<Converter.Factory>());
        this.mConnectionSpecList = Collections.synchronizedList(new ArrayList<ConnectionSpec>());
    }

    public void setCookieJar(CookieJar cookieJar) {
        this.mCookieJar = cookieJar;
    }

    public CookieJar getCookieJar() {
        return mCookieJar;
    }

    @Deprecated
    public void enableSSLConnection() {
        isUseSSL = true;
    }

    @Deprecated
    public void disableSSLConnection() {
        isUseSSL = false;
    }

    @Deprecated
    public boolean isUseSSLConnection() {
        return isUseSSL;
    }

    public void setProtocol(String protocol) {
        this.mProtocol = protocol;
    }

    public String getProtocol() {
        return mProtocol;
    }

    public void setCertPins(String[] certPins) {
        this.mCertPins = certPins;
    }

    public String[] getCertPins() {
        return mCertPins;
    }

    public void setTimeout(long timeout) {
        this.mTimeout = timeout;
    }

    public long getTimeout() {
        return mTimeout;
    }

    public void setCertificatePinner(CertificatePinner certificatePinner) {
        this.mCertificatePinner = certificatePinner;
    }

    public CertificatePinner getCertficatePinner() {
        return mCertificatePinner;
    }

    public void addBaseUrlService(Class<?> webservice, String baseUrl) {
        mWebserviceMap.put(webservice, baseUrl);
    }

    public String getBaseUrl(Class<?> webservice) {
        return mWebserviceMap.get(webservice);
    }

    public void setProtocols(List<Protocol> protocolList) {
        mProtocolList.addAll(protocolList);
    }

    public List<Protocol> getProtocols() {
        return mProtocolList;
    }

    public void setInterceptors(List<Interceptor> interceptorList) {
        this.mInterceptorList.addAll(interceptorList);
    }

    public List<Interceptor> getInterceptorList() {
        return mInterceptorList;
    }

    public void setAuthenticator(Authenticator authenticator) {
        this.mAuthenticator = authenticator;
    }

    public Authenticator getAuthenticator() {
        return mAuthenticator;
    }

    public void setConnectionPool(ConnectionPool connectionPool) {
        this.mConnectionPool = connectionPool;
    }

    public ConnectionPool getConnectionPool() {
        return mConnectionPool;
    }

    public void setNetworkInterceptorList(List<Interceptor> networkInterceptorList) {
        this.mNetworkInterceptorList.addAll(networkInterceptorList);
    }

    public List<Interceptor> getNetworkInterceptorList() {
        return mNetworkInterceptorList;
    }

    public void setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
        this.isRetryOnConnectionFailure = retryOnConnectionFailure;
    }

    public boolean isRetryOnConnectionFailure() {
        return isRetryOnConnectionFailure;
    }

    public Cache getCache() {
        return mCache;
    }

    public void setCache(Cache cache) {
        this.mCache = cache;
    }

    public Dispatcher getDispatcher() {
        return mDispatcher;
    }

    public void setDispatcher(Dispatcher dispatcher) {
        this.mDispatcher = dispatcher;
    }

    public Dns getDns() {
        return mDns;
    }

    public void setDns(Dns dns) {
        this.mDns = dns;
    }

    public boolean isFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean isFollowSslRedirects() {
        return followSslRedirects;
    }

    public void setFollowSslRedirects(boolean followSslRedirects) {
        this.followSslRedirects = followSslRedirects;
    }

    public HostnameVerifier getHostnameVerifier() {
        return mHostnameVerifier;
    }

    public void setHostnameVerifier(HostnameVerifier hostnameVerifier) {
        this.mHostnameVerifier = hostnameVerifier;
    }

    public void setPingInterval(PinInterval pingInterval) {
        this.mPinInterval = pingInterval;
    }

    public PinInterval getPinInterval() {
        return mPinInterval;
    }

    public Proxy getProxy() {
        return mProxy;
    }

    public void setProxy(Proxy proxy) {
        this.mProxy = proxy;
    }

    public Authenticator getProxyAuthenticator() {
        return mProxyAuthenticator;
    }

    public void setProxyAuthenticator(Authenticator proxyAuthenticator) {
        this.mProxyAuthenticator = proxyAuthenticator;
    }

    public List<CallAdapter.Factory> getCallAdapterFactoryList() {
        return mCallAdapterFactoryList;
    }

    public void addCallAdapterFactory(CallAdapter.Factory callAdapterFactory) {
        this.mCallAdapterFactoryList.add(callAdapterFactory);
    }

    public ProxySelector getProxySelector() {
        return mProxySelector;
    }

    public void setProxySelector(ProxySelector proxySelector) {
        this.mProxySelector = proxySelector;
    }

    public SocketFactory getSocketFactory() {
        return mSocketFactory;
    }

    public void setSocketFactory(SocketFactory socketFactory) {
        this.mSocketFactory = socketFactory;
    }

    public SSLFactoryManager getSSLFactoryManager() {
        return mSSLFactoryManager;
    }

    public void setSSLFactoryManager(SSLFactoryManager sslFactoryManager) {
        this.mSSLFactoryManager = sslFactoryManager;
    }

    public List<Converter.Factory> getConverterFactoryList() {
        return mConverterFactoryList;
    }

    public void addConverterFactory(Converter.Factory converterFactory) {
        this.mConverterFactoryList.add(converterFactory);
    }

    public boolean isValidateEagerly() {
        return isValidateEagerly;
    }

    public void setValidateEagerly(boolean validateEagerly) {
        isValidateEagerly = validateEagerly;
    }

    public Executor getExecutor() {
        return mExecutor;
    }

    public void setExecutor(Executor executor) {
        this.mExecutor = executor;
    }

    public Call.Factory getOKHttp3Factory() {
        return mOKHttp3Factory;
    }

    public void setOKHttp3Factory(Call.Factory okHttp3Factory) {
        this.mOKHttp3Factory = okHttp3Factory;
    }

    public List<ConnectionSpec> getConnectionSpecList() {
        return mConnectionSpecList;
    }

    public void addConnectionSpecs(List<ConnectionSpec> connectionSpecList) {
        this.mConnectionSpecList.addAll(connectionSpecList);
    }

    public void setUseSSLCertificatePinning(boolean useSSLCertificatePinning) {
        this.isUseSSLCertificatePinning = useSSLCertificatePinning;
    }

    public boolean isUseSSLCertificatePinning() {
        return isUseSSLCertificatePinning;
    }

    public void setUseSSLFactoryManager(boolean useSSLFactoryManager) {
        this.isUseSSLFactoryManager = useSSLFactoryManager;
    }

    public boolean isUseSSLFactoryManager() {
        return isUseSSLFactoryManager;
    }

    public static class Builder {

        private RetrofitConf _RetrofitConf;

        public Builder() {
            this._RetrofitConf = new RetrofitConf();
        }

        public RetrofitConf build() {
            return _RetrofitConf;
        }

        public Builder setCertificatePinner(CertificatePinner pinner) {
            _RetrofitConf.setCertificatePinner(pinner);
            return this;
        }

        public Builder baseUrls(Class<?> webservice, String baseUrl) {
            _RetrofitConf.addBaseUrlService(webservice, baseUrl);
            return this;
        }

        public Builder timeout(long timeout) {
            _RetrofitConf.setTimeout(timeout);
            return this;
        }

        @Deprecated
        public Builder setUsingSSL(boolean isUsingSSL) {
            if (isUsingSSL) {
                _RetrofitConf.enableSSLConnection();
            } else {
                _RetrofitConf.disableSSLConnection();
            }
            return this;
        }

        public Builder setCookieJar(CookieJar cookieJar) {
            _RetrofitConf.setCookieJar(cookieJar);
            return this;
        }

        public Builder setProtocols(List<Protocol> protocolList) {
            _RetrofitConf.setProtocols(protocolList);
            return this;
        }

        public Builder setInterceptors(List<Interceptor> interceptorList) {
            _RetrofitConf.setInterceptors(interceptorList);
            return this;
        }

        public Builder setAuthenticator(Authenticator authenticator) {
            _RetrofitConf.setAuthenticator(authenticator);
            return this;
        }

        public Builder setConnectionPool(ConnectionPool connectionPool) {
            _RetrofitConf.setConnectionPool(connectionPool);
            return this;
        }

        public Builder setNetworkInterceptorList(List<Interceptor> networkInterceptorList) {
            _RetrofitConf.setNetworkInterceptorList(networkInterceptorList);
            return this;
        }

        public Builder setRetryOnConnectionFailure(boolean retryOnConnectionFailure) {
            _RetrofitConf.setRetryOnConnectionFailure(retryOnConnectionFailure);
            return this;
        }

        public Builder setCache(Cache cache) {
            _RetrofitConf.setCache(cache);
            return this;
        }

        public Builder setDispatcher(Dispatcher dispatcher) {
            _RetrofitConf.setDispatcher(dispatcher);
            return this;
        }

        public Builder setDns(Dns dns) {
            _RetrofitConf.setDns(dns);
            return this;
        }

        public Builder setFollowRedirects(boolean followRedirects) {
            _RetrofitConf.setFollowRedirects(followRedirects);
            return this;
        }

        public Builder setFollowSslRedirects(boolean followSslRedirects) {
            _RetrofitConf.setFollowSslRedirects(followSslRedirects);
            return this;
        }

        public Builder setHostnameVerifier(HostnameVerifier hostnameVerifier) {
            _RetrofitConf.setHostnameVerifier(hostnameVerifier);
            return this;
        }

        public Builder setPinInterval(PinInterval pinInterval) {
            _RetrofitConf.setPingInterval(pinInterval);
            return this;
        }

        public Builder setProxy(Proxy proxy) {
            _RetrofitConf.setProxy(proxy);
            return this;
        }

        public Builder setProxyAuthencator(Authenticator proxyAuthencator) {
            _RetrofitConf.setProxyAuthenticator(proxyAuthencator);
            return this;
        }

        public Builder addCallAdapterFactory(CallAdapter.Factory callAdapterFactory) {
            _RetrofitConf.addCallAdapterFactory(callAdapterFactory);
            return this;
        }

        public Builder setProxySelector(ProxySelector proxySelector) {
            _RetrofitConf.setProxySelector(proxySelector);
            return this;
        }

        public Builder setSocketFactory(SocketFactory socketFactory) {
            _RetrofitConf.setSocketFactory(socketFactory);
            return this;
        }

        public Builder setSSLFactoryManager(SSLFactoryManager sslFactoryManager) {
            _RetrofitConf.setSSLFactoryManager(sslFactoryManager);
            return this;
        }

        public Builder addConverterFactory(Converter.Factory converterFactory) {
            _RetrofitConf.addConverterFactory(converterFactory);
            return this;
        }

        public Builder setValidateEagerly(boolean validateEagerly) {
            _RetrofitConf.setValidateEagerly(validateEagerly);
            return this;
        }

        public Builder setExecutor(Executor executor) {
            _RetrofitConf.setExecutor(executor);
            return this;
        }

        public Builder setOKHttp3Factory(Call.Factory okHttp3Factory) {
            _RetrofitConf.setOKHttp3Factory(okHttp3Factory);
            return this;
        }

        public Builder setProtocol(String protocol) {
            _RetrofitConf.setProtocol(protocol);
            return this;
        }

        public Builder setConnectionSpecList(List<ConnectionSpec> connectionSpecList) {
            _RetrofitConf.addConnectionSpecs(connectionSpecList);
            return this;
        }

        public Builder setUseCertificatePinning(boolean useCertificatePinning) {
            _RetrofitConf.setUseSSLCertificatePinning(useCertificatePinning);
            return this;
        }

        public Builder setUseSSLFactoryManager(boolean useSSLFactoryManager) {
            _RetrofitConf.setUseSSLFactoryManager(useSSLFactoryManager);
            return this;
        }
    }

    public static class PinInterval {

        private long _Interval;
        private TimeUnit _TimeUnit;

        public PinInterval(long interval, TimeUnit timeunit) {
            this._Interval = interval;
            this._TimeUnit = timeunit;
        }

        public long getInterval() {
            return _Interval;
        }

        public TimeUnit getTimeUnit() {
            return _TimeUnit;
        }
    }

    public static class SSLFactoryManager {

        private SSLSocketFactory _SslSocketFactory;
        private X509TrustManager _X509TrustManager;

        private SSLFactoryManager() {
            _SslSocketFactory = null;
            _X509TrustManager = null;
        }

        public void setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this._SslSocketFactory = sslSocketFactory;
        }

        public void setX509TrustManager(X509TrustManager x509TrustManager) {
            this._X509TrustManager = x509TrustManager;
        }

        public X509TrustManager getX509TrustManager() {
            return _X509TrustManager;
        }

        public SSLSocketFactory getSslSocketFactory() {
            return _SslSocketFactory;
        }

        public static class Builder {

            private String[] _SupportProtocols;
            private KeyManager[] _KeyMgrs;
            private boolean _IsIgnoreSSLVerify;
            private KeyStore _KeyStore;
            private String[] _Pins;
            private String _Algorithm;
            private X509TrustManager _TrustMgr;
            private TlsVersion _TlsVersion;

            public Builder() {

            }

            public Builder setProtocol(TlsVersion tlsVersion) {
                this._TlsVersion = tlsVersion;
                return this;
            }

            public Builder setSupportProtocols(String[] supportProtocols) {
                this._SupportProtocols = supportProtocols;
                return this;
            }

            public Builder setKeyManagers(KeyManager[] keyManagers) {
                this._KeyMgrs = keyManagers;
                return this;
            }

            public Builder setTrustManager(X509TrustManager trustManager) {
                this._TrustMgr = trustManager;
                return this;
            }

            public Builder setIgnoreVerify(boolean isIgnoreSSLVerify) {
                this._IsIgnoreSSLVerify = isIgnoreSSLVerify;
                return this;
            }

            public Builder setKeyStore(KeyStore keyStore) {
                this._KeyStore = keyStore;
                return this;
            }

            public Builder setPins(String[] pins) {
                this._Pins = pins;
                return this;
            }

            public Builder setSecureRandomAlgorithm(String algorithm) {
                this._Algorithm = algorithm;
                return this;
            }

            public SSLFactoryManager build() {
                try {
                    X509TrustManager trustManager = null;

                    if (_IsIgnoreSSLVerify) {
                        trustManager = new DefaultTestingTrustManager();
                    } else {
                        trustManager = new EZRetrofitTrustManager(_KeyStore, _Pins);
                        if (_TrustMgr != null) {
                            trustManager = _TrustMgr;
                        }
                    }
                    X509TrustManager[] x509TrustManagers = new X509TrustManager[]{trustManager};

                    SecureRandom secureRandom = null;
                    try {
                        secureRandom = SecureRandom.getInstance(_Algorithm);
                    } catch (Exception e) {
                        secureRandom = new SecureRandom();
                    }

                    SSLContext sslContext = SSLContext.getInstance(_TlsVersion.javaName());
                    sslContext.init(_KeyMgrs, x509TrustManagers, secureRandom);
                    SupportAllTlsSocketFactory tls12SocketFactory = new SupportAllTlsSocketFactory(_SupportProtocols, sslContext.getSocketFactory());

                    SSLFactoryManager sslFactoryManager = new SSLFactoryManager();
                    sslFactoryManager.setSslSocketFactory(tls12SocketFactory);
                    sslFactoryManager.setX509TrustManager(trustManager);
                    return sslFactoryManager;
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return null;
            }
        }

    }
}