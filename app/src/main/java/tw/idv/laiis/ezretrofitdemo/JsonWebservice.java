package tw.idv.laiis.ezretrofitdemo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Created by laiis on 2017/4/27.
 */

public interface JsonWebservice {

    @GET("{END_POINT}")
    Call<ResponseBody> getTestData(@Path(value = "END_POINT", encoded = true) String path, @Query("scope") String scope, @Query("q") String q);
}
