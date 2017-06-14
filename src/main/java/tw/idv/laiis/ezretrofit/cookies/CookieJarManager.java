package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laiis on 2017/6/14.
 */
public class CookieJarManager extends CookieManager implements CookieJar {

    private static volatile CookiePolicy cookiePolicy;

    public CookieJarManager(PersistentCookieStore store, CookiePolicy cookiePolicy) {
        super(store, cookiePolicy);
        this.cookiePolicy = cookiePolicy;
    }

    @Override
    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        if (cookiePolicy == null || CookiePolicy.ACCEPT_NONE == cookiePolicy) {
            return;
        }

        CookieStore cookieStore = getCookieStore();
        for (Cookie cookie : cookies) {
            HttpCookie httpCookie = new HttpCookie(cookie.name(), cookie.value());
            httpCookie.setDomain(cookie.domain());
            httpCookie.setMaxAge(cookie.expiresAt());
            httpCookie.setSecure(cookie.secure());
            httpCookie.setPath(cookie.path());
            httpCookie.setHttpOnly(cookie.httpOnly());

            if (cookiePolicy.shouldAccept(url.uri(), httpCookie)) {
                cookieStore.add(url.uri(), httpCookie);
            }
        }
    }

    @Override
    public List<Cookie> loadForRequest(HttpUrl url) {
        if (cookiePolicy == null || CookiePolicy.ACCEPT_NONE == cookiePolicy) {
            return new ArrayList<>();
        }

        CookieStore cookieStore = getCookieStore();
        List<HttpCookie> httpCookies = cookieStore.get(url.uri());
        List<Cookie> cookies = new ArrayList<>();
        for (HttpCookie httpCookie : httpCookies) {
            Cookie cookie = new Cookie.Builder()
                    .domain(httpCookie.getDomain())
                    .expiresAt(httpCookie.getMaxAge())
                    .name(httpCookie.getName())
                    .value(httpCookie.getValue())
                    .path(httpCookie.getPath())
                    .build();

            if (httpCookie.isHttpOnly()) {
                cookie.httpOnly();
            }

            if (httpCookie.getSecure()) {
                cookie.secure();
            }

            if (cookiePolicy.shouldAccept(url.uri(), httpCookie)) {
                cookies.add(cookie);
            }
        }

        return cookies;
    }
}
