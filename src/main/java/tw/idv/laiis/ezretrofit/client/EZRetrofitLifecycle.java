package tw.idv.laiis.ezretrofit.client;

import retrofit2.Call;
import tw.idv.laiis.ezretrofit.CallManager;
import tw.idv.laiis.ezretrofit.EZCallback;
import tw.idv.laiis.ezretrofit.config.EZRetrofitConfig;

public final class EZRetrofitLifecycle {

    private EZRetrofitLifecycle() {}

    public static void call(Call call, EZCallback ezCallback) {
        EZRetrofitConfig.checkInitialStatus();
        CallManager.newInstance().enqueue(call, ezCallback);
    }

    public static int count() {
        EZRetrofitConfig.checkInitialStatus();
        return CallManager.newInstance().requestAmount();
    }

    public static int count(String tag) {
        EZRetrofitConfig.checkInitialStatus();
        return CallManager.newInstance().requestAmount(tag);
    }

    public static void stop(String tag) {
        EZRetrofitConfig.checkInitialStatus();
        CallManager.newInstance().cancel(tag);
    }

    public static void stopAll() {
        EZRetrofitConfig.checkInitialStatus();
        CallManager.newInstance().cancelAll();
    }
}
