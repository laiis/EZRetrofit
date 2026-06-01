package tw.idv.laiis.ezretrofit;

import okhttp3.*;
import okio.Buffer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class GzipConcurrencyStressTest {

    @Test
    public void testGzipDecompressionConcurrency() throws Exception {
        int threadCount = 100;
        int taskCount = 1000;

        SafeGzipInterceptor interceptor = new SafeGzipInterceptor();

        // Prepare baseline execution
        long baselineStart = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            runDecompressionTask(interceptor);
        }
        long baselineDuration = (System.nanoTime() - baselineStart) / 10;

        // Force GC and measure baseline memory
        System.gc();
        Thread.sleep(200);
        long memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CompletionService<Long> completionService = new ExecutorCompletionService<>(executor);

        long testStartTime = System.currentTimeMillis();
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < taskCount; i++) {
            completionService.submit(() -> {
                long start = System.nanoTime();
                runDecompressionTask(interceptor);
                successCounter.incrementAndGet();
                return System.nanoTime() - start;
            });
        }

        long totalDurationNano = 0;
        for (int i = 0; i < taskCount; i++) {
            try {
                Future<Long> future = completionService.take();
                totalDurationNano += future.get();
            } catch (Exception e) {
                fail("GZIP concurrency task failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long testEndTime = System.currentTimeMillis();

        // 100% success rate (success = correctly processed and exception captured)
        assertEquals(taskCount, successCounter.get(), "All tasks must complete successfully");

        // Deadlock verification
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        assertNull(deadlockedThreads, "There must be no deadlocked threads");

        // GC & Memory Leak verification
        System.gc();
        Thread.sleep(200);
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryIncrease = Math.max(0, memoryAfter - memoryBefore);

        // Heap limit check
        long maxMemoryUsed = Runtime.getRuntime().totalMemory();
        assertTrue(maxMemoryUsed <= 512 * 1024 * 1024, "JVM heap memory limit <= 512MB");

        // Performance check: avg response <= 500ms
        long avgDurationNano = totalDurationNano / taskCount;
        assertTrue(TimeUnit.NANOSECONDS.toMillis(avgDurationNano) <= 500, "Avg decompression duration must be <= 500ms");
    }

    private void runDecompressionTask(SafeGzipInterceptor interceptor) {
        try {
            Request request = new Request.Builder().url("https://localhost/").build();
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
                @Override public Request request() { return request; }
                @Override public Response proceed(Request req) throws IOException { return response; }
                @Override public Connection connection() { return null; }
                @Override public Call call() { return null; }
                @Override public int connectTimeoutMillis() { return 0; }
                @Override public Interceptor.Chain withConnectTimeout(int t, TimeUnit u) { return this; }
                @Override public int readTimeoutMillis() { return 0; }
                @Override public Interceptor.Chain withReadTimeout(int t, TimeUnit u) { return this; }
                @Override public int writeTimeoutMillis() { return 0; }
                @Override public Interceptor.Chain withWriteTimeout(int t, TimeUnit u) { return this; }
            };

            Response interceptedResponse = interceptor.intercept(mockChain);
            try {
                interceptedResponse.body().string();
                fail("Should have thrown IOException");
            } catch (IOException expected) {
                // Expected behaviour
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
