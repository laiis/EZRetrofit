package tw.idv.laiis.ezretrofit.cookies;

import okhttp3.Cookie;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.junit.jupiter.api.Assertions.*;

public class SerializableHttpCookieTest {

    @Test
    void testCookieSerialization() throws Exception {
        Cookie cookie = new Cookie.Builder()
                .name("test_name")
                .value("test_val")
                .domain("example.com")
                .path("/")
                .secure()
                .httpOnly()
                .build();

        SerializableHttpCookie wrapper = new SerializableHttpCookie(cookie);
        assertEquals(cookie, wrapper.getCookie());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(wrapper);
        oos.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableHttpCookie deserialized = (SerializableHttpCookie) ois.readObject();

        assertNotNull(deserialized.getCookie());
        assertEquals("test_name", deserialized.getCookie().name());
        assertEquals("test_val", deserialized.getCookie().value());
        assertEquals("example.com", deserialized.getCookie().domain());
    }
}
