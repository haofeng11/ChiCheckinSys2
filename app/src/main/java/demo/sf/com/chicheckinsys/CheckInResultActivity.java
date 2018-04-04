package demo.sf.com.chicheckinsys;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Vincent Feng(I331077) on 04/03/2018.
 */

public class CheckInResultActivity extends AppCompatActivity {

    String url;
    private TextView textView;

    public static void launchActivity(Activity activity, double latitude, double longitude, boolean ableToCheckIn) {
        Intent intent = new Intent();
        intent.putExtra("latitude", latitude);
        intent.putExtra("longitude", longitude);
        intent.putExtra("ableToCheckIn", ableToCheckIn);
        intent.setClass(activity, CheckInResultActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_result);

        textView = findViewById(R.id.textView);
        double latitude = getIntent().getDoubleExtra("latitude", 0.0);
        double longitude = getIntent().getDoubleExtra("longitude", 0.0);
        boolean ableToCheckIn = getIntent().getBooleanExtra("ableToCheckIn", false);

        if (ableToCheckIn) {
            textView.setText("CheckIn SuccessFully!");
        } else {
            textView.setText("You are far away from company!");
        }

        url = "http://api.map.baidu.com/geoconv/v1/?coords=" + String.valueOf(longitude) + "," + String.valueOf(latitude) +
                "&from=5&to=3&ak=pXfFFt04Sxk6x3og739d2IK2ifkiRHEl&mcode=3C:CA:5F:58:28:9A:BD:9F:B3:17:6D:7D:A6:B1:F3:57:B8:84:3B:3D;demo.sf.com.chicheckinsys";
        final OkHttpClient httpClient = new OkHttpClient();
        final Request request = new Request.Builder()
                .url(url).build();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Response response = null;
                    try {
                        response = httpClient.newCall(request).execute();
                        String responseStr = response.body().string();
                        Log.i("Converted location", responseStr);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }
}
