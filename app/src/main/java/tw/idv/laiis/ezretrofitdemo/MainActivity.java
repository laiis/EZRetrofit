package tw.idv.laiis.ezretrofitdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.JsonWebservice;
import tw.idv.laiis.ezretrofit.TemplateCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        EZRetrofit.count(this, ((JsonWebservice) (EZRetrofit.create(JsonWebservice.class) // build EZRetrofitHelper
                .callback(new TemplateCallback<ResponseBody>() {
                    @Override
                    public void success(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String tag = "";
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(response.body().byteStream()));
                            String str = "";
                            StringBuffer sb = new StringBuffer();
                            while ((str = br.readLine()) != null) {
                                sb.append(str);
                            }

                            tag = sb.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(MainActivity.this, "success: " + tag, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void fail(Call<ResponseBody> call, Response<ResponseBody> response) {
                        String tag = "";
                        try {
                            BufferedReader br = new BufferedReader(new InputStreamReader(response.errorBody().byteStream()));
                            String str = "";
                            StringBuffer sb = new StringBuffer();
                            while ((str = br.readLine()) != null) {
                                sb.append(str);
                            }

                            tag = sb.toString();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        Toast.makeText(MainActivity.this, "fail: " + response.message() + " , " + response.toString(), Toast.LENGTH_SHORT).show();
                        Log.d("tag", "--->" + response.toString());
                    }

                    @Override
                    public void exception(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(MainActivity.this, "exception: ", Toast.LENGTH_SHORT).show();
                    }
                })))
                .getTestData("opendata/datalist/apiAccess", "datasetMetadataSearch", "臺北市文化快遞資訊"));

    }

    @Override
    protected void onStop() {
        super.onStop();
        EZRetrofit.stop(this);
    }
}
