package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ParamCreatorTest {

    @Test
    void testSensitiveHeaders() {
        assertTrue(ParamCreator.isSensitiveHeader("Authorization"));
        assertTrue(ParamCreator.isSensitiveHeader("Cookie"));
        assertTrue(ParamCreator.isSensitiveHeader("Set-Cookie"));
        assertTrue(ParamCreator.isSensitiveHeader("Proxy-Authorization"));
        assertFalse(ParamCreator.isSensitiveHeader("Content-Type"));
        assertFalse(ParamCreator.isSensitiveHeader(null));
    }

    @Test
    void testValidateHeader_InvalidName() {
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader("", "value"));
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader(null, "value"));
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader("Bad Header", "value"));
    }

    @Test
    void testValidateHeader_InvalidValue() {
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader("Key", null));
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader("Key", "Value\nWithNewline"));
        assertThrows(IllegalArgumentException.class, () -> ParamCreator.validateHeader("Authorization", "Bearer \nSecret"));
    }

    @Test
    void testParamCreatorBuilder() {
        ParamCreator.Builder<String> builder = new ParamCreator.Builder<>();
        builder.putValue("Custom-Header", "Value1");
        builder.putValue("Authorization", "Bearer token123");

        Map<String, String> map = builder.build();
        assertEquals(2, map.size());
        assertEquals("Value1", map.get("Custom-Header"));
    }
}
