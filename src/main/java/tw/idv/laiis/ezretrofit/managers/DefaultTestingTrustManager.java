package tw.idv.laiis.ezretrofit.managers;

import javax.net.ssl.X509TrustManager;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Created by laiis on 2017/8/1.
 * <p>
 * NOTE!!<br/>
 * This X509TrustManager is use to be at testing mode in the develop environment.
 * Don't use this in production environment.
 */
public class DefaultTestingTrustManager implements X509TrustManager {

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}
