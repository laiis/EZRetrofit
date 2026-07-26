package tw.idv.laiis.ezretrofit;

import okhttp3.TlsVersion;

import javax.net.ssl.*;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Enables TLS v1.2 when creating SSLSockets.
 * <p/>
 * For some reason, android supports TLS v1.2 from API 16, but enables it by
 * default only from API 20.
 *
 * @link https://developer.android.com/reference/javax/net/ssl/SSLSocket.html
 * @see SSLSocketFactory
 */
public class SupportAllTlsSocketFactory extends SSLSocketFactory {

    private final SSLSocketFactory delegate;
    private String[] currentSupportTls;

    public SupportAllTlsSocketFactory(String[] supportsTls, SSLSocketFactory base) {
        this.delegate = base;
        currentSupportTls = supportsTls;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return delegate.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return delegate.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        return patch(delegate.createSocket(s, host, port, autoClose));
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return patch(delegate.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return patch(delegate.createSocket(host, port));
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return patch(delegate.createSocket(address, port, localAddress, localPort));
    }

    private Socket patch(Socket socket) {
        if (socket instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) socket;
            sslSocket.setEnabledProtocols(currentSupportTls);
            java.util.List<okhttp3.CipherSuite> modernSuites = okhttp3.ConnectionSpec.MODERN_TLS.cipherSuites();
            if (modernSuites != null) {
                java.util.List<String> enabledList = new java.util.ArrayList<>();
                java.util.List<String> supportedList = java.util.Arrays.asList(sslSocket.getSupportedCipherSuites());
                for (okhttp3.CipherSuite suite : modernSuites) {
                    String suiteName = suite.javaName();
                    if (supportedList.contains(suiteName)) {
                        enabledList.add(suiteName);
                    }
                }
                sslSocket.setEnabledCipherSuites(enabledList.toArray(new String[0]));
            }
            try {
                javax.net.ssl.SSLParameters sslParams = sslSocket.getSSLParameters();
                sslParams.setEndpointIdentificationAlgorithm("HTTPS");
                sslSocket.setSSLParameters(sslParams);
            } catch (Exception e) {
                tw.idv.laiis.ezretrofit.config.EZRetrofitConfig.getLogger().warn("SupportAllTlsSocketFactory", "Failed to set SSL parameters on socket", e);
            }
        }
        return socket;
    }
}