package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.idv.laiis.ezretrofit.EZLogger;
import tw.idv.laiis.ezretrofit.EZRetrofit;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class PersistentCookieStoreTest {

    private PersistentCookieStore store;
    private MockCookieStoreRepo repo;

    @BeforeEach
    public void setUp() {
        repo = new MockCookieStoreRepo();
        store = new PersistentCookieStore(repo);
    }

    @Test
    public void testRemoveExpiredCookie() {
        HttpUrl url = HttpUrl.parse("https://example.com");
        
        Cookie activeCookie = new Cookie.Builder()
                .name("session")
                .value("12345")
                .domain("example.com")
                .expiresAt(System.currentTimeMillis() + 100000)
                .build();
        store.add(url, Collections.singletonList(activeCookie));
        
        assertEquals(1, store.get(url).size());

        Cookie expiredCookie = new Cookie.Builder()
                .name("session")
                .value("12345")
                .domain("example.com")
                .expiresAt(System.currentTimeMillis() - 100000)
                .build();
        store.add(url, Collections.singletonList(expiredCookie));

        assertEquals(0, store.get(url).size());
    }

    @Test
    public void testJoinSerializationWithoutTrailingComma() {
        HttpUrl url = HttpUrl.parse("https://example.com");
        
        Cookie c1 = new Cookie.Builder()
                .name("c1")
                .value("v1")
                .domain("example.com")
                .expiresAt(System.currentTimeMillis() + 100000)
                .build();
        Cookie c2 = new Cookie.Builder()
                .name("c2")
                .value("v2")
                .domain("example.com")
                .expiresAt(System.currentTimeMillis() + 100000)
                .build();
                
        store.add(url, Arrays.asList(c1, c2));

        String joinedNames = repo.getString("example.com", null);
        assertNotNull(joinedNames);
        assertFalse(joinedNames.endsWith(","), "Joined string should not end with comma: " + joinedNames);
    }

    @Test
    public void testCorruptCookieDecodingLogsWarning() {
        final AtomicBoolean warningLogged = new AtomicBoolean(false);
        EZRetrofit.setLogger(new EZLogger() {
            @Override
            public void warn(String tag, String message, Throwable t) {
                warningLogged.set(true);
            }
        });

        repo.putString("example.com", "broken_cookie");
        repo.putString("cookie_broken_cookie", "DEADBEEFINVALIDHEX");

        new PersistentCookieStore(repo);

        assertTrue(warningLogged.get(), "Should log warning when decoding corrupt cookie");
    }

    private static class MockCookieStoreRepo implements CookieStoreRepo {
        private final Map<String, String> map = new HashMap<>();

        @Override
        public <T> Map<Object, T> getAll() {
            return (Map<Object, T>) new HashMap(map);
        }

        @Override
        public String getString(String key, String defValue) {
            return map.containsKey(key) ? map.get(key) : defValue;
        }

        @Override
        public void putString(String key, String value) {
            map.put(key, value);
        }

        @Override
        public void clear() {
            map.clear();
        }

        @Override
        public void flush() {
        }

        @Override
        public boolean contains(String key) {
            return map.containsKey(key);
        }

        @Override
        public boolean remove(String key) {
            return map.remove(key) != null;
        }
    }
}
