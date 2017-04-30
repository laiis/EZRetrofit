package tw.idv.laiis.ezretrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import tw.idv.laiis.ezretrofit.managers.CallManager;

/**
 * Created by laiis on 2017/4/28.
 */

public class EZRetrofit {

    private static volatile EZHelper sEZHelper;
    private static volatile RetrofitConf sRetrofitConf;

    private EZRetrofit() {
        sEZHelper = new EZHelper();
    }

    public static <T> EZRetrofitHelper create(Class<T> clsWebservice) {
        EZRetrofitHelper helper = new EZRetrofitHelper();
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
        CallManager.newInstance().enqueue(obj.getClass().getName(), call, sEZHelper.getEZRetrofitHelper()._Callback);
    }

    public static void call(Call call) {
        call.enqueue(sEZHelper.getEZRetrofitHelper()._Callback);
    }

    public static void stop(Object obj){
        CallManager.newInstance().cancel(obj.getClass().getName());
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
        private T mT;
        private Callback _Callback;

        public EZRetrofitHelper() {

        }

        public EZRetrofitHelper setWebservice(Class<T> clsWebservice) {
            this._ClsWebservice = clsWebservice;
            return this;
        }

        private EZRetrofitHelper setConcreteWebservice(T t) {
            this.mT = t;
            return this;
        }

        public EZRetrofitHelper endPoint(String endPoint) {
            return this;
        }

        public T callback(Callback callback) {
            Retrofit retrofit = null;
            return (retrofit.create(_ClsWebservice));
        }
    }
}
