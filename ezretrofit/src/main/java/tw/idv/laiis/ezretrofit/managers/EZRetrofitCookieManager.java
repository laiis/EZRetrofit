package tw.idv.laiis.ezretrofit.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;

import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;

import tw.idv.laiis.ezretrofit.cookies.PersistentCookieStore;

/**
 * Created by laiis on 2017/4/25.
 */

public class EZRetrofitCookieManager {

    private static volatile EZRetrofitCookieManager sEZRetrofitCookieManager;

    private CookieManager mCookieManager;
    private android.webkit.CookieManager mWebViewCookieManager;

    public static EZRetrofitCookieManager newInstance() {
        if (sEZRetrofitCookieManager == null) {
            synchronized (EZRetrofitCookieManager.class) {
                if (sEZRetrofitCookieManager == null) {
                    sEZRetrofitCookieManager = new EZRetrofitCookieManager();
                }
            }
        }

        return sEZRetrofitCookieManager;
    }

    private EZRetrofitCookieManager() {

    }

    public void initial(Context context, SharedPreferences sharedPreferences) {
        mCookieManager = new CookieManager(new PersistentCookieStore(sharedPreferences), CookiePolicy.ACCEPT_ALL);
        mWebViewCookieManager = android.webkit.CookieManager.getInstance();

        CookieHandler.setDefault(mCookieManager);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(context);
        }
        mWebViewCookieManager.setAcceptCookie(true);
    }

    public boolean isInitial() {
        return sEZRetrofitCookieManager != null;
    }

    public void checkInitialYet() {
        if (!isInitial()) {
            throw new RuntimeException("EZRetrofitCookieManager isn't initial yet. Plz initial this class before you use it.");
        }
    }

    public CookieManager getCookieManager() {
        return mCookieManager;
    }

    public android.webkit.CookieManager getWebViewCookieManager() {
        return mWebViewCookieManager;
    }

    public void clearAllCookies() {
        mCookieManager.getCookieStore().removeAll();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mWebViewCookieManager.removeAllCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    // do nothing
                }
            });
            mWebViewCookieManager.removeSessionCookies(new ValueCallback<Boolean>() {
                @Override
                public void onReceiveValue(Boolean value) {
                    // do nothing
                }
            });
            mWebViewCookieManager.flush();
        } else {
            mWebViewCookieManager.removeAllCookie();
            mWebViewCookieManager.removeSessionCookie();
            mWebViewCookieManager.removeExpiredCookie();
        }

    }

}
