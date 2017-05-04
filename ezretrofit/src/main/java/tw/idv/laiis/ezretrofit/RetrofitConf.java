package tw.idv.laiis.ezretrofit;

import android.content.Context;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Authenticator;
import okhttp3.Cache;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import okhttp3.Dns;
import okhttp3.Interceptor;
import okhttp3.Protocol;

/**
 * Created by laiis on 2017/4/27.
 */

public class RetrofitConf {

    private Context mContext;
    private boolean isUseSSL;
    private String[] mCertPins;
    private long mTimeout;
    private CookieHandler mCookieHandler;
    private List<Interceptor> mInterceptorList;
    private List<Interceptor> mNetworkInterceptorList;
    private CertificatePinner mCertificatePinner;
    private boolean isUseSecureRandom;
    private Map<Class<?>, String> mWebserviceMap;
    private List<Protocol> mProtocolList;
    private Authenticator mAuthenticator;
    private ConnectionPool mConnectionPool;
    private boolean isRetryOnConnectionFailure = true;
    private Cache mCache;
    private Dispatcher mDispatcher;
    private Dns mDns;
    private boolean followRedirects = true;

    private RetrofitConf(Context context) {
        this.mContext = context;
        this.mInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mNetworkInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mWebserviceMap = Collections.synchronizedMap(new HashMap<Class<?>, String>());
        this.mProtocolList = Collections.synchronizedList(new ArrayList<Protocol>());
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

    public static class Builder {

        private Context _Context;
        private RetrofitConf _RetrofitConf;

        public Builder(Context context) {
            this._Context = context;
            this._RetrofitConf = new RetrofitConf(_Context);
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
    }
}