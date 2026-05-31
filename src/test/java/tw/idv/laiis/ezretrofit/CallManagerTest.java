package tw.idv.laiis.ezretrofit;

import org.junit.Test;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import static org.junit.Assert.*;

public class CallManagerTest {

    @Test
    public void testConcurrentCancelAll() throws InterruptedException {
        final CallManager manager = CallManager.newInstance();
        final AtomicBoolean hasException = new AtomicBoolean(false);

        int threadCount = 5;
        final int iterations = 100;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount + 1);

        for (int t = 0; t < threadCount; t++) {
            final int threadId = t;
            executor.submit(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < iterations; i++) {
                        Call<Object> mockCall = new DummyCall();
                        String tag = "tag_" + threadId + "_" + i;
                        EZCallback<Object> mockCallback = new EZCallback<Object>(tag) {
                            @Override
                            public void success(Call<Object> call, Response<Object> response) {}
                            @Override
                            public void fail(Call<Object> call, Response<Object> response) {}
                            @Override
                            public void exception(Call<Object> call, Throwable t) {}
                        };
                        try {
                            manager.enqueue(mockCall, mockCallback);
                        } catch (Exception e) {
                            hasException.set(true);
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        executor.submit(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < iterations; i++) {
                    try {
                        manager.cancelAll();
                        Thread.sleep(2);
                    } catch (Exception e) {
                        hasException.set(true);
                        e.printStackTrace();
                    }
                }
            }
        });

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        assertFalse("Concurrent modification exception should not occur during concurrent cancelAll()", hasException.get());
    }

    private static class DummyCall implements Call<Object> {
        private boolean cancelled = false;

        @Override
        public Response<Object> execute() throws IOException { return null; }
        @Override
        public void enqueue(Callback<Object> callback) {}
        @Override
        public boolean isExecuted() { return false; }
        @Override
        public void cancel() { cancelled = true; }
        @Override
        public boolean isCanceled() { return cancelled; }
        @Override
        public Call<Object> clone() { return this; }
        @Override
        public Request request() { return null; }
        
        // 移除 @Override 以免與舊版 Retrofit 衝突
        public Timeout timeout() { return Timeout.NONE; }
    }
}
