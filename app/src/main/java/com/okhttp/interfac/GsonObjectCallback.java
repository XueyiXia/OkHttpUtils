package com.okhttp.interfac;

import android.os.Handler;

import com.google.gson.Gson;
import com.okhttp.utils.OkHttp3Utils;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.lang.reflect.Type;

/**
 * @author: xiaxueyi
 * @date: 2017-11-01
 * @time: 13:48
 * @说明: 1. 类的用途 如果要将得到的json直接转化为集合 建议使用该类
 *          该类的onUi() onFailed()方法运行在主线程
 */
public abstract class GsonObjectCallback <T> implements Callback{

    private Handler handler = OkHttp3Utils.getInstance().getHandler();


    @Override
    public void onFailure(Call call, IOException e) {   //失败的时候
        onFailed(call,e);
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {       //请求json 并直接返回泛型的对象 主线程处理
        final String json = response.body().string();
        Class<T> cls = null;

        Class clz = this.getClass();
        ParameterizedType type = (ParameterizedType) clz.getGenericSuperclass();
        Type[] types = type.getActualTypeArguments();
        cls = (Class<T>) types[0];
        Gson gson = new Gson();
        final T t = gson.fromJson(json, cls);
        handler.post(new Runnable() {
            @Override
            public void run() {
                onUiThread(t,json);
            }
        });
    }


    public abstract void onUiThread(T t,String json );   //主线程处理


    public abstract void onFailed(Call call, IOException exception);    //主线程处理
}
