package com.okhttp;

import android.app.Application;

/**
 * @author: xiaxueyi
 * @date: 2017-11-01
 * @time: 13:36
 * @说明:
 */
public class AppLoader extends Application{

    private static AppLoader mInstance=null;

    public static AppLoader getInstance(){
        return mInstance;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        mInstance=this;
    }
}
