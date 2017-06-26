package tw.idv.laiis.ezretrofit.cookies;

import java.util.Map;

/**
 * Created by laiis on 2017/5/13.
 */
public interface CookieStoreRepo {

    <T> Map<Object, T> getAll();

    String getString(String key, String defValue);

    void putString(String key, String value);

    void clear();

    void flush();

    boolean contains(String key);

    boolean remove(String key);
}
