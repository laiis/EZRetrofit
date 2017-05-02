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

    @NonNull
    public static void initial(RetrofitConf retrofitConf) {
        synchronized (EZRetrofit.class) {
            sRetrofitMap.clear();
            sRetrofitConf = retrofitConf;

            OkHttpClient client = null;
            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            if (sRetrofitConf.getTimeout() > 0L) {
                builder.connectTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
                builder.readTimeout(retrofitConf.getTimeout(), TimeUnit.SECONDS);
            }

            if (sRetrofitConf.getAuthenticator() != null) {
                builder.authenticator(sRetrofitConf.getAuthenticator());
            }

            if (sRetrofitConf.isUseSSLConnection() && sRetrofitConf.getCertficatePinner() != null) {
                builder.certificatePinner(sRetrofitConf.getCertficatePinner());
            }

            if (sRetrofitConf.getCertPins() != null) {
                builder.connectionPool(new ConnectionPool());
            }

            if (sRetrofitConf.getCookieHandler() != null) {
                builder.cookieJar(new JavaNetCookieJar(sRetrofitConf.getCookieHandler()));
            }

            if (sRetrofitConf.getProtocols() != null && sRetrofitConf.getProtocols().size() > 0) {
                builder.protocols(sRetrofitConf.getProtocols());
            }

            if (sRetrofitConf.getInterceptorList() != null && sRetrofitConf.getInterceptorList().size() > 0) {
                for (Interceptor interceptor : sRetrofitConf.getInterceptorList()) {
                    builder.addInterceptor(interceptor);
                }
            }

            if (sRetrofitConf.getConnectionPool() != null) {
                builder.connectionPool(sRetrofitConf.getConnectionPool());
            }

            if (sRetrofitConf.getNetworkInterceptorList() != null) {
                for (Interceptor interceptor : sRetrofitConf.getNetworkInterceptorList()) {
                    builder.addNetworkInterceptor(interceptor);
                }
            }

            client = builder.build();

            sBuilder = new Retrofit.Builder()
                    .client(client);
        }
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
        EZRetrofitHelper<T> helper = new EZRetrofitHelper<T>(retrofitConf);
        return helper;
    }

    public static <T> EZRetrofitHelper<T> create() {
        return create(sRetrofitConf);
    }

    public static class EZRetrofitHelper<T> {

        private RetrofitConf _RetrofitConf;

        public EZRetrofitHelper(RetrofitConf conf) {
            this._RetrofitConf = conf;
        }

        public T webservice(Class<T> clsWebservice) {
            if (sRetrofitMap.get(clsWebservice) == null) {
                Retrofit retrofit = sBuilder.baseUrl(_RetrofitConf.getBaseUrl(clsWebservice))
                        .build();

                sRetrofitMap.put(clsWebservice, retrofit);
            }

            Retrofit retrofit = sRetrofitMap.get(clsWebservice);

            return retrofit.create(clsWebservice);
        }

    }
}
