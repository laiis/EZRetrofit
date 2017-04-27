package tw.idv.laiis.ezretrofit.managers;

import android.util.Log;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;


/**
 * Created by laiis on 2017/4/25.
 */
public class CallManager {

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

    public void enqueue(Call call, Callback callback) {
        enqueue(null,call,callback);
    }

    public void enqueue(String tag, Call call, Callback callback) {
        synchronized (CallManager.class) {

            if (mCallMap.get(tag) == null) {
                mCallMap.put(tag, call);
                if (!mCounterMap.containsKey(tag)) {
                    mCounterMap.put(tag, new RequestCounter());
                }

                mCounterMap.get(tag).increase();
            }

            call.enqueue(callback);

            showCallInMap();
        }
    }

    public void dequeue(String presenterName, String tag) {
        synchronized (CallManager.class) {

            mCallMap.remove(tag);

            if (mCounterMap.containsKey(presenterName)) {
                mCounterMap.get(presenterName).decrease();

                if (mCounterMap.get(presenterName).isZero()) {
                    mCounterMap.remove(presenterName);
                }
            }


            showCallInMap();
        }
    }

    public void cancel(String presenterName, String tag) {
        synchronized (CallManager.class) {
            if (mCallMap.get(tag) != null) {
                mCallMap.get(tag).cancel();
                dequeue(presenterName, tag);
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

        Log.d(TAG, " ---> " + sb.toString());
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
