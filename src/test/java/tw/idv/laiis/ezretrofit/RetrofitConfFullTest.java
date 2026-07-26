package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import org.junit.jupiter.api.Test;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class RetrofitConfFullTest {

    private interface DummyApi {}

    @Test
    void testAllRetrofitConfBuilderMethodsAndGetters() {
        Interceptor dummyInterceptor = chain -> chain.proceed(chain.request());
        CertificatePinner pinner = new CertificatePinner.Builder().build();
        Authenticator authenticator = (route, response) -> null;
        ConnectionPool pool = new ConnectionPool();
        Cache cache = new Cache(new File("build/tmp/cache_legacy"), 5 * 1024 * 1024);
        Dispatcher dispatcher = new Dispatcher();
        Dns dns = Dns.SYSTEM;
        HostnameVerifier verifier = (hostname, session) -> true;
        RetrofitConf.PinInterval pinInterval = new RetrofitConf.PinInterval(15L, TimeUnit.SECONDS);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8888));
        ProxySelector proxySelector = ProxySelector.getDefault();
        SocketFactory socketFactory = SocketFactory.getDefault();

        RetrofitConf.SSLFactoryManager sslManager = new RetrofitConf.SSLFactoryManager.Builder()
                .setProtocol(TlsVersion.TLS_1_2)
                .setSupportProtocols(new String[]{"TLSv1.2"})
                .setIgnoreVerify(true)
                .build();

        Executor executor = Runnable::run;
        Call.Factory callFactory = request -> null;

        RetrofitConf conf = new RetrofitConf.Builder()
                .baseUrls(DummyApi.class, "https://api.fulltest.com/")
                .setProtocol("HTTP/1.1")
                .timeout(25L)
                .setCookieJar(CookieJar.NO_COOKIES)
                .setInterceptors(List.of(dummyInterceptor))
                .setNetworkInterceptorList(List.of(dummyInterceptor))
                .setCertificatePinner(pinner)
                .setProtocols(List.of(Protocol.HTTP_1_1))
                .setAuthenticator(authenticator)
                .setConnectionPool(pool)
                .setRetryOnConnectionFailure(true)
                .setCache(cache)
                .setDispatcher(dispatcher)
                .setDns(dns)
                .setFollowRedirects(false)
                .setFollowSslRedirects(false)
                .setHostnameVerifier(verifier)
                .setPinInterval(pinInterval)
                .setProxy(proxy)
                .setProxyAuthencator(authenticator)
                .setProxySelector(proxySelector)
                .setSocketFactory(socketFactory)
                .setSSLFactoryManager(sslManager)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .setValidateEagerly(true)
                .setExecutor(executor)
                .setOKHttp3Factory(callFactory)
                .setConnectionSpecList(List.of(ConnectionSpec.MODERN_TLS))
                .setUseCertificatePinning(true)
                .setUseSSLFactoryManager(true)
                .build();

        assertEquals("https://api.fulltest.com/", conf.getBaseUrl(DummyApi.class));
        assertEquals(1, conf.getWebserviceMap().size());
        assertEquals("HTTP/1.1", conf.getProtocol());
        assertEquals(25L, conf.getTimeout());
        assertEquals(CookieJar.NO_COOKIES, conf.getCookieJar());
        assertEquals(1, conf.getInterceptorList().size());
        assertEquals(1, conf.getNetworkInterceptorList().size());
        assertEquals(pinner, conf.getCertificatePinner());
        assertEquals(pinner, conf.getCertficatePinner());
        assertEquals(1, conf.getProtocols().size());
        assertEquals(authenticator, conf.getAuthenticator());
        assertEquals(pool, conf.getConnectionPool());
        assertTrue(conf.isRetryOnConnectionFailure());
        assertEquals(cache, conf.getCache());
        assertEquals(dispatcher, conf.getDispatcher());
        assertEquals(dns, conf.getDns());
        assertFalse(conf.isFollowRedirects());
        assertFalse(conf.isFollowSslRedirects());
        assertEquals(verifier, conf.getHostnameVerifier());
        assertEquals(pinInterval, conf.getPinInterval());
        assertEquals(proxy, conf.getProxy());
        assertEquals(authenticator, conf.getProxyAuthenticator());
        assertEquals(proxySelector, conf.getProxySelector());
        assertEquals(socketFactory, conf.getSocketFactory());
        assertEquals(sslManager, conf.getSSLFactoryManager());
        assertEquals(1, conf.getCallAdapterFactoryList().size());
        assertEquals(1, conf.getConverterFactoryList().size());
        assertTrue(conf.isValidateEagerly());
        assertEquals(executor, conf.getExecutor());
        assertEquals(callFactory, conf.getOKHttp3Factory());
        assertEquals(1, conf.getConnectionSpecList().size());
        assertTrue(conf.isUseSSLCertificatePinning());
        assertTrue(conf.isUseSSLFactoryManager());
    }

    @Test
    void testDirectSetters() {
        RetrofitConf conf = new RetrofitConf.Builder().build();
        conf.setProtocol("HTTP/2");
        assertEquals("HTTP/2", conf.getProtocol());

        conf.setCertPins(new String[]{"p1"});
        assertArrayEquals(new String[]{"p1"}, conf.getCertPins());

        conf.setTimeout(50L);
        assertEquals(50L, conf.getTimeout());

        conf.setCertificatePinner(new CertificatePinner.Builder().build());
        assertNotNull(conf.getCertificatePinner());

        conf.addBaseUrlService(DummyApi.class, "https://direct.com/");
        assertEquals("https://direct.com/", conf.getBaseUrl(DummyApi.class));

        conf.setProtocols(List.of(Protocol.HTTP_1_1));
        assertEquals(1, conf.getProtocols().size());

        conf.setCookieJar(CookieJar.NO_COOKIES);
        assertEquals(CookieJar.NO_COOKIES, conf.getCookieJar());
    }
}
