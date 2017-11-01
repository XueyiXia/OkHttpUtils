package com.okhttp.interfac;

import com.okhttp.AppLoader;
import com.okhttp.utils.NetWorkUtils;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * @author: xiaxueyi
 * @date: 2017-11-01
 * @time: 13:28
 * @说明: 为okhttp添加缓存，这里是考虑到服务器不支持缓存时，从而让okhttp支持缓存
 */
public class CacheInterceptor implements Interceptor {


    public CacheInterceptor(){

    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        //有网络的时候，设置缓存超时的时间
        int maxAge=60*60;   //3600秒为一个小时
        //没有网络，设置超时为1天
        int maxStale=60*60*24;
        //获取请求的对象
        Request request=chain.request();

        if(NetWorkUtils.isNetWorkAvailable(AppLoader.getInstance())){   //有网络的时候，从网络获取
            request=request.newBuilder().cacheControl(CacheControl.FORCE_NETWORK).build();
        }else{
            //没有网络，从缓存中获取
            request=request.newBuilder().cacheControl(CacheControl.FORCE_CACHE).build();
        }

        //获取返回的数据
        Response response=chain.proceed(request);
        if(NetWorkUtils.isNetWorkAvailable(AppLoader.getInstance())){
            response = response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, max-age=" + maxAge)
                    .build();
        }else{
            response = response.newBuilder()
                    .removeHeader("Pragma")
                    .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                    .build();
        }
        return response;
    }
}
