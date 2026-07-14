package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import retrofit2.*;
import retrofit2.Call;
import tw.idv.laiis.ezretrofit.client.EZRetrofitClient;
import tw.idv.laiis.ezretrofit.client.EZRetrofitLifecycle;
import tw.idv.laiis.ezretrofit.config.EZRetrofitConfig;
import java.util.*;

/**
 * Created by laiis on 2017/4/28.
 * @deprecated Use {@link EZRetrofitConfig}, {@link EZRetrofitClient}, and {@link EZRetrofitLifecycle} instead.
 */
@Deprecated
public class EZRetrofit<T> {

    private static final Map<Class<?>, Retrofit> sRetrofitMap = new java.util.concurrent.ConcurrentHashMap<>();

    private EZRetrofit() {}

    public static Map<Class<?>, Retrofit> getRetrofitMap() {
        return sRetrofitMap;
    }

    public static void setLogger(EZLogger logger) {
        EZRetrofitConfig.setLogger(logger);
    }

    public static EZLogger getLogger() {
        return EZRetrofitConfig.getLogger();
    }

    public static void initial(RetrofitConf retrofitConf) {
        EZRetrofitConfig.initial(retrofitConf);
    }

    public static boolean isInitial() {
        return EZRetrofitConfig.isInitial();
    }

    public static void checkInitialStatus() {
        EZRetrofitConfig.checkInitialStatus();
    }

    public static void call(Call call, EZCallback ezCallback) {
        EZRetrofitLifecycle.call(call, ezCallback);
    }

    public static int count() {
        return EZRetrofitLifecycle.count();
    }

    public static int count(String tag) {
        return EZRetrofitLifecycle.count(tag);
    }

    public static void stop(String tag) {
        EZRetrofitLifecycle.stop(tag);
    }

    public static void stopAll() {
        EZRetrofitLifecycle.stopAll();
    }

    @SuppressWarnings("unchecked")
    public static <T> EZRetrofitHelper<T> create(RetrofitConf retrofitConf) {
        return new EZRetrofitClient().createHelper(retrofitConf);
    }

    @SuppressWarnings("unchecked")
    public static <T> EZRetrofitHelper<T> create() {
        return new EZRetrofitClient().createHelper();
    }

    @SuppressWarnings("unchecked")
    public static <T> T create(Class<T> cls) {
        return new EZRetrofitClient().create(cls);
    }
}
