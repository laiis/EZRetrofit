package tw.idv.laiis.ezretrofit;

import retrofit2.Call;
import retrofit2.Callback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by laiis on 2017/4/25.
 */
public final class CallManager {

    private static final String TAG = CallManager.class.getName();

    private final Map<String, Call> mCallMap;
    private final Map<String, RequestCounter> mCounterMap;

    private static class InnerHelper {
        public static volatile CallManager sCallManager = new CallManager();
    }

    public static CallManager newInstance() {
        return InnerHelper.sCallManager;
    }

    private CallManager() {
        mCallMap = new ConcurrentHashMap<>();
        mCounterMap = new ConcurrentHashMap<>();
    }

    public void enqueue(Call call, EZCallback callback) {
        if (callback != null) {
            String tag = callback.getTag();
            if (tag != null && !tag.isEmpty()) {
                if (mCallMap.putIfAbsent(tag, call) == null) {
                    mCounterMap.computeIfAbsent(tag, k -> new RequestCounter()).increase();
                }
            }
        }

        if (call != null) {
            call.enqueue(callback);
        }

        showCallInMap();
    }

    public void dequeue(String tag) {
        if (tag != null) {
            mCallMap.remove(tag);
            mCounterMap.computeIfPresent(tag, (k, counter) -> {
                counter.decrease();
                return counter.isZero() ? null : counter;
            });
        }

        showCallInMap();
    }

    public void cancel(String tag) {
        if (tag != null) {
            Call call = mCallMap.remove(tag);
            if (call != null) {
                call.cancel();
            }
            mCounterMap.computeIfPresent(tag, (k, counter) -> {
                counter.decrease();
                return counter.isZero() ? null : counter;
            });
        }
    }

    public void cancelAll() {
        for (Call call : mCallMap.values()) {
            if (call != null) {
                call.cancel();
            }
        }

        mCallMap.clear();
        mCounterMap.clear();
    }

    private void showCallInMap() {
        if (LibConfig.IS_DEBUG) {
            StringBuffer sb = new StringBuffer();
            sb.append("[ isRequestEmpty ] CallManager tag size: ");
            sb.append(String.valueOf(mCallMap.size()));
            for (String tag : mCallMap.keySet()) {
                sb.append("\ntag :\t" + tag);
            }
            System.out.println(sb.toString());
        }
    }

    public int requestAmount() {
        return mCallMap.size();
    }

    public int requestAmount(String presenterName) {
        if (presenterName != null) {
            RequestCounter counter = mCounterMap.get(presenterName);
            if (counter != null) {
                return counter.getReqCount();
            }
        }
        return 0;
    }

    public boolean isRequestEmpty() {
        showCallInMap();
        return mCallMap.isEmpty();
    }

    public boolean isRequestEmpty(String presenterName) {
        showCallInMap();
        return !mCounterMap.containsKey(presenterName);
    }

    private static final class RequestCounter {
        private final AtomicInteger reqCount = new AtomicInteger(0);

        public void increase() {
            reqCount.incrementAndGet();
        }

        public void decrease() {
            reqCount.decrementAndGet();
        }

        public int getReqCount() {
            return reqCount.get();
        }

        public void zero() {
            reqCount.set(0);
        }

        public boolean isZero() {
            return reqCount.get() <= 0;
        }
    }
}

