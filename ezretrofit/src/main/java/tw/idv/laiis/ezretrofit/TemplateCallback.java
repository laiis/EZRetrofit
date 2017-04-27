package tw.idv.laiis.ezretrofit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by laiis on 2017/4/27.
 */

public abstract class TemplateCallback<T> implements Callback<T> {

    @Override
    public final void onResponse(Call<T> call, Response<T> response) {
        if (!response.isSuccessful()) {
            fail(call, response);
            return;
        }

        success(call, response);
    }

    @Override
    public final void onFailure(Call<T> call, Throwable t) {
        exception(call, t);
    }

    public abstract void success(Call<T> call, Response<T> response);

    public abstract void fail(Call<T> call, Response<T> response);

    public abstract void exception(Call<T> call, Throwable t);
}
