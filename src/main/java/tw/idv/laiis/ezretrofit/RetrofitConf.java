package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import retrofit2.CallAdapter;
import retrofit2.Converter;
import tw.idv.laiis.ezretrofit.managers.EZRetrofitTrustManager;

import javax.net.SocketFactory;
import javax.net.ssl.*;
import java.net.CookieHandler;
import java.net.Proxy;
import java.net.ProxySelector;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by laiis on 2017/4/27.
 */

public class RetrofitConf {

    private boolean isUseSSL;
    private String mProtocol;
    private String[] mCertPins;
    private long mTimeout = 15L; // default
    private CookieHandler mCookieHandler;
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
    private okhttp3.Call.Factory mOKHttp3Factory;

    private RetrofitConf() {
        this.mInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mNetworkInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mWebserviceMap = Collections.synchronizedMap(new HashMap<Class<?>, String>());
        this.mProtocolList = Collections.synchronizedList(new ArrayList<Protocol>());
        this.mCallAdapterFactoryList = Collections.synchronizedList(new ArrayList<CallAdapter.Factory>());
        this.mConverterFactoryList = Collections.synchronizedList(new ArrayList<Converter.Factory>());
    }

    public void setCookieHandler(CookieHandler cookieHandler) {
        this.mCookieHandler = cookieHandler;
    }

    public CookieHandler getCookieHandler() {
        return mCookieHandler;
    }

    public void enableSSLConnection() {
        isUseSSL = true;
    }

    public void disableSSLConnection() {
        isUseSSL = false;
    }

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
        this.mHostnameVerifier = mHostnameVerifier;
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

        public Builder setUsingSSL(boolean isUsingSSL) {
            if (isUsingSSL) {
                _RetrofitConf.enableSSLConnection();
            } else {
                _RetrofitConf.disableSSLConnection();
            }
            return this;
        }

        public Builder setCookieHandler(CookieHandler cookieHandler) {
            _RetrofitConf.setCookieHandler(cookieHandler);
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

    /**
     * 待驗證
     */
    public static class SSLFactoryManager {

        private SSLSocketFactory _SslSocketFactory;
        private X509TrustManager _TrustManager;

        public SSLSocketFactory getSSLSocketFactory(KeyManager keyMgr, String protocol, X509TrustManager trustManager) throws SSLHandshakeException, GeneralSecurityException {
            SSLContext sslContext = SSLContext.getInstance(protocol);
            sslContext.init(new KeyManager[]{keyMgr}, new TrustManager[]{trustManager}, SecureRandom.getInstance("SHA1PRNG"));
            return sslContext.getSocketFactory();
        }

        public SSLFactoryManager(String protocol, KeyManager keyMgr, KeyStore keyStore, String[] pins) {
            try {
                this._TrustManager = new EZRetrofitTrustManager(keyStore, pins);
                this._SslSocketFactory = getSSLSocketFactory(keyMgr, protocol, _TrustManager);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public SSLSocketFactory getSslSocketFactory() {
            return _SslSocketFactory;
        }

        public X509TrustManager getX509TrustManager() {
            return _TrustManager;
        }
    }
}