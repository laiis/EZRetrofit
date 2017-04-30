package tw.idv.laiis.ezretrofit;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import tw.idv.laiis.ezretrofit.managers.CallManager;

/**
 * Created by laiis on 2017/4/28.
 */

public class EZRetrofit {

    private static volatile EZHelper sEZHelper = new EZHelper();
    private static volatile RetrofitConf sRetrofitConf;

    private EZRetrofit() {

    }

    public static <T> EZRetrofitHelper create(Class<T> clsWebservice) {
        EZRetrofitHelper helper = new EZRetrofitHelper(sRetrofitConf);
        helper.setWebservice(clsWebservice);
        sEZHelper.setEZRetrofitHelper(helper);
        return helper;
    }

    public static void initial(RetrofitConf retrofitConf) {
        synchronized (EZRetrofit.class) {
            sRetrofitConf = retrofitConf;
        }
    }

    public static void count(Object obj, Call call) {
        String tag = "";
        if (obj != null) {
            tag = obj.getClass().getName();
        }
        CallManager.newInstance().enqueue(tag, call, sEZHelper.getEZRetrofitHelper()._Callback);
    }

    public static void call(Call call) {
        count(null, call);
    }

    public static void stop(Object obj) {
        CallManager.newInstance().cancel(obj.getClass().getName());
    }

    public static void stopAll() {
        CallManager.newInstance().cancelAll();
    }

    public static class EZHelper {
        private EZRetrofitHelper _Helper;

        public void setEZRetrofitHelper(EZRetrofitHelper helper) {
            this._Helper = helper;
        }

        public EZRetrofitHelper getEZRetrofitHelper() {
            return _Helper;
        }
    }

    public static class EZRetrofitHelper<T> {

        private Class<T> _ClsWebservice;
        private Callback _Callback;
        private RetrofitConf _RetrofitConf;

        public EZRetrofitHelper(RetrofitConf conf) {
            this._RetrofitConf = conf;
        }

        public EZRetrofitHelper setWebservice(Class<T> clsWebservice) {
            this._ClsWebservice = clsWebservice;
            return this;
        }

        public T callback(Callback callback) {
            this._Callback = callback;

            Retrofit retrofit = null;
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(_RetrofitConf.getTimeout(), TimeUnit.SECONDS)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(_RetrofitConf.getBaseUrl(_ClsWebservice))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build();
            return retrofit.create(_ClsWebservice);
        }
    }
}
