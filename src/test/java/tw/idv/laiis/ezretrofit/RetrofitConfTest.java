package tw.idv.laiis.ezretrofit;

import okhttp3.CertificatePinner;
import okhttp3.Protocol;
import okhttp3.TlsVersion;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RetrofitConfTest {

    private interface DummyService {}

    @Test
    void testRetrofitConfBuilder() {
        CertificatePinner pinner = new CertificatePinner.Builder().build();

        RetrofitConf conf = new RetrofitConf.Builder()
                .baseUrls(DummyService.class, "https://api.example.com/")
                .timeout(30L)
                .setFollowRedirects(true)
                .setFollowSslRedirects(true)
                .setCertificatePinner(pinner)
                .setUseCertificatePinning(true)
                .setRetryOnConnectionFailure(true)
                .setProtocols(List.of(Protocol.HTTP_1_1, Protocol.HTTP_2))
                .build();

        assertEquals("https://api.example.com/", conf.getBaseUrl(DummyService.class));
        assertEquals(30L, conf.getTimeout());
        assertTrue(conf.isFollowRedirects());
        assertTrue(conf.isFollowSslRedirects());
        assertSame(pinner, conf.getCertificatePinner());
        assertTrue(conf.isUseSSLCertificatePinning());
        assertTrue(conf.isRetryOnConnectionFailure());
        assertEquals(2, conf.getProtocols().size());
    }

    @Test
    void testSSLFactoryManagerBuilder() {
        RetrofitConf.SSLFactoryManager manager = new RetrofitConf.SSLFactoryManager.Builder()
                .setProtocol(TlsVersion.TLS_1_2)
                .setSupportProtocols(new String[]{"TLSv1.2", "TLSv1.3"})
                .setIgnoreVerify(true)
                .build();

        assertNotNull(manager);
        assertNotNull(manager.getSslSocketFactory());
        assertNotNull(manager.getX509TrustManager());
    }

    @Test
    void testPinInterval() {
        RetrofitConf.PinInterval interval = new RetrofitConf.PinInterval(10L, TimeUnit.SECONDS);
        assertEquals(10L, interval.getInterval());
        assertEquals(TimeUnit.SECONDS, interval.getTimeUnit());
    }

    @Test
    void testDeprecatedSSLMethods() {
        RetrofitConf conf = new RetrofitConf.Builder().build();
        conf.enableSSLConnection();
        assertTrue(conf.isUseSSLConnection());
        conf.disableSSLConnection();
        assertFalse(conf.isUseSSLConnection());
    }
}
