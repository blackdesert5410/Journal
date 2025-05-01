package com.edu.journal.location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.AfterPermissionGranted;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;

public class GoodLocation {

    private static volatile GoodLocation mInstance;

    @SuppressLint("StaticFieldLeak")
    private static AMapLocationClient mLocationClient = null;

    private GoodLocationListener goodLocationListener;

    private static LocationCallback callback;



    public GoodLocation(Context context) {
        initLocation(context);
    }

    //返回单例
    public static GoodLocation getInstance(Context context) {
        if (mInstance == null) {
            synchronized (GoodLocation.class) {
                if (mInstance == null) {
                    mInstance = new GoodLocation(context);
                }
            }
        }
        return mInstance;
    }
    private void initLocation(Context context) {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(context);
            goodLocationListener = new  GoodLocationListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            //设置定位回调监听
            mLocationClient.setLocationListener(goodLocationListener);
            //初始化AMapLocationClientOption对象
            AMapLocationClientOption option = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            option.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            option.setNeedAddress(true);
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            option.setHttpTimeOut(20000);
            //关闭缓存机制，高精度定位会产生缓存。
            option.setLocationCacheEnable(false);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(option);
            Log.i("初始化","定位参数设置初始化");
        }
    }

    /**
     * 需要定位的页面调用此方法进行接口回调处理
     */
    public void setCallback(LocationCallback callback) {
        GoodLocation.callback = callback;
    }

    public void startlocate(Context context) {
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(context, true, true);
        AMapLocationClient.updatePrivacyAgree(context, true);
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(context, true, true);
        MapsInitializer.updatePrivacyAgree(context, true);
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(context, true, true);
        ServiceSettings.updatePrivacyAgree(context, true);
        if (mLocationClient != null) {
            Log.e("运行","开始定位");
            mLocationClient.startLocation();
        }
    }
    /**
     * 请求定位
     */
    private static void requestLocation() {
        if (mLocationClient != null) {
            mLocationClient.startLocation();
        }
    }

    /**
     * 停止定位
     */
    private static void stopLocate() {
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
        }
    }

    /**
     * 内部类实现定位结果接收
     */
    public static class GoodLocationListener implements AMapLocationListener {
        private final String TAG = GoodLocationListener.class.getSimpleName();

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation == null) return;
            if(aMapLocation.getDistrict() == null){
                Log.e(TAG,"goodlocation 未获取区县数据");
                requestLocation();
            }
            stopLocate();
            if(callback == null){
                Log.e(TAG,"callback is null");
                return;
            }
            callback.onLocationChanged(aMapLocation);
        }

    }
    


}
