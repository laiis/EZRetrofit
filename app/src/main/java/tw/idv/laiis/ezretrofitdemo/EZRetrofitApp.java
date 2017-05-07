package tw.idv.laiis.ezretrofitdemo;

import android.app.Application;
import android.content.Context;

import java.net.CookiePolicy;

import retrofit2.converter.gson.GsonConverterFactory;
import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import tw.idv.laiis.ezretrofit.managers.EZRetrofitCookieManager;

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
        EZRetrofitCookieManager.newInstance().initial(sEZRetrofitApp, sEZRetrofitApp.getSharedPreferences("test", Context.MODE_PRIVATE), CookiePolicy.ACCEPT_ALL);
        EZRetrofit.initial(new RetrofitConf.Builder(getEZRetrofitApp())
                .setCookieHandler(EZRetrofitCookieManager.newInstance().getCookieManager())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrls(JsonWebservice.class, "http://data.taipei/")
                .build());
    }
}
