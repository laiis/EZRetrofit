package tw.idv.laiis.ezretrofitdemo;

import retrofit2.Call;
import retrofit2.Response;
import tw.idv.laiis.ezretrofit.EZCallback;

/**
 * Created by laiis on 2017/4/30.
 */

public abstract class IDCallback extends EZCallback {

    protected long mId;

    public IDCallback(long id) {
        this.mId = id;
    }

    @Override
    public final void success(Call call, Response response) {
        success(mId, call, response);
    }

    @Override
    public final void fail(Call call, Response response) {
        fail(mId, call, response);
    }

    @Override
    public final void exception(Call call, Throwable t) {
        exception(mId, call, t);
    }

    public void success(long id, Call call, Response response) {

    }

    public void fail(long id, Call call, Response response) {

    }

    public void exception(long id, Call call, Throwable t) {

    }
}
