package tw.idv.laiis.ezretrofit.managers;

import org.junit.Test;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import static org.junit.Assert.*;

public class SSLFactoryManagerTest {

    @Test
    public void testBuildMissingTlsVersionThrowsException() {
        RetrofitConf.SSLFactoryManager.Builder builder = new RetrofitConf.SSLFactoryManager.Builder()
                .setSupportProtocols(new String[]{"TLSv1.2"});
        
        try {
            builder.build();
            fail("Should throw IllegalStateException when TlsVersion is missing");
        } catch (IllegalStateException e) {
            assertEquals("TlsVersion cannot be null", e.getMessage());
        }
    }

    @Test
    public void testBuildMissingSupportProtocolsThrowsException() {
        RetrofitConf.SSLFactoryManager.Builder builder = new RetrofitConf.SSLFactoryManager.Builder()
                .setProtocol(okhttp3.TlsVersion.TLS_1_2);
        
        try {
            builder.build();
            fail("Should throw IllegalStateException when SupportProtocols is missing");
        } catch (IllegalStateException e) {
            assertEquals("SupportProtocols cannot be null or empty", e.getMessage());
        }
    }
}
