package tw.idv.laiis.ezretrofit.client;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import retrofit2.converter.gson.GsonConverterFactory;
import tw.idv.laiis.ezretrofit.config.*;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitClientCoverageTest {

    private interface MockApi {}

    @Test
    void testClientBuildFullConfiguration() {
        SslConfig sslConfig = new SslConfig.Builder()
                .useSSLCertificatePinning(true)
                .certificatePinner(new CertificatePinner.Builder().build())
                .build();

        ProxyConfig proxyConfig = new ProxyConfig.Builder()
                .proxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080)))
                .build();

        InterceptorConfig interceptorConfig = new InterceptorConfig.Builder()
                .build();

        EZRetrofitConfig config = new EZRetrofitConfig.Builder()
                .addBaseUrlService(MockApi.class, "https://mock.api.com/")
                .sslConfig(sslConfig)
                .proxyConfig(proxyConfig)
                .interceptorConfig(interceptorConfig)
                .timeout(new TimeoutConfig(30L))
                .followRedirects(true)
                .followSslRedirects(true)
                .hostnameVerifier((hostname, session) -> true)
                .addConverterFactory(GsonConverterFactory.create())
                .validateEagerly(true)
                .build();

        EZRetrofitClient client = new EZRetrofitClient(config);
        MockApi api = client.create(MockApi.class);

        assertNotNull(api);
    }
}
