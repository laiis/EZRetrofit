package tw.idv.laiis.ezretrofit.config;

import okhttp3.CookieJar;
import okhttp3.Dns;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import tw.idv.laiis.ezretrofit.EZLogger;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitConfigTest {

    interface TestService {}

    @BeforeEach
    public void setUp() {
        EZRetrofitConfig.reset();
    }

    @Test
    public void testBuilderAndGetters() {
        SslConfig ssl = new SslConfig.Builder()
                .useSSLCertificatePinning(true)
                .build();
        ProxyConfig proxy = new ProxyConfig.Builder().build();
        TimeoutConfig timeout = new TimeoutConfig(30L);
        InterceptorConfig interceptors = new InterceptorConfig.Builder().build();

        EZRetrofitConfig config = new EZRetrofitConfig.Builder()
                .sslConfig(ssl)
                .proxyConfig(proxy)
                .timeout(timeout)
                .interceptorConfig(interceptors)
                .cookieJar(CookieJar.NO_COOKIES)
                .dns(Dns.SYSTEM)
                .addBaseUrlService(TestService.class, "https://api.test.com/")
                .build();

        assertTrue(config.getSslConfig().isUseSSLCertificatePinning());
        assertEquals(30L, config.getTimeoutConfig().getTimeoutSeconds());
        assertEquals(CookieJar.NO_COOKIES, config.getCookieJar());
        assertEquals(Dns.SYSTEM, config.getDns());
        assertEquals("https://api.test.com/", config.getBaseUrl(TestService.class));
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
        };
        EZRetrofitConfig.setLogger(customLogger);
        assertEquals(customLogger, EZRetrofitConfig.getLogger());
    }
}
