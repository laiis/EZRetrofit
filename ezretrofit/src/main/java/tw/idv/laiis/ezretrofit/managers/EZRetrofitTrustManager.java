package tw.idv.laiis.ezretrofit.managers;

/**
 * Created by laiis on 2017/5/8.
 */

import android.util.Log;

import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import tw.idv.laiis.ezretrofit.BuildConfig;

/**
 * ref : https://github.com/rfreedman/android-ssl/blob/master/src/main/java/com/chariotsolutions/example/http/CustomTrustManager.java <p>
 * <p/>
 * A custom X509TrustManager implementation that trusts a specified server certificate in addition
 * to those that are in the system TrustStore.
 * Also handles an out-of-order certificate chain, as is often produced by Apache's mod_ssl
 */
public class EZRetrofitTrustManager implements X509TrustManager {

    private static final String TAG = EZRetrofitTrustManager.class.getName();
    private final String ALGORITHM_SHA1 = "SHA-1";

    private KeyStore mKeyStore;
    private String[] mPins;
    private X509TrustManager mX509TrustManager;
    private MessageDigest mMessageDigest;

    public EZRetrofitTrustManager(KeyStore keyStore, String[] pins) throws NoSuchAlgorithmException {
        this.mKeyStore = keyStore;
        this.mPins = pins;
        this.mMessageDigest = MessageDigest.getInstance(ALGORITHM_SHA1);
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(mKeyStore);
            mX509TrustManager = getTrustManager(trustManagerFactory);

            if (mX509TrustManager == null) {
                throw new CertificateException("You must create a X509TrustManager instance.!");
            }

        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "---> " + e.getMessage());
            }
        }
    }

    private X509TrustManager getTrustManager(TrustManagerFactory factory) {
        for (TrustManager tm : factory.getTrustManagers()) {
            if (tm instanceof X509TrustManager) {
                return (X509TrustManager) tm;
            }
        }

        return null;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
        return mX509TrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        mX509TrustManager.checkClientTrusted(chain, authType);
    }

    private boolean validateCertificatePin(X509Certificate cert) throws CertificateException {
        final byte[] pubKeyInfo = cert.getPublicKey().getEncoded();
        final byte[] pin = mMessageDigest.digest(pubKeyInfo);
        final String pinAsHex = bytesToHex(pin);
        for (String validPin : mPins) {
            boolean result = validPin.equalsIgnoreCase(pinAsHex);
            Log.d(TAG, " ---> " + validPin + " == " + pinAsHex + " : " + result);
            if (result) {
                return true;
            }
        }
        return false;
    }

    /**
     * Given the partial or complete certificate chain provided by the peer,
     * build a certificate path to a trusted root and return if it can be validated and is trusted
     * for client SSL authentication based on the authentication type. The authentication type is
     * determined by the actual certificate used. For instance, if RSAPublicKey is used, the authType should be "RSA".
     * Checking is case-sensitive.
     * Defers to the default trust manager first, checks the cert supplied in the ctor if that fails.
     *
     * @param chain    the server's certificate chain
     * @param authType the authentication type based on the client certificate
     * @throws java.security.cert.CertificateException
     */
    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
        try {
            mX509TrustManager.checkServerTrusted(chain, authType);
        } catch (CertificateException originalException) {
            try {
                if (mPins != null) {
                    for (X509Certificate cert : chain) {
                        final boolean expected = validateCertificatePin(cert);
                        if (!expected) {
                            throw new CertificateException("Could not find a valid pin");
                        }
                    }
                }

                if (mKeyStore != null) {
                    X509Certificate[] reorderedChain = reorderCertificateChain(chain);
                    CertPathValidator validator = CertPathValidator.getInstance("PKIX");
                    CertificateFactory factory = CertificateFactory.getInstance("X509");
                    CertPath certPath = factory.generateCertPath(Arrays.asList(reorderedChain));
                    PKIXParameters params = new PKIXParameters(mKeyStore);
                    params.setRevocationEnabled(false);
                    validator.validate(certPath, params);
                }

            } catch (Exception ex) {
                throw originalException;
            }
        }

    }

    /**
     * Puts the certificate chain in the proper order, to deal with out-of-order
     * certificate chains as are sometimes produced by Apache's mod_ssl
     *
     * @param chain the certificate chain, possibly with bad ordering
     * @return the re-ordered certificate chain
     */
    private X509Certificate[] reorderCertificateChain(X509Certificate[] chain) {

        X509Certificate[] reorderedChain = new X509Certificate[chain.length];
        List<X509Certificate> certificates = Arrays.asList(chain);

        int position = chain.length - 1;
        X509Certificate rootCert = findRootCert(certificates);
        reorderedChain[position] = rootCert;

        X509Certificate cert = rootCert;
        while ((cert = findSignedCert(cert, certificates)) != null && position > 0) {
            reorderedChain[--position] = cert;
        }

        return reorderedChain;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the root certificate in a possibly out-of-order certificate chain.
     *
     * @param certificates the certificate change, possibly out-of-order
     * @return the root certificate, if any, that was found in the list of certificates
     */
    private X509Certificate findRootCert(List<X509Certificate> certificates) {
        X509Certificate rootCert = null;

        for (X509Certificate cert : certificates) {
            X509Certificate signer = findSigner(cert, certificates);
            if (signer == null || signer.equals(cert)) { // no signer present, or self-signed
                rootCert = cert;
                break;
            }
        }

        return rootCert;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the first certificate in the list of certificates that is signed by the sigingCert.
     */
    private X509Certificate findSignedCert(X509Certificate signingCert, List<X509Certificate> certificates) {
        X509Certificate signed = null;

        for (X509Certificate cert : certificates) {
            Principal signingCertSubjectDN = signingCert.getSubjectDN();
            Principal certIssuerDN = cert.getIssuerDN();
            if (certIssuerDN.equals(signingCertSubjectDN) && !cert.equals(signingCert)) {
                signed = cert;
                break;
            }
        }

        return signed;
    }

    /**
     * A helper method for certificate re-ordering.
     * Finds the certificate in the list of certificates that signed the signedCert.
     */
    private X509Certificate findSigner(X509Certificate signedCert, List<X509Certificate> certificates) {
        X509Certificate signer = null;

        for (X509Certificate cert : certificates) {
            Principal certSubjectDN = cert.getSubjectDN();
            Principal issuerDN = signedCert.getIssuerDN();
            if (certSubjectDN.equals(issuerDN)) {
                signer = cert;
                break;
            }
        }

        return signer;
    }

    private String bytesToHex(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

        char[] hexChars = new char[bytes.length * 2];
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
}