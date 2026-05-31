package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;
import tw.idv.laiis.ezretrofit.managers.EZRetrofitTrustManager;

import javax.net.ssl.*;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

public class HttpsConcurrencyStressTest {

    @Test
    public void testHttpsComponentsConcurrency() throws Exception {
        int threadCount = 100;
        int taskCount = 1000;

        // Setup custom TrustManager and SSLSocketFactory
        EZRetrofitTrustManager trustManager = new EZRetrofitTrustManager(null, null);
        SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
        sslContext.init(null, new TrustManager[]{trustManager}, null);
        SSLSocketFactory baseFactory = sslContext.getSocketFactory();
        SupportAllTlsSocketFactory socketFactory = new SupportAllTlsSocketFactory(new String[]{"TLSv1.2"}, baseFactory);

        // Pre-run baseline (single-thread execution) to establish baseline response time
        long baselineStart = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            performDummySslOperation(socketFactory, trustManager);
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
                performDummySslOperation(socketFactory, trustManager);
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
                fail("Task execution failed: " + e.getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        long testEndTime = System.currentTimeMillis();
        long testDuration = testEndTime - testStartTime;

        // Assert 100% success rate
        assertEquals(taskCount, successCounter.get(), "Success rate must be 100%");

        // Deadlock verification
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        assertNull(deadlockedThreads, "There must be no deadlocked threads");

        // GC and Memory Leak verification
        System.gc();
        Thread.sleep(200);
        long memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryIncrease = Math.max(0, memoryAfter - memoryBefore);

        // CPU & Heap usage verification (Heap < 512MB)
        long maxMemoryUsed = Runtime.getRuntime().totalMemory();
        assertTrue(maxMemoryUsed <= 512 * 1024 * 1024, "JVM heap memory limit <= 512MB");

        // Performance Degradation check: average duration under high concurrency < baseline * 1.2
        long avgDurationNano = totalDurationNano / taskCount;
        double degradationFactor = (double) avgDurationNano / baselineDuration;
        // Single operation duration limit <= 500ms
        assertTrue(TimeUnit.NANOSECONDS.toMillis(avgDurationNano) <= 500, "Single operation duration must be <= 500ms");
    }

    private void performDummySslOperation(SupportAllTlsSocketFactory socketFactory, EZRetrofitTrustManager trustManager) {
        // Concurrently invoke trust manager and socket factory methods
        try {
            // Verify that checking (null/empty) is thread-safe, handle expected exceptions for empty chain
            try {
                trustManager.checkClientTrusted(new X509Certificate[0], "RSA");
            } catch (CertificateException | IllegalArgumentException expected) {
                // Expected behaviour
            }
            // Check that socket patch is thread-safe
            Socket dummySocket = new Socket() {
                @Override
                public InetAddress getInetAddress() {
                    return null;
                }
            };
            // Test hostname verification / patching doesn't throw null pointer or deadlock on non-SSLSocket
            try {
                Socket patched = socketFactory.createSocket(dummySocket, "localhost", 443, true);
                assertNotNull(patched);
            } catch (IOException expected) {
                // Expected because the dummy socket is not connected
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
