package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class HttpsHeaderSecurityTest {

    @Test
    public void testSensitiveHeaderRedactedInExceptions() {
        // 驗證當設定不合法的敏感 Header (例如 Authorization, Cookie) 時，拋出的 Exception 不會洩漏敏感內容
        IllegalArgumentException authEx = assertThrows(IllegalArgumentException.class, () -> {
            ParamCreator.validateHeader("Authorization", "Bearer invalid\nvalue");
        });
        assertTrue(authEx.getMessage().contains("value redacted"), "Exception message should redact sensitive authorization value");
        assertFalse(authEx.getMessage().contains("invalid\nvalue"), "Exception message must not contain the sensitive value itself");

        IllegalArgumentException cookieEx = assertThrows(IllegalArgumentException.class, () -> {
            ParamCreator.validateHeader("Cookie", "session=123\u007f456");
        });
        assertTrue(cookieEx.getMessage().contains("value redacted"), "Exception message should redact sensitive cookie value");
        assertFalse(cookieEx.getMessage().contains("123"), "Exception message must not contain the sensitive value itself");
    }

    @Test
    public void testNonSensitiveHeaderExceptionNotRedacted() {
        // 驗證非敏感 Header 的異常訊息中會包含不合法內容
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            ParamCreator.validateHeader("Custom-Header", "invalid\nvalue");
        });
        assertTrue(ex.getMessage().contains("invalid\nvalue"), "Non-sensitive header should show the invalid value for debugging");
    }
}
