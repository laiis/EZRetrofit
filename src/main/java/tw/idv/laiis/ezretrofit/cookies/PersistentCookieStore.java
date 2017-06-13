package tw.idv.laiis.ezretrofit.cookies;

import java.io.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
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
public class PersistentCookieStore implements CookieStore {

    private static final String TAG = "TAG_COOKIESTORE";
    private static final String COOKIE_NAME_PREFIX = "cookie_";

    private final HashMap<String, HashMap<String, HttpCookie>> cookies;
    private final CookieStoreRepo mCookieStoreRepo;

    /**
     * Construct a persistent cookie store.
     */
    public PersistentCookieStore(CookieStoreRepo cookieStoreRepo) {
        mCookieStoreRepo = cookieStoreRepo;
        cookies = new HashMap<>();

        // Load any previously stored cookies into the store
        Map<String, ?> prefsMap = mCookieStoreRepo.getAll();
        for (Map.Entry<String, ?> entry : prefsMap.entrySet()) {
            if (entry.getValue() instanceof String && ((String) entry.getValue()) != null && !((String) entry.getValue()).startsWith(COOKIE_NAME_PREFIX)) {
                String[] cookieNames = ((String) entry.getValue()).split(",");
                for (String name : cookieNames) {
                    String encodedCookie = mCookieStoreRepo.getString(COOKIE_NAME_PREFIX + name, null);
                    if (encodedCookie != null) {
                        HttpCookie decodedCookie = decodeCookie(encodedCookie);
                        if (decodedCookie != null) {
                            if (!cookies.containsKey(entry.getKey())) {
                                cookies.put(entry.getKey(), new HashMap<String, HttpCookie>());
                            }
                            cookies.get(entry.getKey()).put(name, decodedCookie);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {

        // Save cookie into local store, or remove if expired
        if (!cookie.hasExpired()) {
            if (!cookies.containsKey(cookie.getDomain())) {
                cookies.put(cookie.getDomain(), new HashMap<String, HttpCookie>());
            }
            cookies.get(cookie.getDomain()).put(cookie.getName(), cookie);
        } else {
            if (cookies.containsKey(cookie.getDomain())) {
                cookies.get(cookie.getDomain()).remove(cookie.getDomain());
            }
        }

        // Save cookie into persistent store
        mCookieStoreRepo.putString(cookie.getDomain(), join(",", cookies.get(cookie.getDomain()).keySet()));
        mCookieStoreRepo.putString(COOKIE_NAME_PREFIX + cookie.getName(), encodeCookie(new SerializableHttpCookie(cookie)));
        mCookieStoreRepo.flush();
    }

    private String join(String seperator, Set<String> keySet) {
        StringBuffer sb = new StringBuffer();
        for (Iterator<String> iterator = keySet.iterator(); iterator.hasNext(); ) {
            boolean hasNext = iterator.hasNext();
            sb.append(iterator.next());
            if (hasNext) {
                sb.append(",");
            }
        }

        return sb.toString();
    }

    protected String getCookieToken(URI uri, HttpCookie cookie) {
        return cookie.getName() + cookie.getDomain();
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : cookies.keySet()) {
            if (uri.getHost().contains(key)) {
                ret.addAll(cookies.get(key).values());
            }
        }
        return ret;
    }

    @Override
    public boolean removeAll() {
        mCookieStoreRepo.clear();
        mCookieStoreRepo.flush();
        cookies.clear();
        return true;
    }


    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        String name = getCookieToken(uri, cookie);

        if (cookies.containsKey(uri.getHost()) && cookies.get(uri.getHost()).containsKey(name)) {
            cookies.get(uri.getHost()).remove(name);

            if (mCookieStoreRepo.contains(COOKIE_NAME_PREFIX + name)) {
                mCookieStoreRepo.remove(COOKIE_NAME_PREFIX + name);
            }
            mCookieStoreRepo.putString(uri.getHost(), join(",", cookies.get(uri.getHost()).keySet()));
            mCookieStoreRepo.flush();

            return true;
        }

        return false;
    }

    @Override
    public List<HttpCookie> getCookies() {
        ArrayList<HttpCookie> ret = new ArrayList<HttpCookie>();
        for (String key : cookies.keySet()) {
            ret.addAll(cookies.get(key).values());
        }

        return ret;
    }

    @Override
    public List<URI> getURIs() {
        ArrayList<URI> ret = new ArrayList<URI>();
        for (String key : cookies.keySet()) {
            try {
                ret.add(new URI(key));
            } catch (URISyntaxException e) {

            }
        }

        return ret;
    }

    /**
     * Serializes Cookie object into String
     *
     * @param cookie cookie to be encoded, can be null
     * @return cookie encoded as String
     */
    protected String encodeCookie(SerializableHttpCookie cookie) {
        if (cookie == null) {
            return null;
        }
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
     * Returns cookie decoded from cookie string
     *
     * @param cookieString string of cookie as returned from http request
     * @return decoded cookie or null if exception occured
     */
    protected HttpCookie decodeCookie(String cookieString) {
        byte[] bytes = hexStringToByteArray(cookieString);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        HttpCookie cookie = null;
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            cookie = ((SerializableHttpCookie) objectInputStream.readObject()).getCookie();
        } catch (IOException e) {

        } catch (ClassNotFoundException e) {

        }

        return cookie;
    }

    /**
     * Using some super basic byte array <-> hex conversions so we don't have to rely on any
     * large Base64 libraries. Can be overridden if you like!
     *
     * @param bytes byte array to be converted
     * @return string containing hex values
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
     * Converts hex values from strings to byte arra
     *
     * @param hexString string of hex-encoded values
     * @return decoded byte array
     */
    protected byte[] hexStringToByteArray(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
