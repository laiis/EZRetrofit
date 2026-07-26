package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.CookiePolicy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CookieJarManagerTest {

    private PersistentCookieStore cookieStore;

    @BeforeEach
    void setUp() {
        CookieStoreRepo repo = new CookieStoreRepo() {
            private final Map<Object, Object> map = new HashMap<>();
            @Override public <T> Map<Object, T> getAll() { return (Map) map; }
            @Override public String getString(String key, String defValue) { return (String) map.getOrDefault(key, defValue); }
            @Override public void putString(String key, String value) { map.put(key, value); }
            @Override public void clear() { map.clear(); }
            @Override public void flush() {}
            @Override public boolean contains(String key) { return map.containsKey(key); }
            @Override public boolean remove(String key) { return map.remove(key) != null; }
        };
        cookieStore = new PersistentCookieStore(repo);
    }

    @Test
    void testSaveFromResponse_AcceptAll() {
        CookieJarManager manager = new CookieJarManager(cookieStore, CookiePolicy.ACCEPT_ALL);
        HttpUrl url = HttpUrl.get("https://example.com/");
        Cookie cookie = new Cookie.Builder().domain("example.com").name("session").value("123").build();

        manager.saveFromResponse(url, List.of(cookie));
        List<Cookie> loaded = manager.loadForRequest(url);

        assertEquals(1, loaded.size());
        assertEquals("session", loaded.get(0).name());
        assertSame(cookieStore, manager.getPersistentCookieStore());
    }

    @Test
    void testSaveFromResponse_AcceptNone() {
        CookieJarManager manager = new CookieJarManager(cookieStore, CookiePolicy.ACCEPT_NONE);
        HttpUrl url = HttpUrl.get("https://example.com/");
        Cookie cookie = new Cookie.Builder().domain("example.com").name("session").value("123").build();

        manager.saveFromResponse(url, List.of(cookie));
        List<Cookie> loaded = manager.loadForRequest(url);

        assertTrue(loaded.isEmpty());
    }

    @Test
    void testSaveAndLoad_AcceptOriginalServer() {
        CookieJarManager manager = new CookieJarManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        HttpUrl url = HttpUrl.get("https://example.com/");
        Cookie matchingCookie = new Cookie.Builder().domain("example.com").name("session").value("123").build();
        
        manager.saveFromResponse(url, List.of(matchingCookie));
        List<Cookie> loaded = manager.loadForRequest(url);

        assertFalse(loaded.isEmpty());
    }
}
