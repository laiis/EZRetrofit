package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import okio.Buffer;
import okio.GzipSink;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SafeGzipInterceptorTest {

    @Test
    public void testSkipDecompressionWhenContentEncodingMissing() throws IOException {
        SafeGzipInterceptor interceptor = new SafeGzipInterceptor();

        Request request = new Request.Builder()
                .url("https://example.com")
                .build();

        String rawContent = "Plaintext content";
        Response originalResponse = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .body(ResponseBody.create(rawContent, MediaType.parse("text/plain")))
                .build();

        Interceptor.Chain chain = new MockChain(request, originalResponse);
        Response interceptedResponse = interceptor.intercept(chain);

        // When Content-Encoding is missing (e.g., OkHttp already decompressed), SafeGzipInterceptor does nothing
        assertNotNull(interceptedResponse);
        assertNull(interceptedResponse.header("Content-Encoding"));
        assertEquals(rawContent, interceptedResponse.body().string());
    }

    @Test
    public void testDecompressWhenContentEncodingIsGzip() throws IOException {
        SafeGzipInterceptor interceptor = new SafeGzipInterceptor();

        Request request = new Request.Builder()
                .url("https://example.com")
                .build();

        String plainText = "Hello Gzip World!";
        Buffer gzippedBuffer = new Buffer();
        GzipSink gzipSink = new GzipSink(gzippedBuffer);
        gzipSink.write(new Buffer().writeUtf8(plainText), plainText.length());
        gzipSink.close();

        Response gzippedResponse = new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(200)
                .message("OK")
                .header("Content-Encoding", "gzip")
                .body(ResponseBody.create(gzippedBuffer.readByteArray(), MediaType.parse("text/plain")))
                .build();

        Interceptor.Chain chain = new MockChain(request, gzippedResponse);
        Response interceptedResponse = interceptor.intercept(chain);

        assertNotNull(interceptedResponse);
        assertNull(interceptedResponse.header("Content-Encoding"));
        assertEquals(plainText, interceptedResponse.body().string());
    }

    private static class MockChain implements Interceptor.Chain {
        private final Request request;
        private final Response response;

        public MockChain(Request request, Response response) {
            this.request = request;
            this.response = response;
        }

        @Override public Request request() { return request; }
        @Override public Response proceed(Request request) throws IOException { return response; }
        @Override public Connection connection() { return null; }
        @Override public Call call() { return null; }
        @Override public int connectTimeoutMillis() { return 0; }
        @Override public Interceptor.Chain withConnectTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }
        @Override public int readTimeoutMillis() { return 0; }
        @Override public Interceptor.Chain withReadTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }
        @Override public int writeTimeoutMillis() { return 0; }
        @Override public Interceptor.Chain withWriteTimeout(int timeout, java.util.concurrent.TimeUnit unit) { return this; }
    }
}
