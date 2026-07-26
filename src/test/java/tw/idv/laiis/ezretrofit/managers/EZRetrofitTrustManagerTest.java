package tw.idv.laiis.ezretrofit.managers;

import org.junit.jupiter.api.Test;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitTrustManagerTest {

    @Test
    void testEZRetrofitTrustManagerInitialization() throws Exception {
        EZRetrofitTrustManager tm = new EZRetrofitTrustManager();
        assertNotNull(tm);
        X509Certificate[] acceptedIssuers = tm.getAcceptedIssuers();
        assertNotNull(acceptedIssuers);
    }

    @Test
    void testEZRetrofitTrustManagerWithPins() throws Exception {
        String[] pins = new String[]{"ABCDEF1234567890"};
        EZRetrofitTrustManager tm = new EZRetrofitTrustManager(null, pins);
        assertNotNull(tm);
    }

    @Test
    void testCheckClientAndServerTrusted() throws Exception {
        EZRetrofitTrustManager tm = new EZRetrofitTrustManager();
        X509Certificate[] emptyChain = new X509Certificate[0];
        
        assertThrows(Exception.class, () -> tm.checkClientTrusted(emptyChain, "RSA"));
        assertThrows(Exception.class, () -> tm.checkServerTrusted(emptyChain, "RSA"));
    }
}
