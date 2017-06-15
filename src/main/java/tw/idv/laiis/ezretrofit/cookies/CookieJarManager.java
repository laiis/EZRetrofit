package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laiis on 2017/6/14.
 */
public class CookieJarManager extends CookieManager implements CookieJar {

    private static volatile CookiePolicy cookiePolicy;
    private PersistentCookieStore mCookieStore;

    public CookieJarManager(PersistentCookieStore store, CookiePolicy cookiePolicy) {
        this.cookiePolicy = cookiePolicy;
        this.mCookieStore = store;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookiePolicy == null || CookiePolicy.ACCEPT_NONE == cookiePolicy) {
            return;
        }

        if (CookiePolicy.ACCEPT_ALL == cookiePolicy) {
            mCookieStore.add(url, cookies);
        } else {
            for (Cookie cookie : cookies) {
                if (cookie.matches(url)) {
                    mCookieStore.add(url, cookies);
                }
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (cookiePolicy == null || CookiePolicy.ACCEPT_NONE == cookiePolicy) {
            return new ArrayList<>();
        }

        if (CookiePolicy.ACCEPT_ALL == cookiePolicy) {
            return mCookieStore.get(url);
        } else {
            List<Cookie> cookies = new ArrayList<>();
            for (Cookie cookie : mCookieStore.get(url)) {
                if (cookie.matches(url)) {
                    cookies.add(cookie);
                }
            }

            return cookies;
        }
    }
}
