package tw.idv.laiis.ezretrofitdemo;

import android.app.Application;

import java.net.CookieManager;

import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.RetrofitConf;

/**
 * Created by laiis on 2017/4/30.
 */

public class EZRetrofitApp extends Application {

    private static volatile EZRetrofitApp sEZRetrofitApp;

    private static EZRetrofitApp getEZRetrofitApp() {
        return sEZRetrofitApp;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sEZRetrofitApp = this;
        initialEZRetrofit();
    }

    public static void initialEZRetrofit() {
        // I want to use this pattern to call api.
        EZRetrofit.initial(new RetrofitConf.Builder(getEZRetrofitApp())
                .setCookieHandler(CookieManager.getDefault())
                .timeout(15L)
                .baseUrls(JsonWebservice.class, "http://data.taipei/")
                .build());
    }
}
