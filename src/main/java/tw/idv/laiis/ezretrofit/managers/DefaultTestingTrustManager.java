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

    private final String expectedSha256Fingerprint;

    public DefaultTestingTrustManager() {
        this(null);
    }

    public DefaultTestingTrustManager(String expectedSha256Fingerprint) {
        this.expectedSha256Fingerprint = expectedSha256Fingerprint;
    }

    @Override
    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        verifySecurity(x509Certificates);
    }

    @Override
    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
        verifySecurity(x509Certificates);
    }

    private void verifySecurity(X509Certificate[] x509Certificates) throws CertificateException {
        if (!tw.idv.laiis.ezretrofit.BuildConfig.DEBUG) {
            throw new SecurityException("DefaultTestingTrustManager must not be used in production");
        }
        if (expectedSha256Fingerprint != null && !expectedSha256Fingerprint.isEmpty()) {
            if (x509Certificates == null || x509Certificates.length == 0) {
                throw new SecurityException("Certificate chain is empty, expected SHA-256 fingerprint verification failed");
            }
            boolean matched = false;
            try {
                // 每次呼叫獨立建立 MessageDigest 實例，確保多執行緒下的安全性 (T2/S1)
                java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
                for (X509Certificate cert : x509Certificates) {
                    byte[] digest = md.digest(cert.getEncoded());
                    StringBuilder sb = new StringBuilder();
                    for (byte b : digest) {
                        sb.append(String.format("%02X", b));
                    }
                    if (sb.toString().equalsIgnoreCase(expectedSha256Fingerprint.replace(":", ""))) {
                        matched = true;
                        break;
                    }
                }
            } catch (Exception e) {
                if (e instanceof SecurityException) {
                    throw (SecurityException) e;
                }
                throw new SecurityException("Failed to calculate certificate SHA-256 fingerprint", e);
            }
            if (!matched) {
                throw new SecurityException("Certificate SHA-256 fingerprint mismatch");
            }
        }
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return new X509Certificate[0];
    }
}

