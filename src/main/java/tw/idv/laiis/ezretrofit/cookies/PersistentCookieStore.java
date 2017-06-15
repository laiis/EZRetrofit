package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

import java.io.*;
import java.util.*;

/**
 * author: <a href="http://www.jiechic.com" target="_blank">jiechic</a> on 15/5/27.<br/>
 * ref: <a href="http://www.jiechic.com/archives/cookie-automatic-management-optimization-of-okhttp-framework" target="_blank">jiechic's blog</a><br/>
 * A persistent cookie store which implements the Apache HttpClient CookieStore interface.
 * Cookies are stored and will persist on the user's device between application sessions since they
 * are serialized and stored in SharedPreferences. Instances of this class are
 * designed to be used with AsyncHttpClient#setCookieStore, but can also be used with a
 * regular old apache HttpClient/HttpContext if you prefer.
 */
public class PersistentCookieStore {

    private static final String TAG = "TAG_COOKIESTORE";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final HashMap<String, Map<String, Cookie>> mCookies;
    private final CookieStoreRepo mCookieStoreRepo;

    /**
     * Construct a persistent cookie store. * * @param context Context to attach cookie store to
     */
    public PersistentCookieStore(CookieStoreRepo cookieStoreRepo) {
        mCookieStoreRepo = cookieStoreRepo;
        mCookies = new HashMap<>();//Load any previously stored cookies into the store

        Map<String, ?> prefsMap = mCookieStoreRepo.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            if (null != entry.getValue() && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = ((String) entry.getValue()).split(",");
                for (String name : cookieNames) {
                    String encodedCookie = mCookieStoreRepo.getString(COOKIE_NAME_PREFIX + name, null);
                    if (encodedCookie != null) {
                        Cookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if (!mCookies.containsKey(entry.getKey()))
                                mCookies.put(entry.getKey(), Collections.synchronizedMap(new HashMap<String, Cookie>()));
                            mCookies.get(entry.getKey()).put(name, decodedCookie);
                        }
                    }
                }
            }
        }
    }

    protected String getCookieToken(Cookie cookie) {
        return cookie.name() + cookie.domain();
    }

    public void add(HttpUrl httpUrl, List<Cookie> cookies) {
        if (null != cookies && cookies.size() > 0) {
            for (Cookie item : cookies) {
                add(item);
            }
        }
    }

    private void add(Cookie cookie) {
//        String name = getCookieToken(cookie);

        //Save cookie into local store, or remove if expired
        if (!mCookies.containsKey(cookie.domain()))
            mCookies.put(cookie.domain(), Collections.synchronizedMap(new HashMap<String, Cookie>()));

        if (cookie.expiresAt() > System.currentTimeMillis()) {
            mCookies.get(cookie.domain()).put(cookie.name(), cookie);
        } else {
            if (mCookies.containsKey(cookie.domain()))
                mCookies.get(cookie.domain()).remove(cookie.domain());
        }
        //Save cookie into persistent store
        mCookieStoreRepo.putString(cookie.domain(), join(",", mCookies.get(cookie.domain()).keySet()));
        mCookieStoreRepo.putString(COOKIE_NAME_PREFIX + cookie.name(), encodeCookie(new SerializableHttpCookie(cookie)));
        mCookieStoreRepo.flush();
    }


    public List<Cookie> get(HttpUrl uri) {
        ArrayList<Cookie> ret = new ArrayList<>();
        for (String key : mCookies.keySet()) {
            if (uri.host().contains(key)) {
                ret.addAll(mCookies.get(key).values());
            }
        }
        return ret;
    }

    /**
     * Serializes Cookie object into String * * @param cookie cookie to be encoded, can be null * @return cookie encoded as String
     */
    protected String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null) return null;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            ObjectOutputStream outputStream = new ObjectOutputStream(os);
            outputStream.writeObject(cookie);
        } catch (IOException e) {
            return null;
        }
        return byteArrayToHexString(os.toByteArray());
    }

    /**
     * Returns cookie decoded from cookie string * * @param cookieString string of cookie as returned from http request * @return decoded cookie or null if exception occured
     */
    protected Cookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        Cookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject()).getCookie();
        } catch (Exception e) {
        }
        return cookie;
    }

    /**
     * Using some super basic byte array <-> hex conversions so we don't have to rely on any * large Base64 libraries. Can be overridden if you like! * * @param bytes byte array to be converted * @return string containing hex values
     */
    protected String byteArrayToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte element : bytes) {
            int v = element & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase(Locale.US);
    }

    /**
     * Converts hex values from strings to byte arra * * @param hexString string of hex-encoded values * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }

    private String join(String seperator, Set<String> keySet) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            boolean hasNext = iterator.hasNext();
            sb.append(iterator.next());
            if (hasNext) {
                sb.append(seperator);
            }
        }

        return sb.toString();
    }
}
