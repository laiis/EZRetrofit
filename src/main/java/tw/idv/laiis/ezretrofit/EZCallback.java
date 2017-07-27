package tw.idv.laiis.ezretrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by laiis on 2017/4/27.
 */

public abstract class EZCallback<T> implements Callback<T> {

    private String mTag;

    public EZCallback(String tag) {
        this.mTag = tag;
    }

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        CallManager.newInstance().dequeue(mTag);
        if (!response.isSuccessful()) {
            fail(call, response);
            return;
        }

        success(call, response);
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        CallManager.newInstance().dequeue(mTag);
        exception(call, t);
    }

    public abstract void success(Call<T> call, Response<T> response);

    public abstract void fail(Call<T> call, Response<T> response);

    public abstract void exception(Call<T> call, Throwable t);
}
