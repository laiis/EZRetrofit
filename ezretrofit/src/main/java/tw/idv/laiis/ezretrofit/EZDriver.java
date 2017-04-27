package tw.idv.laiis.ezretrofit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by laiis on 2017/4/27.
 */

public class EZDriver implements Driver {

    private static volatile Map<Class<?>, String> sWebserviceMap = Collections.synchronizedMap(new HashMap<Class<?>, String>());
    private RetrofitConf mRetrofitConf;

    @Override
    public RetrofitConf getRetrofitConf() {
        return mRetrofitConf;
    }

    @Override
    public void setRetrofitConf(RetrofitConf retrofitConf) {
        this.mRetrofitConf = retrofitConf;
    }
}
