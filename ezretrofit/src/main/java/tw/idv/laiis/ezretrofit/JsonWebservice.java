package tw.idv.laiis.ezretrofit;

import retrofit2.Call;

/**
 * Created by laiis on 2017/4/27.
 */

public interface JsonWebservice {

    <T> Call<T> getTestData();
}
