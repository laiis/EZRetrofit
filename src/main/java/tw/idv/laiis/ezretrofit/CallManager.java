package tw.idv.laiis.ezretrofit;


import retrofit2.Call;
import retrofit2.Callback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * Created by laiis on 2017/4/25.
 */
final class CallManager {

    private static final String TAG = CallManager.class.getName();

    private static volatile CallManager sCallManager;

    private Map<String, Call> mCallMap;
    private Map<String, RequestCounter> mCounterMap;

    public static CallManager newInstance() {
        if (sCallManager == null) {
            synchronized (CallManager.class) {
                if (sCallManager == null) {
                    sCallManager = new CallManager();
                }
            }
        }
        return sCallManager;
    }

    private CallManager() {
        mCallMap = Collections.synchronizedMap(new HashMap<String, Call>());
        mCounterMap = Collections.synchronizedMap(new HashMap<String, RequestCounter>());
    }

    public void enqueue(String tag, Call call, Callback callback) {
        synchronized (CallManager.class) {

            if (!(tag == null || tag.length() == 0)) {
                if (mCallMap.get(tag) == null) {
                    mCallMap.put(tag, call);
                    if (!mCounterMap.containsKey(tag)) {
                        mCounterMap.put(tag, new RequestCounter());
                    }

                    mCounterMap.get(tag).increase();
                }
            }


            call.enqueue(callback);

            showCallInMap();
        }
    }

    public void dequeue(String tag) {
        synchronized (CallManager.class) {

            mCallMap.remove(tag);

            if (mCounterMap.containsKey(tag)) {
                mCounterMap.get(tag).decrease();

                if (mCounterMap.get(tag).isZero()) {
                    mCounterMap.remove(tag);
                }
            }


            showCallInMap();
        }
    }

    public void cancel(String tag) {
        synchronized (CallManager.class) {
            if (mCallMap.get(tag) != null) {
                mCallMap.get(tag).cancel();
                dequeue(tag);
            }
        }
    }

    public void cancelAll() {
        synchronized (CallManager.class) {
            for (Call call : mCallMap.values()) {
                call.cancel();
            }

            mCallMap.clear();
            mCounterMap.clear();
        }
    }

    private void showCallInMap() {
        StringBuffer sb = new StringBuffer();
        sb.append("[ isRequestEmpty ] CallManager tag size: ");
        sb.append(String.valueOf(mCallMap.size()));
        for (String tag : mCallMap.keySet()) {
            sb.append("\ntag :\t" + tag);
        }
    }

    public int requestAmount() {
        return mCallMap.size();
    }

    public int requestAmount(String presenterName) {
        if (mCounterMap.get(presenterName) != null) {
            return mCounterMap.get(presenterName).getReqCount();
        }
        return 0;
    }

    public boolean isRequestEmpty() {
        synchronized (CallManager.class) {
            showCallInMap();
            return mCallMap.isEmpty();
        }
    }

    public boolean isRequestEmpty(String presenterName) {
        synchronized (CallManager.class) {
            showCallInMap();
            return !mCounterMap.containsKey(presenterName);
        }
    }

    private static class RequestCounter {

        private int reqCount;

        public void increase() {
            reqCount++;
        }

        public void decrease() {
            reqCount--;
        }

        public int getReqCount() {
            return reqCount;
        }

        public void zero() {
            reqCount = 0;
        }

        public boolean isZero() {
            return reqCount == 0;
        }
    }
}
