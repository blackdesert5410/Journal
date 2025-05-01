package com.edu.mylibrary.network.interceptor;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;

import com.edu.mylibrary.network.INetworkRequiredInfo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

//在网络数据交互的时候有请求和返回，那么在两个过程中我们可以获取一些信息，就需要拦截器
public class RequestInterceptor implements Interceptor {
    private final INetworkRequiredInfo iNetworkRequiredInfo;
    public RequestInterceptor(INetworkRequiredInfo iNetworkRequiredInfo){
        this.iNetworkRequiredInfo = iNetworkRequiredInfo;
    }

    /**
     * 拦截
     */
//    @NonNull 是 声明非空约束 的注解，帮助减少 NullPointerException
    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        //构建器
        Request.Builder builder = chain.request().newBuilder();
        //添加使用环境
        builder.addHeader("os","android");
        //添加版本号
        builder.addHeader("appVersionCode",this.iNetworkRequiredInfo.getAppVersionCode());
        //添加版本名
        builder.addHeader("appVersionName",this.iNetworkRequiredInfo.getAppVersionName());
        //添加日期
        builder.addHeader("datatime",getNowDateTime());
        //返回

        return chain.proceed(builder.build());

    }
    public static String getNowDateTime(){
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }
}
