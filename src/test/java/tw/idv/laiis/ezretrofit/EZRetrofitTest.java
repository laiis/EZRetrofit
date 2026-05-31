package tw.idv.laiis.ezretrofit;

import org.junit.Before;
import org.junit.Test;
import java.util.concurrent.*;
import static org.junit.Assert.*;

public class EZRetrofitTest {

    interface TestServiceA {}
    interface TestServiceB {}

    @Before
    public void setUp() {
        RetrofitConf conf = new RetrofitConf.Builder()
                .baseUrls(TestServiceA.class, "https://service-a.com/")
                .baseUrls(TestServiceB.class, "https://service-b.com/")
                .build();
        EZRetrofit.initial(conf);
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
            assertTrue("Retrofit service creation failed in thread", future.get());
        }

        executor.shutdown();
        long duration = System.currentTimeMillis() - startTime;
        assertTrue("Concurrent stress test took too long: " + duration + "ms", duration < 5000);
    }
}
