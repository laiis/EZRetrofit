package tw.idv.laiis.ezretrofit;

import android.content.Context;

import java.net.CookieHandler;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.CertificatePinner;
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
    private CertificatePinner mCertificatePinner;
    private String mProtocol; // i.e : TLS ...
    private List<String> mEndPointList;
    private boolean isUseSecureRandom;
    private Map<Class<?>, String> mWebserviceMap;
    private List<Protocol> mProtocolList;

    private RetrofitConf(Context context) {
        this.mContext = context;
        this.mEndPointList = Collections.synchronizedList(new ArrayList<String>());
        this.mInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
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

        public Builder setCertPins(String[] pins) {
            _RetrofitConf.setCertPins(pins);
            return this;
        }

        public Builder setProtocols(List<Protocol> protocolList) {
            _RetrofitConf.setProtocols(protocolList);
            return this;
        }
    }
}
