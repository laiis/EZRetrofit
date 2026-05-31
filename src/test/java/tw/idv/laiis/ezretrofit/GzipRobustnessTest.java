package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class GzipRobustnessTest {

    @Test
    public void testCorruptGzipThrowsIOException() throws IOException {
        SafeGzipInterceptor interceptor = new SafeGzipInterceptor();

        Request request = new Request.Builder()
                .url("https://localhost/")
                .build();

        byte[] corruptData = new byte[] { 31, (byte) 139, 8, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3 };

        ResponseBody corruptBody = ResponseBody.create(
                new Buffer().write(corruptData),
                MediaType.parse("text/plain"),
                -1
        );

        Response response = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Content-Encoding", "gzip")
                .body(corruptBody)
                .build();

        Interceptor.Chain mockChain = new Interceptor.Chain() {
            @Override
            public Request request() {
                return request;
            }

            @Override
            public Response proceed(Request req) throws IOException {
                return response;
            }

            @Override
            public Connection connection() {
                return null;
            }

            @Override
            public Call call() {
                return null;
            }

            @Override
            public int connectTimeoutMillis() { return 0; }

            @Override
            public Interceptor.Chain withConnectTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }

            @Override
            public int readTimeoutMillis() { return 0; }

            @Override
            public Interceptor.Chain withReadTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }

            @Override
            public int writeTimeoutMillis() { return 0; }

            @Override
            public Interceptor.Chain withWriteTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }
        };

        Response interceptedResponse = interceptor.intercept(mockChain);
        assertNotNull(interceptedResponse);

        assertThrows(IOException.class, () -> {
            interceptedResponse.body().string();
        });
    }
}
