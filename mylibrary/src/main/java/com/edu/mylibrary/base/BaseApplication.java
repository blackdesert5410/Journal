package com.edu.mylibrary.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;

public class BaseApplication extends Application {
    private static ActivityManager activityManager;
    @SuppressLint("StaticFieldLeak")
    private static BaseApplication application;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        activityManager = new ActivityManager();
        context = getApplicationContext();
        application = this;
        Log.i("初始化","baseapplication初始化成功");
    }

    public static ActivityManager getActivityManager(){
        return activityManager;
    }
    //内容提供
    public static Context getContext(){
        return context;
    }

//    public static BaseApplication getApplication(){
//        return application;
//    }//getApplication() AppCompatActivity中有该方法冲突
    public static BaseApplication getApplication(){
        return application;
    }


}
