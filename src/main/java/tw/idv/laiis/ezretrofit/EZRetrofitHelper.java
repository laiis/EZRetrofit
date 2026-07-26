package tw.idv.laiis.ezretrofit;

import java.util.Map;
import retrofit2.Retrofit;

/**
 * EZRetrofitHelper 負責依據配置與 Builder 建立並快取 Retrofit 服務實例。
 * 此類別改為非單例（Non-Singleton）模式，每次 newInstance() 都回傳全新實例，
 * 以確保在多執行緒環境下同時配置並建立服務時的執行緒安全（Thread-safety）。
 */
public class EZRetrofitHelper<T> {

    private RetrofitConf _RetrofitConf;
    private Retrofit.Builder _Builder;
    private final Map<Class<?>, Retrofit> _RetrofitMap = new java.util.concurrent.ConcurrentHashMap<>();

    public static <T> EZRetrofitHelper<T> newInstance() {
        return new EZRetrofitHelper<>();
    }

    EZRetrofitHelper() {
    }

    public EZRetrofitHelper<T> setRetrofitConf(RetrofitConf conf) {
        this._RetrofitConf = conf;
        return this;
    }

    public EZRetrofitHelper<T> setRetrofitBuilder(Retrofit.Builder builder) {
        this._Builder = builder;
        return this;
    }

    @Deprecated
    public EZRetrofitHelper<T> setRetrofitMap(Map<Class<?>, Retrofit> retrofitMap) {
        if (retrofitMap != null) {
            this._RetrofitMap.putAll(retrofitMap);
        }
        return this;
    }

    public T webservice(Class<T> clsWebservice) {
        return _RetrofitMap.computeIfAbsent(clsWebservice, k -> {
            Retrofit retrofit = _Builder.baseUrl(_RetrofitConf.getBaseUrl(k)).build();
            return retrofit;
        }).create(clsWebservice);
    }
}
