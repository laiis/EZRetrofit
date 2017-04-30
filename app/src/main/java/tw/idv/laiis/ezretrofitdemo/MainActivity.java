package tw.idv.laiis.ezretrofitdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import java.util.HashMap;

import okhttp3.CertificatePinner;
import retrofit2.Call;
import retrofit2.Response;
import tw.idv.laiis.ezretrofit.EZRetrofit;
import tw.idv.laiis.ezretrofit.JsonWebservice;
import tw.idv.laiis.ezretrofit.RetrofitConf;
import tw.idv.laiis.ezretrofit.TemplateCallback;
import tw.idv.laiis.ezretrofit.XMLWebservice;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // I want to use this pattern to call api.
        EZRetrofit.initial(new RetrofitConf.Builder(this)
                .setCertificatePinner(new CertificatePinner.Builder()
                        .add("xxx", "xxx")
                        .build())
                .baseUrls(JsonWebservice.class, "https://www.google.com/")
                .baseUrls(XMLWebservice.class, "https://www.facebook.com/")
                .build());

        EZRetrofit.call(((JsonWebservice) (EZRetrofit.create(JsonWebservice.class) // build EZRetrofitHelper
                .endPoint("")
                .callback(new TemplateCallback() {

                    @Override
                    public void success(Call call, Response response) {

                    }

                    @Override
                    public void fail(Call call, Response response) {

                    }

                    @Override
                    public void exception(Call call, Throwable t) {

                    }
                })))
                .getTestData());
        ;

        EZRetrofit.count(this,
                ((JsonWebservice) (EZRetrofit.create(JsonWebservice.class) // build EZRetrofitHelper
                        .endPoint("")
                        .request(new HashMap<String, String>(), new TemplateCallback() {

                            @Override
                            public void success(Call call, Response response) {

                            }

                            @Override
                            public void fail(Call call, Response response) {

                            }

                            @Override
                            public void exception(Call call, Throwable t) {

                            }
                        })))
                        .getTestData());

        EZRetrofit.create(JsonWebservice.class) // build EZRetrofitHelper
                .endPoint(endPoint, var1, var2)
                .count(this)
                .upload(query, new TemplateCallback() {
                    @Override
                    public void success(Call call, Response response) {

                    }

                    @Override
                    public void fail(Call call, Response response) {

                    }

                    @Override
                    public void exception(Call call, Throwable t) {

                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();

        EZRetrofit.stop(this);
    }
}
