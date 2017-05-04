package tw.idv.laiis.ezretrofitdemo;

import android.app.Application;
import android.content.Context;

import java.io.IOException;

import okhttp3.Authenticator;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
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
        EZRetrofitCookieManager.newInstance().initial(sEZRetrofitApp, sEZRetrofitApp.getSharedPreferences("test", Context.MODE_PRIVATE));
        EZRetrofit.initial(new RetrofitConf.Builder(getEZRetrofitApp())
                .setCookieHandler(EZRetrofitCookieManager.newInstance().getCookieManager())
                .setFollowRedirects()
                .timeout(15L)
                .setAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        return null;
                    }
                })
                .setRetryOnConnectionFailure(true)
                .baseUrls(JsonWebservice.class, "http://data.taipei/")
                .build());
    }
}
