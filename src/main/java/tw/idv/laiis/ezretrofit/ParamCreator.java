package tw.idv.laiis.ezretrofit;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ParamCreator {

    public static boolean isSensitiveHeader(String name) {
        if (name == null) return false;
        String lower = name.toLowerCase();
        return lower.equals("authorization") 
            || lower.equals("cookie") 
            || lower.equals("set-cookie") 
            || lower.equals("proxy-authorization");
    }

    public static void validateHeader(String name, String value) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Header name cannot be null or empty");
        }
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException(String.format("Unexpected char 0x%02x in header name: %s", (int) c, name));
            }
        }
        if (value == null) {
            throw new IllegalArgumentException("Header value cannot be null for name: " + name);
        }
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                if (isSensitiveHeader(name)) {
                    throw new IllegalArgumentException(String.format("Unexpected char 0x%02x in sensitive header %s (value redacted)", (int) c, name));
                } else {
                    throw new IllegalArgumentException(String.format("Unexpected char 0x%02x in header %s value: %s", (int) c, name, value));
                }
            }
        }
    }

    public static class Builder<E> {

        private Map<String, E> mParams = Collections.synchronizedMap(new HashMap<String, E>());

        public Builder<E> putValue(String key, E e) {
            if (key != null && e != null) {
                validateHeader(key, String.valueOf(e));
            }
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
                    String valueStr = String.valueOf(entry.getValue());
                    if (isSensitiveHeader(entry.getKey())) {
                        valueStr = "[REDACTED]";
                    }
                    System.out.println(String.format(" ---> [key, value]:[%s, %s]", entry.getKey(), valueStr));
                }
            }
        }
    }
}
