package com.edu.journal;

import android.app.Application;

import com.edu.mylibrary.network.INetworkRequiredInfo;
public class NetworkRequiredInfo implements INetworkRequiredInfo {
    private final Application application;

    public NetworkRequiredInfo(Application application){
        this.application = application;
    }
    /**
     * 版本名
     */
    public String getAppVersionName(){
//        return "";
        return String.valueOf(BuildConfig.VERSION_NAME);
    }
    /**
     * 版本号
     */
    @Override
    public String getAppVersionCode() {
//        return"";
            return String.valueOf(BuildConfig.VERSION_CODE);
    }

    /**
     * 是否为debug
     */
    @Override
    public boolean isDebug() {
//        return false;
        return BuildConfig.DEBUG;
    }

    /**
     * 应用全局上下文
     */
    @Override
    public Application getApplicationContext() {
        return application;
    }

}
