package tw.idv.laiis.ezretrofit.client;

import okhttp3.Request;
import okio.Timeout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import tw.idv.laiis.ezretrofit.EZCallback;
import tw.idv.laiis.ezretrofit.EZRetrofitHelper;
import tw.idv.laiis.ezretrofit.config.EZRetrofitConfig;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class EZRetrofitClientTest {

    interface DummyService {}

    private EZRetrofitConfig config;

    @BeforeEach
    public void setUp() {
        config = new EZRetrofitConfig.Builder()
                .addBaseUrlService(DummyService.class, "https://dummy-api.com/")
                .build();
        EZRetrofitConfig.initial(config);
    }

    @Test
    public void testClientInstantiationAndCreate() {
        EZRetrofitClient client = new EZRetrofitClient(config);
        DummyService service = client.create(DummyService.class);
        assertNotNull(service);

        EZRetrofitClient defaultClient = new EZRetrofitClient();
        DummyService defaultService = defaultClient.create(DummyService.class);
        assertNotNull(defaultService);
    }

    @Test
    public void testCreateHelper() {
        EZRetrofitClient client = new EZRetrofitClient(config);
        EZRetrofitHelper<DummyService> helper = client.createHelper();
        assertNotNull(helper);
        
        DummyService service = helper.webservice(DummyService.class);
        assertNotNull(service);
    }

    @Test
    public void testLifecycleCall() {
        // 驗證呼叫，因為 CallManager 會使用它。
        // 我們測試 count 與 stop 相關功能是否被轉發。
        // 這裡因為沒有真正的網路 Call，我們傳入 Dummy Call，檢查是否會拋出異常或正常工作。
        // 為了避免 CallManager 的空指針異常或真的去 enqueue，我們在 CallManagerTest 中已有驗證，
        // 這裡只要確認 EZRetrofitLifecycle 能成功呼叫到 checkInitialStatus 即可。
        int currentCount = EZRetrofitLifecycle.count();
        assertEquals(0, currentCount);

        EZRetrofitLifecycle.stopAll();
        EZRetrofitLifecycle.stop("dummyTag");
        assertEquals(0, EZRetrofitLifecycle.count("dummyTag"));
    }
}
