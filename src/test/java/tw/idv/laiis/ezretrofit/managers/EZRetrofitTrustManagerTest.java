package tw.idv.laiis.ezretrofit.managers;

import org.junit.Test;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Set;
import static org.junit.Assert.*;

public class EZRetrofitTrustManagerTest {

    @Test
    public void testValidateCertificatePinEmptyPins() throws Exception {
        X509Certificate mockCert = new DummyX509Certificate(new byte[]{1, 2, 3});

        // 1. 測試 null pins 應安全返回，不拋 NPE
        EZRetrofitTrustManager trustManagerNull = new EZRetrofitTrustManager(null, null);
        
        try {
            java.lang.reflect.Method method = EZRetrofitTrustManager.class.getDeclaredMethod("validateCertificatePin", X509Certificate.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(trustManagerNull, mockCert);
            assertTrue("validateCertificatePin should return true when pins are null", result);
        } catch (Exception e) {
            fail("Should not throw exception when pins are null: " + e.getMessage());
        }

        // 2. 測試空 pins 陣列
        EZRetrofitTrustManager trustManagerEmpty = new EZRetrofitTrustManager(null, new String[0]);
        try {
            java.lang.reflect.Method method = EZRetrofitTrustManager.class.getDeclaredMethod("validateCertificatePin", X509Certificate.class);
            method.setAccessible(true);
            boolean result = (boolean) method.invoke(trustManagerEmpty, mockCert);
            assertTrue("validateCertificatePin should return true when pins are empty", result);
        } catch (Exception e) {
            fail("Should not throw exception when pins are empty: " + e.getMessage());
        }
    }

    @Test
    public void testSha256PinValidation() throws Exception {
        byte[] pubKeyEncoded = new byte[]{10, 20, 30, 40};
        X509Certificate mockCert = new DummyX509Certificate(pubKeyEncoded);

        // 使用 SHA-256 計算正確的 Pin
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] sha256Bytes = digest.digest(pubKeyEncoded);
        
        StringBuilder sb = new StringBuilder();
        for (byte b : sha256Bytes) {
            sb.append(String.format("%02X", b));
        }
        String correctSha256Pin = sb.toString();

        // 1. 使用正確的 SHA-256 Pin
        EZRetrofitTrustManager trustManager = new EZRetrofitTrustManager(null, new String[]{correctSha256Pin});
        java.lang.reflect.Method method = EZRetrofitTrustManager.class.getDeclaredMethod("validateCertificatePin", X509Certificate.class);
        method.setAccessible(true);
        boolean result = (boolean) method.invoke(trustManager, mockCert);
        assertTrue("SHA-256 Pin matching should succeed", result);

        // 2. 使用不正確的 Pin 驗證
        EZRetrofitTrustManager trustManagerWrong = new EZRetrofitTrustManager(null, new String[]{"WRONGPINHEX123456"});
        boolean resultWrong = (boolean) method.invoke(trustManagerWrong, mockCert);
        assertFalse("Incorrect SHA-256 Pin should fail", resultWrong);

        // 3. 測試已被廢棄的 SHA-1 Pin (在此應被視為無效或不相符，因為我們強制使用 SHA-256)
        java.security.MessageDigest sha1Digest = java.security.MessageDigest.getInstance("SHA-1");
        byte[] sha1Bytes = sha1Digest.digest(pubKeyEncoded);
        StringBuilder sb1 = new StringBuilder();
        for (byte b : sha1Bytes) {
            sb1.append(String.format("%02X", b));
        }
        String legacySha1Pin = sb1.toString();
        
        EZRetrofitTrustManager trustManagerSha1 = new EZRetrofitTrustManager(null, new String[]{legacySha1Pin});
        boolean resultSha1 = (boolean) method.invoke(trustManagerSha1, mockCert);
        assertFalse("Legacy SHA-1 Pin should fail validation", resultSha1);
    }

    private static class DummyX509Certificate extends X509Certificate {
        private final byte[] encodedKey;

        public DummyX509Certificate(byte[] encodedKey) {
            this.encodedKey = encodedKey;
        }

        @Override
        public PublicKey getPublicKey() {
            return new PublicKey() {
                @Override
                public String getAlgorithm() { return "RSA"; }
                @Override
                public String getFormat() { return "X.509"; }
                @Override
                public byte[] getEncoded() { return encodedKey; }
            };
        }

        // 以下為 X509Certificate 必須實現的虛設方法
        @Override
        public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {}
        @Override
        public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {}
        @Override
        public int getVersion() { return 3; }
        @Override
        public BigInteger getSerialNumber() { return BigInteger.ONE; }
        @Override
        public Principal getIssuerDN() { return null; }
        @Override
        public Principal getSubjectDN() { return null; }
        @Override
        public Date getNotBefore() { return new Date(); }
        @Override
        public Date getNotAfter() { return new Date(); }
        @Override
        public byte[] getTBSCertificate() throws CertificateEncodingException { return new byte[0]; }
        @Override
        public byte[] getSignature() { return new byte[0]; }
        @Override
        public String getSigAlgName() { return null; }
        @Override
        public String getSigAlgOID() { return null; }
        @Override
        public byte[] getSigAlgParams() { return new byte[0]; }
        @Override
        public boolean[] getIssuerUniqueID() { return new boolean[0]; }
        @Override
        public boolean[] getSubjectUniqueID() { return new boolean[0]; }
        @Override
        public boolean[] getKeyUsage() { return new boolean[0]; }
        @Override
        public int getBasicConstraints() { return 0; }
        @Override
        public byte[] getEncoded() throws CertificateEncodingException { return new byte[0]; }
        @Override
        public void verify(PublicKey key) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {}
        @Override
        public void verify(PublicKey key, String sigProvider) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {}
        @Override
        public String toString() { return ""; }
        @Override
        public boolean hasUnsupportedCriticalExtension() { return false; }
        @Override
        public Set<String> getCriticalExtensionOIDs() { return null; }
        @Override
        public byte[] getExtensionValue(String oid) { return new byte[0]; }
        @Override
        public Set<String> getNonCriticalExtensionOIDs() { return null; }
    }
}
