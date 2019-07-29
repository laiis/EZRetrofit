package tw.idv.laiis.ezretrofit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ParamCreator {

    private static class Builder<E> {

        private Map<String, E> mParams = Collections.synchronizedMap(new HashMap<String, E>());

        public Builder<E> putValue(String key, E e) {
            mParams.put(key, e);
            return this;
        }

        public Map<String, E> build() {
            showParams();
            return mParams;
        }

        public void showParams() {
            if (LibConfig.IS_DEBUG) {
                for (Map.Entry<String, E> entry : mParams.entrySet()) {
                    System.out.println(String.format(" ---> [key, value]:[%s, %s]", entry.getKey(), entry.getValue()));
                }
            }
        }
    }
}
