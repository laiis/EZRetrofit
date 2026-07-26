package tw.idv.laiis.ezretrofit;

import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Response;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

public class EZCallbackTest {

    @Test
    public void testInjectedDequeueHandlerOnResponse() {
        AtomicBoolean dequeueCalled = new AtomicBoolean(false);
        Runnable customDequeue = () -> dequeueCalled.set(true);

        EZCallback<String> callback = new EZCallback<String>("test_tag", customDequeue) {
            @Override
            public void success(Call<String> call, Response<String> response) {}
            @Override
            public void fail(Call<String> call, Response<String> response) {}
            @Override
            public void exception(Call<String> call, Throwable t) {}
        };

        Response<String> successResponse = Response.success("OK");
        callback.onResponse(null, successResponse);

        assertTrue(dequeueCalled.get(), "Injected dequeue handler should be invoked onResponse");
    }

    @Test
    public void testInjectedDequeueHandlerOnFailure() {
        AtomicBoolean dequeueCalled = new AtomicBoolean(false);
        Runnable customDequeue = () -> dequeueCalled.set(true);

        EZCallback<String> callback = new EZCallback<String>("test_tag", customDequeue) {
            @Override
            public void success(Call<String> call, Response<String> response) {}
            @Override
            public void fail(Call<String> call, Response<String> response) {}
            @Override
            public void exception(Call<String> call, Throwable t) {}
        };

        callback.onFailure(null, new RuntimeException("Test Exception"));

        assertTrue(dequeueCalled.get(), "Injected dequeue handler should be invoked onFailure");
    }

    @Test
    public void testHelperCacheIsolation() {
        EZRetrofitHelper<Object> helper1 = EZRetrofitHelper.newInstance();
        EZRetrofitHelper<Object> helper2 = EZRetrofitHelper.newInstance();

        assertNotSame(helper1, helper2, "EZRetrofitHelper instances should be distinct and isolated");
    }
}
