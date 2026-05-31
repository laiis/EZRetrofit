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
    private Map<Class<?>, Retrofit> _RetrofitMap;

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

    public EZRetrofitHelper<T> setRetrofitMap(Map<Class<?>, Retrofit> retrofitMap) {
        this._RetrofitMap = retrofitMap;
        return this;
    }

    public T webservice(Class<T> clsWebservice) {
        if (_RetrofitMap.get(clsWebservice) == null) {
            synchronized (_RetrofitMap) {
                if (_RetrofitMap.get(clsWebservice) == null) {
                    Retrofit retrofit = _Builder.baseUrl(_RetrofitConf.getBaseUrl(clsWebservice))
                            .build();
                    _RetrofitMap.put(clsWebservice, retrofit);
                }
            }
        }

        Retrofit retrofit = _RetrofitMap.get(clsWebservice);
        return retrofit.create(clsWebservice);
    }
}
