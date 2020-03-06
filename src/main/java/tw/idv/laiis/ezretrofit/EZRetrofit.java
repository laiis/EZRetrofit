package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import retrofit2.Callback;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by laiis on 2017/4/28.
 */

public class EZRetrofit<T> {

    private static volatile Map<Class<?>, Retrofit> sRetrofitMap = Collections.synchronizedMap(new HashMap<Class<?>, Retrofit>());
    private static volatile RetrofitConf sRetrofitConf;
    private static volatile Retrofit.Builder sBuilder;

    private static final Object obj = new Object();

    private EZRetrofit() {

    }

    public static void initial(RetrofitConf retrofitConf) {
        synchronized (obj) {
            sRetrofitMap.clear();
            sRetrofitConf = retrofitConf;
            sBuilder = build(retrofitConf);
        }
    }

    private static Retrofit.Builder build(RetrofitConf retrofitConf) {
        OkHttpClient client = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.followRedirects(retrofitConf.isFollowRedirects());
        builder.followSslRedirects(retrofitConf.isFollowSslRedirects());

        if (retrofitConf.getTimeout() > 0L) {
            builder.connectTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
            builder.readTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
        }

        if (retrofitConf.getAuthenticator() != null) {
            builder.authenticator(retrofitConf.getAuthenticator());
        }

        if (retrofitConf.getCookieJar() != null) {
            builder.cookieJar(retrofitConf.getCookieJar());
        }

        if (retrofitConf.getProtocols() != null && retrofitConf.getProtocols().size() > 0) {
            builder.protocols(retrofitConf.getProtocols());
        }

        if (retrofitConf.getInterceptorList() != null && retrofitConf.getInterceptorList().size() > 0) {
            for (Interceptor interceptor : retrofitConf.getInterceptorList()) {
                builder.addInterceptor(interceptor);
            }
        }

        if (retrofitConf.getConnectionPool() != null) {
            builder.connectionPool(retrofitConf.getConnectionPool());
        }

        if (retrofitConf.getNetworkInterceptorList() != null) {
            for (Interceptor interceptor : retrofitConf.getNetworkInterceptorList()) {
                builder.addNetworkInterceptor(interceptor);
            }
        }

        if (retrofitConf.getCache() != null) {
            builder.cache(retrofitConf.getCache());
        }

        if (retrofitConf.getDispatcher() != null) {
            builder.dispatcher(retrofitConf.getDispatcher());
        }

        if (retrofitConf.getDns() != null) {
            builder.dns(retrofitConf.getDns());
        }

        if (retrofitConf.getHostnameVerifier() != null) {
            builder.hostnameVerifier(retrofitConf.getHostnameVerifier());
        }

        if (retrofitConf.getPinInterval() != null) {
            RetrofitConf.PinInterval pinInterval = retrofitConf.getPinInterval();
            builder.pingInterval(pinInterval.getInterval(), pinInterval.getTimeUnit());
        }

        if (retrofitConf.getProxy() != null) {
            builder.proxy(retrofitConf.getProxy());
        }

        if (retrofitConf.getProxyAuthenticator() != null) {
            builder.proxyAuthenticator(retrofitConf.getProxyAuthenticator());
        }

        if (retrofitConf.getProxySelector() != null) {
            builder.proxySelector(retrofitConf.getProxySelector());
        }

        if (retrofitConf.getSocketFactory() != null) {
            builder.socketFactory(retrofitConf.getSocketFactory());
        }

        if (retrofitConf.isUseSSLCertificatePinning()) {
            builder.certificatePinner(retrofitConf.getCertficatePinner());
        } else if (retrofitConf.isUseSSLFactoryManager()) {
            RetrofitConf.SSLFactoryManager sslFactoryManager = retrofitConf.getSSLFactoryManager();
            builder.sslSocketFactory(sslFactoryManager.getSslSocketFactory(), sslFactoryManager.getX509TrustManager());
        }

        client = builder.build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(client);

        if (retrofitConf.getCallAdapterFactoryList() != null && retrofitConf.getCallAdapterFactoryList().size() > 0) {
            for (CallAdapter.Factory factory : retrofitConf.getCallAdapterFactoryList()) {
                retrofitBuilder.addCallAdapterFactory(factory);
            }
        }

        if (retrofitConf.getConverterFactoryList() != null && retrofitConf.getConverterFactoryList().size() > 0) {
            for (Converter.Factory factory : retrofitConf.getConverterFactoryList()) {
                retrofitBuilder.addConverterFactory(factory);
            }
        }

        if (retrofitConf.getExecutor() != null) {
            retrofitBuilder.callbackExecutor(retrofitConf.getExecutor());
        }

        if (retrofitConf.getOKHttp3Factory() != null) {
            retrofitBuilder.callFactory(retrofitConf.getOKHttp3Factory());
        }

        retrofitBuilder.validateEagerly(retrofitConf.isValidateEagerly());

        return retrofitBuilder;
    }

