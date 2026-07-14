package tw.idv.laiis.ezretrofit.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import okhttp3.Interceptor;

public final class InterceptorConfig {
    private final List<Interceptor> interceptorList;
    private final List<Interceptor> networkInterceptorList;

    private InterceptorConfig(Builder builder) {
        this.interceptorList = Collections.unmodifiableList(new ArrayList<>(builder.interceptorList));
        this.networkInterceptorList = Collections.unmodifiableList(new ArrayList<>(builder.networkInterceptorList));
    }

    public List<Interceptor> getInterceptorList() {
        return interceptorList;
    }

    public List<Interceptor> getNetworkInterceptorList() {
        return networkInterceptorList;
    }

    public static class Builder {
        private final List<Interceptor> interceptorList = new ArrayList<>();
        private final List<Interceptor> networkInterceptorList = new ArrayList<>();

        public Builder addInterceptor(Interceptor interceptor) {
            if (interceptor != null) {
                this.interceptorList.add(interceptor);
            }
            return this;
        }

        public Builder addNetworkInterceptor(Interceptor interceptor) {
            if (interceptor != null) {
                this.networkInterceptorList.add(interceptor);
            }
            return this;
        }

        public InterceptorConfig build() {
            return new InterceptorConfig(this);
        }
    }
}
