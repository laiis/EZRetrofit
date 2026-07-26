package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class SupportAllTlsSocketFactoryTest {

    @Test
    void testSupportAllTlsSocketFactoryDelegation() throws Exception {
        SSLSocketFactory defaultFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        String[] protocols = new String[]{"TLSv1.2", "TLSv1.3"};

        SupportAllTlsSocketFactory factory = new SupportAllTlsSocketFactory(protocols, defaultFactory);

        assertNotNull(factory.getDefaultCipherSuites());
        assertNotNull(factory.getSupportedCipherSuites());
        assertTrue(factory.getDefaultCipherSuites().length > 0);
    }
}