    public static boolean isInitial() {
        return sRetrofitConf != null;
    }

    public static void checkInitialStatus() {
        if (!isInitial()) {
            throw new RuntimeException("You must initial EZRetrofit before you using it.");
        }
    }

    public static void call(Call call, EZCallback ezCallback) {
        checkInitialStatus();
        CallManager.newInstance().enqueue(call, ezCallback);
    }

    public static int count() {
        checkInitialStatus();
        return CallManager.newInstance().requestAmount();
    }

    public static int count(String tag) {
        checkInitialStatus();
        return CallManager.newInstance().requestAmount(tag);
    }

    public static void stop(String tag) {
        checkInitialStatus();
        CallManager.newInstance().cancel(tag);
    }

    public static void stopAll() {
        checkInitialStatus();
        CallManager.newInstance().cancelAll();
    }

    @SuppressWarnings("unchecked")
    public static <T> EZRetrofitHelper<T> create(RetrofitConf retrofitConf) {
        checkInitialStatus();
        EZRetrofitHelper<T> helper = EZRetrofitHelper.newInstance()
                .setRetrofitBuilder(build(retrofitConf))
                .setRetrofitConf(retrofitConf)
                .setRetrofitMap(sRetrofitMap);
        return helper;
    }

    @SuppressWarnings("unchecked")
    public static <T> EZRetrofitHelper<T> create() {
        checkInitialStatus();
        return (EZRetrofitHelper<T>) EZRetrofitHelper.newInstance()
                .setRetrofitBuilder(sBuilder)
                .setRetrofitConf(sRetrofitConf)
                .setRetrofitMap(sRetrofitMap);
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> cls) {
        checkInitialStatus();
        EZRetrofitHelper<T> helper = EZRetrofitHelper.newInstance()
                .setRetrofitBuilder(sBuilder)
                .setRetrofitConf(sRetrofitConf)
                .setRetrofitMap(sRetrofitMap);
        return helper.webservice(cls);
    }

    public static class EZRetrofitHelper<T> {

        private RetrofitConf _RetrofitConf;
        private Retrofit.Builder _Builder;
        private Map<Class<?>, Retrofit> _RetrofitMap;

        private static final class InnerHelper {
            public static volatile EZRetrofitHelper _sHelper = new EZRetrofitHelper();
        }

        public static EZRetrofitHelper newInstance() {
            return InnerHelper._sHelper;
        }

        EZRetrofitHelper() {

        }

        public EZRetrofitHelper setRetrofitConf(RetrofitConf conf) {
            this._RetrofitConf = conf;
            return this;
        }

        public EZRetrofitHelper setRetrofitBuilder(Retrofit.Builder builder) {
            this._Builder = builder;
            return this;
        }

        public EZRetrofitHelper setRetrofitMap(Map<Class<?>, Retrofit> retrofitMap) {
            this._RetrofitMap = retrofitMap;
            return this;
        }

        public T webservice(Class<T> clsWebservice) {
            if (_RetrofitMap.get(clsWebservice) == null) {
                Retrofit retrofit = _Builder.baseUrl(_RetrofitConf.getBaseUrl(clsWebservice))
                        .build();
                _RetrofitMap.put(clsWebservice, retrofit);
            }

            Retrofit retrofit = _RetrofitMap.get(clsWebservice);

            return retrofit.create(clsWebservice);
        }
    }
}
