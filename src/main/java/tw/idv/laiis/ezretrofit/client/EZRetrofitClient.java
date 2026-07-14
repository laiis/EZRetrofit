package tw.idv.laiis.ezretrofit.client;

import okhttp3.*;
import retrofit2.*;
import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.EZRetrofitHelper;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import tw.idv.laiis.ezretrofit.SafeGzipInterceptor;
import tw.idv.laiis.ezretrofit.config.*;
import java.util.concurrent.TimeUnit;

public class EZRetrofitClient {

    private final EZRetrofitConfig config;
    private final Retrofit.Builder retrofitBuilder;

    public EZRetrofitClient() {
        EZRetrofitConfig.checkInitialStatus();
        this.config = EZRetrofitConfig.getInstance();
        this.retrofitBuilder = build(this.config);
    }

    public EZRetrofitClient(EZRetrofitConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("EZRetrofitConfig cannot be null");
        }
        this.config = config;
        this.retrofitBuilder = build(this.config);
    }

    public <T> EZRetrofitHelper<T> createHelper(RetrofitConf retrofitConf) {
        EZRetrofitConfig tempConfig = EZRetrofitConfig.fromRetrofitConf(retrofitConf);
        return EZRetrofitHelper.<T>newInstance()
                .setRetrofitBuilder(build(tempConfig))
                .setRetrofitConf(retrofitConf)
                .setRetrofitMap(EZRetrofit.getRetrofitMap());
    }

    public <T> EZRetrofitHelper<T> createHelper() {
        return EZRetrofitHelper.<T>newInstance()
                .setRetrofitBuilder(this.retrofitBuilder)
                .setRetrofitConf(this.config.toRetrofitConf())
                .setRetrofitMap(EZRetrofit.getRetrofitMap());
    }

    public <T> T create(Class<T> cls) {
        EZRetrofitHelper<T> helper = createHelper();
        return helper.webservice(cls);
    }

    private Retrofit.Builder build(EZRetrofitConfig conf) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();

        builder.followRedirects(conf.isFollowRedirects());
        builder.followSslRedirects(conf.isFollowSslRedirects());

        if (conf.getTimeoutConfig() != null && conf.getTimeoutConfig().getTimeoutSeconds() > 0L) {
            builder.connectTimeout(conf.getTimeoutConfig().getTimeoutSeconds(), TimeUnit.SECONDS);
            builder.readTimeout(conf.getTimeoutConfig().getTimeoutSeconds(), TimeUnit.SECONDS);
        }

        if (conf.getAuthenticator() != null) {
            builder.authenticator(conf.getAuthenticator());
        }

        if (conf.getCookieJar() != null) {
            builder.cookieJar(conf.getCookieJar());
        }

        if (conf.getProtocols() != null && !conf.getProtocols().isEmpty()) {
            builder.protocols(conf.getProtocols());
        }

        builder.addInterceptor(new SafeGzipInterceptor());

        if (conf.getInterceptorConfig() != null && conf.getInterceptorConfig().getInterceptorList() != null) {
            for (Interceptor interceptor : conf.getInterceptorConfig().getInterceptorList()) {
                builder.addInterceptor(interceptor);
            }
        }

        if (conf.getConnectionPool() != null) {
            builder.connectionPool(conf.getConnectionPool());
        }

        if (conf.getInterceptorConfig() != null && conf.getInterceptorConfig().getNetworkInterceptorList() != null) {
            for (Interceptor interceptor : conf.getInterceptorConfig().getNetworkInterceptorList()) {
                builder.addNetworkInterceptor(interceptor);
            }
        }

        if (conf.getCache() != null) {
            builder.cache(conf.getCache());
        }

        if (conf.getDispatcher() != null) {
            builder.dispatcher(conf.getDispatcher());
        }

        if (conf.getDns() != null) {
            builder.dns(conf.getDns());
        }

        if (conf.getHostnameVerifier() != null) {
            builder.hostnameVerifier(conf.getHostnameVerifier());
        }

        if (conf.getPinInterval() != null) {
            RetrofitConf.PinInterval pinInterval = conf.getPinInterval();
            builder.pingInterval(pinInterval.getInterval(), pinInterval.getTimeUnit());
        }

        if (conf.getProxyConfig() != null && conf.getProxyConfig().getProxy() != null) {
            builder.proxy(conf.getProxyConfig().getProxy());
        }

        if (conf.getProxyConfig() != null && conf.getProxyConfig().getProxyAuthenticator() != null) {
            builder.proxyAuthenticator(conf.getProxyConfig().getProxyAuthenticator());
        }

        if (conf.getProxyConfig() != null && conf.getProxyConfig().getProxySelector() != null) {
            builder.proxySelector(conf.getProxyConfig().getProxySelector());
        }

        if (conf.getSslConfig() != null) {
            SslConfig ssl = conf.getSslConfig();
            if (ssl.isUseSSLCertificatePinning()) {
                builder.certificatePinner(ssl.getCertificatePinner());
            } else if (ssl.isUseSSLFactoryManager()) {
                builder.sslSocketFactory(ssl.getSslSocketFactory(), ssl.getX509TrustManager());
            }
        }

        OkHttpClient client = builder.build();

        Retrofit.Builder retrofitBuilder = new Retrofit.Builder()
                .client(client);

        if (conf.getCallAdapterFactoryList() != null) {
            for (CallAdapter.Factory factory : conf.getCallAdapterFactoryList()) {
                retrofitBuilder.addCallAdapterFactory(factory);
            }
        }

        if (conf.getConverterFactoryList() != null) {
            for (Converter.Factory factory : conf.getConverterFactoryList()) {
                retrofitBuilder.addConverterFactory(factory);
            }
        }

        if (conf.getExecutor() != null) {
            retrofitBuilder.callbackExecutor(conf.getExecutor());
        }

        if (conf.getOKHttp3Factory() != null) {
            retrofitBuilder.callFactory(conf.getOKHttp3Factory());
        }

        retrofitBuilder.validateEagerly(conf.isValidateEagerly());

        return retrofitBuilder;
    }
}
