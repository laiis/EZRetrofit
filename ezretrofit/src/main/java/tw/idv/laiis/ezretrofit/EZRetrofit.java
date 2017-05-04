package tw.idv.laiis.ezretrofit;

import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import tw.idv.laiis.ezretrofit.managers.CallManager;

/**
 * Created by laiis on 2017/4/28.
 */

public class EZRetrofit<T> {

    @NonNull
    private static volatile Map<Class<?>, Retrofit> sRetrofitMap = Collections.synchronizedMap(new HashMap<Class<?>, Retrofit>());
    private static volatile RetrofitConf sRetrofitConf;
    private static volatile Retrofit.Builder sBuilder;

    private EZRetrofit() {

    }

    public static void initial(@NonNull RetrofitConf retrofitConf) {
        synchronized (EZRetrofit.class) {
            sRetrofitMap.clear();
            sRetrofitConf = retrofitConf;
            sBuilder = build(retrofitConf);
        }
    }

    private static Retrofit.Builder build(@NonNull RetrofitConf retrofitConf) {
        OkHttpClient client = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (retrofitConf.getTimeout() > 0L) {
            builder.connectTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
            builder.readTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
        }

        if (retrofitConf.getAuthenticator() != null) {
            builder.authenticator(retrofitConf.getAuthenticator());
        }

        if (retrofitConf.isUseSSLConnection() && retrofitConf.getCertficatePinner() != null) {
            builder.certificatePinner(retrofitConf.getCertficatePinner());
        }

        if (retrofitConf.getCertPins() != null) {
            builder.connectionPool(new ConnectionPool());
        }

        if (retrofitConf.getCookieHandler() != null) {
            builder.cookieJar(new JavaNetCookieJar(retrofitConf.getCookieHandler()));
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

        builder.followRedirects(retrofitConf.isFollowRedirects());
        builder.followSslRedirects(retrofitConf.followSslRedirects());
        builder.hostnameVerifier();
        builder.pingInterval();
        builder.proxy();
        builder.proxyAuthenticator();
        builder.proxySelector();
        builder.socketFactory();
        builder.sslSocketFactory();


        client = builder.build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(client);
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

    public static void count(Object obj, Call call, Callback callback) {
        checkInitialStatus();
        String tag = "";
        if (obj != null) {
            tag = obj.getClass().getName();
        }

        CallManager.newInstance().enqueue(tag, call, callback);
    }

    public static void call(Call call, Callback callback) {
        count(null, call, callback);
    }

    public static void stop(Object obj) {
        checkInitialStatus();
        CallManager.newInstance().cancel(obj.getClass().getName());
    }

    public static void stopAll() {
        checkInitialStatus();
        CallManager.newInstance().cancelAll();
    }

    public static <T> EZRetrofitHelper<T> create(RetrofitConf retrofitConf) {
        checkInitialStatus();
        EZRetrofitHelper<T> helper = new EZRetrofitHelper<T>(build(retrofitConf), retrofitConf);
        helper.setRetrofitMap(sRetrofitMap);
        return helper;
    }

    public static <T> EZRetrofitHelper<T> create() {
        checkInitialStatus();
        EZRetrofitHelper<T> helper = new EZRetrofitHelper<T>(sRetrofitConf);
        helper.setRetrofitMap(sRetrofitMap);
        return helper;
    }

    public static class EZRetrofitHelper<T> {

        private RetrofitConf _RetrofitConf;
        private Retrofit.Builder _Builder;
        private Map<Class<?>, Retrofit> _RetrofitMap;

        public EZRetrofitHelper(RetrofitConf conf) {
            this(sBuilder, conf);
        }

        public EZRetrofitHelper(Retrofit.Builder builder, RetrofitConf conf) {
            this._RetrofitConf = conf;
            this._Builder = builder;
        }

        public void setRetrofitMap(Map<Class<?>, Retrofit> retrofitMap) {
            this._RetrofitMap = retrofitMap;
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
