package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;
import okhttp3.Request;
import okio.Timeout;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class CallManagerConcurrencyTest {

    @Test
    public void testHighConcurrencyEnqueueAndDequeue() throws InterruptedException {
        final CallManager manager = CallManager.newInstance();
        final int threadCount = 100;
        final int requestsPerThread = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean hasError = new AtomicBoolean(false);
        final AtomicInteger successCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    String presenter = "Presenter_" + (threadId % 10);
                    for (int j = 0; j < requestsPerThread; j++) {
                        String tag = "Tag_" + threadId + "_" + j;
                        Call<Object> dummyCall = new DummyCall();
                        EZCallback<Object> callback = new EZCallback<Object>(tag) {
                            @Override public void success(Call<Object> call, Response<Object> response) {}
                            @Override public void fail(Call<Object> call, Response<Object> response) {}
                            @Override public void exception(Call<Object> call, Throwable t) {}
                        };

                        manager.enqueue(dummyCall, callback);
                        manager.requestAmount(presenter);
                        manager.isRequestEmpty(presenter);
                        manager.dequeue(tag);
                        successCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    hasError.set(true);
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        long elapsedTime = System.currentTimeMillis() - startTime;
        System.out.println("Concurrent execution time for " + (threadCount * requestsPerThread) + " ops: " + elapsedTime + " ms");

        assertTrue(completed, "All threads should complete within time limit");
        assertFalse(hasError.get(), "No exceptions should occur during concurrent operations");
        assertEquals(threadCount * requestsPerThread, successCount.get());
    }

    @Test
    public void testConcurrentCancelAndCancelAll() throws InterruptedException {
        final CallManager manager = CallManager.newInstance();
        final int threadCount = 50;
        final ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        final CountDownLatch latch = new CountDownLatch(threadCount);
        final AtomicBoolean hasError = new AtomicBoolean(false);

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < 20; j++) {
                        String tag = "CancelTag_" + threadId + "_" + j;
                        Call<Object> dummyCall = new DummyCall();
                        EZCallback<Object> callback = new EZCallback<Object>(tag) {
                            @Override public void success(Call<Object> call, Response<Object> response) {}
                            @Override public void fail(Call<Object> call, Response<Object> response) {}
                            @Override public void exception(Call<Object> call, Throwable t) {}
                        };

                        manager.enqueue(dummyCall, callback);
                        if (j % 2 == 0) {
                            manager.cancel(tag);
                        }
                    }
                } catch (Exception e) {
                    hasError.set(true);
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        manager.cancelAll();
        executor.shutdown();

        assertFalse(hasError.get(), "Concurrent cancel operations must not throw exceptions");
        assertTrue(manager.isRequestEmpty(), "CallManager should be empty after cancelAll()");
    }

    private static class DummyCall implements Call<Object> {
        private boolean cancelled = false;

        @Override public Response<Object> execute() throws IOException { return null; }
        @Override public void enqueue(Callback<Object> callback) {}
        @Override public boolean isExecuted() { return false; }
        @Override public void cancel() { cancelled = true; }
        @Override public boolean isCanceled() { return cancelled; }
        @Override public Call<Object> clone() { return this; }
        @Override public Request request() { return null; }
        public Timeout timeout() { return Timeout.NONE; }
    }
}
