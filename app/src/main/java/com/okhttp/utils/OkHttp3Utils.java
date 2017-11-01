package com.okhttp.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.okhttp.AppLoader;
import com.okhttp.interfac.CacheInterceptor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author: xiaxueyi
 * @date: 2017-11-01
 * @time: 13:06
 * @说明: okHttp使用的工具类
 */
public class OkHttp3Utils {

    private static final String TAG="OkHttp3Utils";

    private static OkHttp3Utils mInstance=null;

    private static OkHttpClient mOkHttpClient=null; //链接实例

    private static Handler mHandler = null;


    /**
     * 获取OkHttp3Utils单例
     * @return
     */
    public static OkHttp3Utils getInstance(){
        if(mInstance==null){
            synchronized (OkHttp3Utils.class){  //线程安全
                mInstance=new OkHttp3Utils();
            }
        }
        return mInstance;
    }


    /**
     * 获取OkHttpClient单例
     * @return
     */
    public static OkHttpClient getOkHttpClient(){
        if(mOkHttpClient==null){
            mOkHttpClient=new OkHttpClient();
            //设置缓存目录
            File sdcach=new File(Environment.getExternalStorageDirectory(),"cache");
            //设置缓存大小
            int cachSize=10*1024*1024;
            //设置Log 拦截器
            HttpLoggingInterceptor httpLoggingInterceptor=new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                @Override
                public void log(String message) {
                    Log.i(TAG,message);
                }
            });

            //设置OkHttp log 的等级
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

            //实现链接方式
            mOkHttpClient=new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
                    .addInterceptor(httpLoggingInterceptor) //添加日志拦截器
                    .addNetworkInterceptor(new CacheInterceptor())
                    .writeTimeout(20,TimeUnit.SECONDS)  //写入超时时间
                    .readTimeout(20,TimeUnit.SECONDS)   //读取网络超时时间
                    .cache(new Cache(sdcach,cachSize))
                    .build();
        }

        return mOkHttpClient;
    }


    /**
     * 获取Handler 对象
     * @return
     */
    public synchronized static Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }



    /**
     * get请求
     * 参数1 url
     * 参数2 回调Callback
     */

    public static void doGet(String url, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getOkHttpClient();
        //创建Request
        Request request = new Request.Builder().url(url).build();
        //得到Call对象
        Call call = okHttpClient.newCall(request);
        //执行异步请求
        call.enqueue(callback);


    }

    /**
     * post请求
     * 参数1 url
     * 参数2 回调Callback
     */

    public static void doPost(String url, Map<String, String> params, Callback callback) {

        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getOkHttpClient();
        //3.x版本post请求换成FormBody 封装键值对参数

        FormBody.Builder builder = new FormBody.Builder();
        //遍历集合
        for (String key : params.keySet()) {
            builder.add(key, params.get(key));

        }


        //创建Request
        Request request = new Request.Builder().url(url).post(builder.build()).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);

    }

    /**
     * post请求上传文件
     * 参数1 url
     * 参数2 回调Callback
     */
    public static void uploadPic(String url, File file, String fileName) {
        //创建OkHttpClient请求对象
        OkHttpClient okHttpClient = getOkHttpClient();
        //创建RequestBody 封装file参数
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        //创建RequestBody 设置类型等
        RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM).addFormDataPart("file", fileName, fileBody).build();
        //创建Request
        Request request = new Request.Builder().url(url).post(requestBody).build();

        //得到Call
        Call call = okHttpClient.newCall(request);
        //执行请求
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //上传成功回调 目前不需要处理
            }
        });

    }

    /**
     * Post请求发送JSON数据
     * 参数一：请求Url
     * 参数二：请求的JSON
     * 参数三：请求回调
     */
    public static void doPostJson(String url, String jsonParams, Callback callback) {
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), jsonParams);
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(callback);


    }

    /**
     * 下载文件 以流的形式把apk写入的指定文件 得到file后进行安装
     * 参数一：请求Url
     * 参数二：保存文件的路径名
     * 参数三：保存文件的文件名
     */
    public static void download(final Context context, final String url, final String saveDir) {
        Request request = new Request.Builder().url(url).build();
        Call call = getOkHttpClient().newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.i("xxx", e.toString());
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {

                InputStream is = null;
                byte[] buf = new byte[2048];
                int len = 0;
                FileOutputStream fos = null;
                try {
                    is = response.body().byteStream();
                    //apk保存路径
                    final String fileDir = isExistDir(saveDir);
                    //文件
                    File file = new File(fileDir, getNameFromUrl(url));
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    //apk下载完成后 调用系统的安装方法
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    AppLoader.getInstance().startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (is != null) is.close();
                    if (fos != null) fos.close();


                }
            }
        });

    }

    /**
     * @param saveDir
     * @return
     * @throws IOException 判断下载目录是否存在
     */
    public static String isExistDir(String saveDir) throws IOException {
        // 下载位置
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            File downloadFile = new File(Environment.getExternalStorageDirectory(), saveDir);
            if (!downloadFile.mkdirs()) {
                downloadFile.createNewFile();
            }
            String savePath = downloadFile.getAbsolutePath();
            Log.e("savePath", savePath);
            return savePath;
        }
        return null;
    }

    /**
     * @param url
     * @return 从下载连接中解析出文件名
     */
    private static String getNameFromUrl(String url) {
        return url.substring(url.lastIndexOf("/") + 1);
    }
}
