package com.edu.journal;

import com.amap.api.location.AMapLocationClient;

import com.edu.journal.db.AppDatabase;
import com.edu.journal.utils.MVUtils;
import com.edu.mylibrary.base.BaseApplication;
import com.edu.mylibrary.network.NetworkApi;
import com.tencent.mmkv.MMKV;

public class WeatherApp extends BaseApplication {

    private static AppDatabase db;
    @Override
    public void onCreate() {
        super.onCreate();
        AMapLocationClient.updatePrivacyShow(this,true,true);
        AMapLocationClient.updatePrivacyAgree(this,true);
        //初始化网络框架
        NetworkApi.init(new NetworkRequiredInfo(this));
        //MMKV初始化
        MMKV.initialize(this);
        //工具类初始化
        MVUtils.getInstance();
        //初始化Room数据库
        db = AppDatabase.getInstance(this);
    }

    public static AppDatabase getDb(){
        return db;
    }
}
