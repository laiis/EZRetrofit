package tw.idv.laiis.ezretrofit.config;

import okhttp3.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.converter.gson.GsonConverterFactory;
import tw.idv.laiis.ezretrofit.EZLogger;
import tw.idv.laiis.ezretrofit.RetrofitConf;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.File;
import java.util.List;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitConfigTest {

    interface TestService {}

    @BeforeEach
    public void setUp() {
        EZRetrofitConfig.reset();
    }

    @Test
    public void testBuilderAndGetters() {
        SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SslConfig ssl = new SslConfig.Builder()
                .useSSLCertificatePinning(true)
                .useSSLFactoryManager(true)
                .certificatePinner(new CertificatePinner.Builder().build())
                .sslSocketFactory(sslSocketFactory)
                .build();

        ProxyConfig proxy = new ProxyConfig.Builder().build();
        TimeoutConfig timeout = new TimeoutConfig(30L);
        
        Interceptor interceptor = chain -> chain.proceed(chain.request());
        InterceptorConfig interceptors = new InterceptorConfig.Builder()
                .addInterceptor(interceptor)
                .addNetworkInterceptor(interceptor)
                .build();

        Authenticator authenticator = (route, response) -> null;
        ConnectionPool pool = new ConnectionPool();
        Cache cache = new Cache(new File("build/tmp/cache"), 10 * 1024 * 1024);
        Dispatcher dispatcher = new Dispatcher();
        HostnameVerifier verifier = (hostname, session) -> true;
        Executor executor = Runnable::run;
        Call.Factory callFactory = request -> null;

        EZRetrofitConfig config = new EZRetrofitConfig.Builder()
                .sslConfig(ssl)
                .proxyConfig(proxy)
                .timeout(timeout)
                .interceptorConfig(interceptors)
                .cookieJar(CookieJar.NO_COOKIES)
                .dns(Dns.SYSTEM)
                .authenticator(authenticator)
                .connectionPool(pool)
                .retryOnConnectionFailure(true)
                .cache(cache)
                .dispatcher(dispatcher)
                .followRedirects(true)
                .followSslRedirects(true)
                .hostnameVerifier(verifier)
                .addConverterFactory(GsonConverterFactory.create())
                .validateEagerly(true)
                .executor(executor)
                .okHttp3Factory(callFactory)
                .connectionSpecList(List.of(ConnectionSpec.MODERN_TLS))
                .protocols(List.of(Protocol.HTTP_1_1))
                .addBaseUrlService(TestService.class, "https://api.test.com/")
                .build();

        assertTrue(config.getSslConfig().isUseSSLCertificatePinning());
        assertTrue(config.getSslConfig().isUseSSLFactoryManager());
        assertEquals(30L, config.getTimeoutConfig().getTimeoutSeconds());
        assertEquals(CookieJar.NO_COOKIES, config.getCookieJar());
        assertEquals(Dns.SYSTEM, config.getDns());
        assertEquals(authenticator, config.getAuthenticator());
        assertEquals(pool, config.getConnectionPool());
        assertEquals(cache, config.getCache());
        assertEquals(dispatcher, config.getDispatcher());
        assertTrue(config.isFollowRedirects());
        assertTrue(config.isFollowSslRedirects());
        assertEquals(verifier, config.getHostnameVerifier());
        assertTrue(config.isValidateEagerly());
        assertEquals(executor, config.getExecutor());
        assertEquals(callFactory, config.getOKHttp3Factory());
        assertEquals(1, config.getConnectionSpecList().size());
        assertEquals(1, config.getProtocols().size());
        assertEquals("https://api.test.com/", config.getBaseUrl(TestService.class));
        assertEquals(1, config.getInterceptorConfig().getInterceptorList().size());
        assertEquals(1, config.getInterceptorConfig().getNetworkInterceptorList().size());
    }

    @Test
    public void testFromRetrofitConf() {
        RetrofitConf conf = new RetrofitConf.Builder()
                .timeout(45L)
                .baseUrls(TestService.class, "https://from-legacy.com/")
                .build();

        EZRetrofitConfig config = EZRetrofitConfig.fromRetrofitConf(conf);
        assertEquals(45L, config.getTimeoutConfig().getTimeoutSeconds());
        assertEquals("https://from-legacy.com/", config.getBaseUrl(TestService.class));
    }

    @Test
    public void testToRetrofitConf() {
        EZRetrofitConfig config = new EZRetrofitConfig.Builder()
                .timeout(new TimeoutConfig(60L))
                .addBaseUrlService(TestService.class, "https://to-legacy.com/")
                .build();

        RetrofitConf conf = config.toRetrofitConf();
        assertEquals(60L, conf.getTimeout());
        assertEquals("https://to-legacy.com/", conf.getBaseUrl(TestService.class));
    }

    @Test
    public void testInitialAndStatus() {
        EZRetrofitConfig config = new EZRetrofitConfig.Builder()
                .timeout(new TimeoutConfig(10L))
                .build();

        assertFalse(EZRetrofitConfig.isInitial());
        assertThrows(IllegalStateException.class, EZRetrofitConfig::checkInitialStatus);

        EZRetrofitConfig.initial(config);
        assertTrue(EZRetrofitConfig.isInitial());
        assertDoesNotThrow(EZRetrofitConfig::checkInitialStatus);
        assertEquals(config, EZRetrofitConfig.getInstance());
    }

    @Test
    public void testLogger() {
        EZLogger customLogger = new EZLogger() {
            @Override
            public void warn(String tag, String message, Throwable t) {}
            @Override
            public void info(String tag, String message, Throwable t) {}
            @Override
            public void debug(String tag, String message, Throwable t) {}
            @Override
            public void error(String tag, String message, Throwable t) {}
        };
        EZRetrofitConfig.setLogger(customLogger);
        assertEquals(customLogger, EZRetrofitConfig.getLogger());

        EZRetrofitConfig.getLogger().info("tag", "msg", null);
        EZRetrofitConfig.getLogger().debug("tag", "msg", null);
        EZRetrofitConfig.getLogger().warn("tag", "msg", null);
        EZRetrofitConfig.getLogger().error("tag", "msg", null);
    }
}
