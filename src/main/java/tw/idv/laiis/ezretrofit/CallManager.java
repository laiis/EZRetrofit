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

    private static final boolean IS_IN_DEBUG = false;
    private static final String TAG = CallManager.class.getName();

    private Map<String, Call> mCallMap;
    private Map<String, RequestCounter> mCounterMap;

    private static class InnerHelper {
        public static volatile CallManager sCallManager = new CallManager();
    }

    public static CallManager newInstance() {
        return InnerHelper.sCallManager;
    }

    private CallManager() {
        mCallMap = Collections.synchronizedMap(new HashMap<String, Call>());
        mCounterMap = Collections.synchronizedMap(new HashMap<String, RequestCounter>());
    }

    public void enqueue(String tag, Call call, Callback callback) {
        synchronized (this) {

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
        synchronized (this) {

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
        synchronized (this) {
            if (mCallMap.get(tag) != null) {
                mCallMap.get(tag).cancel();
                dequeue(tag);
            }
        }
    }

    public void cancelAll() {
        synchronized (this) {
            for (Call call : mCallMap.values()) {
                call.cancel();
            }

            mCallMap.clear();
            mCounterMap.clear();

        }
    }

    private void showCallInMap() {
        if (IS_IN_DEBUG) {
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
        synchronized (this) {
            return mCallMap.size();
        }
    }

    public int requestAmount(String presenterName) {
        synchronized (this) {
            if (mCounterMap.get(presenterName) != null) {
                return mCounterMap.get(presenterName).getReqCount();
            }
            return 0;
        }
    }

    public boolean isRequestEmpty() {
        synchronized (this) {
            showCallInMap();
            return mCallMap.isEmpty();
        }
    }

    public boolean isRequestEmpty(String presenterName) {
        synchronized (this) {
            showCallInMap();
            return !mCounterMap.containsKey(presenterName);
        }
    }

    private static final class RequestCounter {

        private int reqCount = 0;

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
