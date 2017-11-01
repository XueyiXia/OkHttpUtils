package com.okhttp.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.okhttp.R;
import com.okhttp.interfac.GsonObjectCallback;
import com.okhttp.utils.OkHttp3Utils;

import java.io.IOException;
import java.util.Objects;

import okhttp3.Call;

public class MainActivity extends AppCompatActivity {

    private TextView mContent;

    String url="http://api.tianapi.com/social/?key=71e58b5b2f930eaf1f937407acde08fe&num=20";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContent=(TextView)super.findViewById(R.id.content);

        getData();
    }


    private void getData(){
        OkHttp3Utils.getInstance().doGet(url, new GsonObjectCallback<Objects>() {
            @Override
            public void onUiThread(Objects objects,String json) {
                mContent.setText(json);
            }

            @Override
            public void onFailed(Call call, IOException exception) {

            }
        }) ;
    }
}
