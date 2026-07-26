package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class SupportAllTlsSocketFactoryFullTest {

    private static class DummySSLSocketFactory extends SSLSocketFactory {
        @Override
        public String[] getDefaultCipherSuites() {
            return new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"};
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return new String[]{"TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256"};
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            return new Socket();
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            return new Socket();
        }

        @Override
        public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
            return new Socket();
        }

        @Override
        public Socket createSocket(InetAddress host, int port) throws IOException {
            return new Socket();
        }

        @Override
        public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
            return new Socket();
        }
    }

    @Test
    void testCreateSocketOverloads() throws Exception {
        DummySSLSocketFactory dummyFactory = new DummySSLSocketFactory();
        SupportAllTlsSocketFactory factory = new SupportAllTlsSocketFactory(new String[]{"TLSv1.2"}, dummyFactory);

        assertNotNull(factory.createSocket(new Socket(), "localhost", 443, true));
        assertNotNull(factory.createSocket("localhost", 443));
        assertNotNull(factory.createSocket("localhost", 443, InetAddress.getLoopbackAddress(), 0));
        assertNotNull(factory.createSocket(InetAddress.getLoopbackAddress(), 443));
        assertNotNull(factory.createSocket(InetAddress.getLoopbackAddress(), 443, InetAddress.getLoopbackAddress(), 0));
    }
}
