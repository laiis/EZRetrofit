package tw.idv.laiis.ezretrofit.managers;

import org.junit.jupiter.api.Test;
import tw.idv.laiis.ezretrofit.BuildConfig;
import java.security.cert.X509Certificate;
import static org.junit.jupiter.api.Assertions.*;

public class DefaultTestingTrustManagerTest {

    @Test
    public void testEnvironmentCheck() throws Exception {
        DefaultTestingTrustManager trustManager = new DefaultTestingTrustManager();
        X509Certificate[] chain = new X509Certificate[0];

        // 1. 根據目前的 BuildConfig.DEBUG 狀態進行測試
        if (BuildConfig.DEBUG) {
            // Debug 模式：呼叫不應拋出例外
            try {
                trustManager.checkServerTrusted(chain, "RSA");
                trustManager.checkClientTrusted(chain, "RSA");
            } catch (SecurityException e) {
                fail("Should not throw SecurityException in debug mode: " + e.getMessage());
            }
        } else {
            // Release 模式：呼叫必須拋出 SecurityException
            try {
                trustManager.checkServerTrusted(chain, "RSA");
                fail("Should throw SecurityException in release mode");
            } catch (SecurityException e) {
                assertEquals("DefaultTestingTrustManager must not be used in production", e.getMessage());
            }
        }

        // 2. 嘗試透過反射修改 DEBUG 旗標來驗證另一種環境下的行為
        try {
            java.lang.reflect.Field debugField = BuildConfig.class.getDeclaredField("DEBUG");
            debugField.setAccessible(true);

            // 嘗試移除 final 限制
            try {
                java.lang.reflect.Field modifiersField = java.lang.reflect.Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(debugField, debugField.getModifiers() & ~java.lang.reflect.Modifier.FINAL);
            } catch (Exception ignored) {
                // JDK 12+ 開始反射修改 modifiers 可能被封鎖，我們優雅忽略
            }

            boolean originalValue = BuildConfig.DEBUG;
            try {
                debugField.set(null, !originalValue);
                boolean newValue = BuildConfig.DEBUG;

                if (newValue != originalValue) {
                    // 成功修改，進行反向測試
                    if (newValue) {
                        trustManager.checkServerTrusted(chain, "RSA");
                    } else {
                        try {
                            trustManager.checkServerTrusted(chain, "RSA");
                            fail("Should throw SecurityException when DEBUG reflection-set to false");
                        } catch (SecurityException e) {
                            assertEquals("DefaultTestingTrustManager must not be used in production", e.getMessage());
                        }
                    }
                }
            } finally {
                // 還原原始值
                try {
                    debugField.set(null, originalValue);
                } catch (Exception ignored) {}
            }
        } catch (Exception ignored) {
            // 反射失敗在現代 JDK 下為正常，因 JVM 限制
        }
    }
}
