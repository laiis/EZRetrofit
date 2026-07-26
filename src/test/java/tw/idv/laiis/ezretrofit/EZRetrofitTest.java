package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitTest {

    interface TestServiceA {}
    interface TestServiceB {}

    @BeforeEach
    public void setUp() {
        RetrofitConf conf = new RetrofitConf.Builder()
                .baseUrls(TestServiceA.class, "https://service-a.com/")
                .baseUrls(TestServiceB.class, "https://service-b.com/")
                .build();
        EZRetrofit.initial(conf);
    }

    @Test
    public void testFacadeLifecycleMethods() {
        assertEquals(0, EZRetrofit.count());
        assertEquals(0, EZRetrofit.count("tag1"));

        assertDoesNotThrow(() -> EZRetrofit.stop("tag1"));
        assertDoesNotThrow(EZRetrofit::stopAll);

        assertDoesNotThrow(() -> EZRetrofit.setLogger(new EZLogger() {
            @Override
            public void warn(String tag, String message, Throwable t) {}
        }));

        EZRetrofitHelper<TestServiceA> helper = EZRetrofit.create();
        assertNotNull(helper);

        RetrofitConf conf2 = new RetrofitConf.Builder()
                .baseUrls(TestServiceA.class, "https://service-a2.com/")
                .build();
        EZRetrofitHelper<TestServiceA> helper2 = EZRetrofit.create(conf2);
        assertNotNull(helper2);
    }

    @Test
    public void testConcurrentCreate() throws InterruptedException, ExecutionException {
        int threadCount = 10;
        int iterations = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CompletionService<Boolean> completionService = new ExecutorCompletionService<>(executor);

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < iterations; i++) {
            final int index = i;
            completionService.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() {
                    try {
                        if (index % 2 == 0) {
                            TestServiceA service = EZRetrofit.create(TestServiceA.class);
                            assertNotNull(service);
                        } else {
                            TestServiceB service = EZRetrofit.create(TestServiceB.class);
                            assertNotNull(service);
                        }
                        return true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return false;
                    }
                }
            });
        }

        for (int i = 0; i < iterations; i++) {
            Future<Boolean> future = completionService.take();
            assertTrue(future.get(), "Retrofit service creation failed in thread");
        }

        executor.shutdown();
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 5000, "Concurrent stress test took too long: " + duration + "ms");
    }
}
