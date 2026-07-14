package tw.idv.laiis.ezretrofit.config;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import okhttp3.CertificatePinner;

public final class SslConfig {
    private final boolean useSSLCertificatePinning;
    private final boolean useSSLFactoryManager;
    private final CertificatePinner certificatePinner;
    private final SSLSocketFactory sslSocketFactory;
    private final X509TrustManager x509TrustManager;

    private SslConfig(Builder builder) {
        this.useSSLCertificatePinning = builder.useSSLCertificatePinning;
        this.useSSLFactoryManager = builder.useSSLFactoryManager;
        this.certificatePinner = builder.certificatePinner;
        this.sslSocketFactory = builder.sslSocketFactory;
        this.x509TrustManager = builder.x509TrustManager;
    }

    public boolean isUseSSLCertificatePinning() {
        return useSSLCertificatePinning;
    }

    public boolean isUseSSLFactoryManager() {
        return useSSLFactoryManager;
    }

    public CertificatePinner getCertificatePinner() {
        return certificatePinner;
    }

    public SSLSocketFactory getSslSocketFactory() {
        return sslSocketFactory;
    }

    public X509TrustManager getX509TrustManager() {
        return x509TrustManager;
    }

    public static class Builder {
        private boolean useSSLCertificatePinning;
        private boolean useSSLFactoryManager;
        private CertificatePinner certificatePinner;
        private SSLSocketFactory sslSocketFactory;
        private X509TrustManager x509TrustManager;

        public Builder useSSLCertificatePinning(boolean useSSLCertificatePinning) {
            this.useSSLCertificatePinning = useSSLCertificatePinning;
            return this;
        }

        public Builder useSSLFactoryManager(boolean useSSLFactoryManager) {
            this.useSSLFactoryManager = useSSLFactoryManager;
            return this;
        }

        public Builder certificatePinner(CertificatePinner certificatePinner) {
            this.certificatePinner = certificatePinner;
            return this;
        }

        public Builder sslSocketFactory(SSLSocketFactory sslSocketFactory) {
            this.sslSocketFactory = sslSocketFactory;
            return this;
        }

        public Builder x509TrustManager(X509TrustManager x509TrustManager) {
            this.x509TrustManager = x509TrustManager;
            return this;
        }

        public SslConfig build() {
            return new SslConfig(this);
        }
    }
}
