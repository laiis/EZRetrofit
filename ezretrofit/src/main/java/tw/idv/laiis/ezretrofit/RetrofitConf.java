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

/**
 * Created by laiis on 2017/4/27.
 */

public class RetrofitConf {

    public enum RetrofitType {
        JSON, XML
    }

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

    private RetrofitConf(Context context) {
        this.mContext = context;
        this.mEndPointList = Collections.synchronizedList(new ArrayList<String>());
        this.mInterceptorList = Collections.synchronizedList(new ArrayList<Interceptor>());
        this.mWebserviceMap = Collections.synchronizedMap(new HashMap<Class<?>, String>());
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

    }
}
