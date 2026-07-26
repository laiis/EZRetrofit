package tw.idv.laiis.ezretrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by laiis on 2017/4/27.
 */

public abstract class EZCallback<T> implements Callback<T> {

    private String mTag;
    private final Runnable mDequeueHandler;

    public EZCallback() {
        this(null, null);
    }

    public EZCallback(String tag) {
        this(tag, null);
    }

    public EZCallback(String tag, Runnable dequeueHandler) {
        this.mTag = tag;
        this.mDequeueHandler = dequeueHandler;
    }

    public String getTag() {
        return mTag;
    }

    protected void handleDequeue() {
        if (mDequeueHandler != null) {
            mDequeueHandler.run();
        } else {
            CallManager.newInstance().dequeue(mTag);
        }
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        handleDequeue();
        if (!response.isSuccessful()) {
            fail(call, response);
            return;
        }

        success(call, response);
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        handleDequeue();
        exception(call, t);
    }

    public abstract void success(Call<T> call, Response<T> response);

    public abstract void fail(Call<T> call, Response<T> response);

    public abstract void exception(Call<T> call, Throwable t);
}

