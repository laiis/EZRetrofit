package tw.idv.laiis.ezretrofit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

import java.io.IOException;

public final class SafeGzipInterceptor implements Interceptor {
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        ResponseBody body = response.body();
        if (body == null) {
            return response;
        }

        // 若 Response 仍包含 Content-Encoding: gzip（表示 OkHttp 未自動解壓），執行安全解壓縮 (F1)
        String contentEncoding = response.header("Content-Encoding");
        if (contentEncoding != null && contentEncoding.equalsIgnoreCase("gzip")) {
            BufferedSource bodySource = body.source();
            Source gzipSource = new GzipSource(bodySource);
            Source safeSource = new ForwardingSource(gzipSource) {
                @Override
                public long read(okio.Buffer sink, long byteCount) throws IOException {
                    try {
                        return super.read(sink, byteCount);
                    } catch (RuntimeException e) {
                        throw new IOException("Failed to decompress GZIP stream safely", e);
                    }
                }
            };
            BufferedSource bufferedSource = Okio.buffer(safeSource);
            MediaType contentType = body.contentType();
            return response.newBuilder()
                    .headers(response.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build())
                    .body(ResponseBody.create(bufferedSource, contentType, -1L))
                    .build();
        }
        return response;
    }
}


