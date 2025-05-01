package com.edu.journal;

import android.Manifest;
import android.content.pm.PackageManager;
import android.util.Log;


import android.os.Bundle;
import android.os.Build;
import android.content.Context;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import pub.devrel.easypermissions.EasyPermissions;
import pub.devrel.easypermissions.AfterPermissionGranted;

import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap;
import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;

import com.amap.api.location.AMapLocation;
import com.amap.api.maps.model.MyLocationStyle;

import com.edu.journal.databinding.LocationLayoutBinding;
import com.edu.journal.utils.MVUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class map extends AppCompatActivity implements AMapLocationListener, LocationSource {
    private LocationLayoutBinding binding;
    private TextView tv_location = null;
    private MapView mapView = null;
    //地图控制器
    private AMap aMap = null;
    //位置更改监听
    private OnLocationChangedListener mListener;

    //请求权限码
    private static final int REQUEST_PERMISSIONS = 9527;
    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
    public AMapLocationClientOption mLocationOption = null;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.location_layout);
        // 使用 ViewBinding 代替 setContentView
        binding = LocationLayoutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Context context = this;
        //暂时确认
        //定位隐私政策同意
        AMapLocationClient.updatePrivacyShow(context,true,true);
        AMapLocationClient.updatePrivacyAgree(context,true);
        //地图隐私政策同意
        MapsInitializer.updatePrivacyShow(context,true,true);
        MapsInitializer.updatePrivacyAgree(context,true);
        //搜索隐私政策同意
        ServiceSettings.updatePrivacyShow(context,true,true);
        ServiceSettings.updatePrivacyAgree(context,true);

//        MapView mapView = findViewById(R.id.map);
        mapView = binding.map;
        mapView.onCreate(savedInstanceState);
        initMap(savedInstanceState);
        initLocation();
        checkingAndroidVersion();

    }
    /**
     * 检查Android版本
     */
    private void checkingAndroidVersion() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            //Android6.0及以上先获取权限再定位
            requestPermission();
        }else {
            //Android6.0以下直接定位
            mLocationClient.startLocation();
        }
    }
    /**
     * 动态请求权限
     */
    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private void requestPermission() {
        // 检查是否有被拒绝的权限记录
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                
                // 检查定位权限是否在被拒绝的权限列表中
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || 
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION) ||
                        permission.equals(Manifest.permission.READ_PHONE_STATE) ||
                        permission.equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        permissionsToRequest.add(permission);
                    }
                }
                
                // 如果需要请求权限，则请求
                if (!permissionsToRequest.isEmpty()) {
                    Log.i("初始化", "请求被拒绝的权限: " + permissionsToRequest);
                    EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, 
                        permissionsToRequest.toArray(new String[0]));
                    return;
                }
            } catch (JSONException e) {
                Log.e("初始化", "解析被拒绝的权限列表失败", e);
            }
        }
        
        // 如果没有被拒绝的权限记录，检查当前权限状态
        String[] permissions = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };

        if (EasyPermissions.hasPermissions(this, permissions)) {
            //true 有权限 开始定位
            showMsg("已获得权限，可以定位啦！");
            mLocationClient.startLocation();
        } else {
            //false 无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, permissions);
        }
    }
    /**
     * 请求权限结果
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        
        // 更新被拒绝的权限列表
        updateDeniedPermissions(permissions, grantResults);
    }
    
    /**
     * 更新被拒绝的权限列表
     */
    private void updateDeniedPermissions(String[] permissions, int[] grantResults) {
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        List<String> remainingPermissions = new ArrayList<>();
        
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                
                // 检查权限是否在被拒绝的权限列表中
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    boolean stillDenied = true;
                    
                    // 检查当前权限请求结果
                    for (int j = 0; j < permissions.length; j++) {
                        if (permission.equals(permissions[j]) && grantResults[j] == PackageManager.PERMISSION_GRANTED) {
                            stillDenied = false;
                            break;
                        }
                    }
                    
                    // 如果权限仍然被拒绝，则添加到剩余权限列表中
                    if (stillDenied) {
                        remainingPermissions.add(permission);
                    }
                }
                
                // 更新被拒绝的权限列表
                JSONArray newJsonArray = new JSONArray();
                for (String permission : remainingPermissions) {
                    newJsonArray.put(permission);
                }
                MVUtils.put(Constant.DENIED_PERMISSIONS, newJsonArray.toString());
            } catch (JSONException e) {
                Log.e("初始化", "更新被拒绝的权限列表失败", e);
            }
        }
    }
    /**
     * Toast提示
     * @param msg 提示内容
     */
    private void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }
    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
        try {
            mLocationClient = new AMapLocationClient(getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mLocationClient != null) {
            //设置定位回调监听
            mLocationClient.setLocationListener(this);
            //初始化AMapLocationClientOption对象
            mLocationOption = new AMapLocationClientOption();
            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            //获取最近3s内精度最高的一次定位结果：
            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
            mLocationOption.setOnceLocationLatest(true);
            //设置是否返回地址信息（默认返回地址信息）
            mLocationOption.setNeedAddress(true);
            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
            mLocationOption.setHttpTimeOut(20000);
            //关闭缓存机制，高精度定位会产生缓存。
            mLocationOption.setLocationCacheEnable(false);
            //给定位客户端对象设置定位参数
            mLocationClient.setLocationOption(mLocationOption);
        }
    }
    /**
     * 接收异步返回的定位结果
     *
     * @param aMapLocation
     */
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //地址
                StringBuilder stringBuilder = new StringBuilder();
                String address = aMapLocation.getAddress() + '\n' + aMapLocation.getCountry();
                stringBuilder.append(address + '\n');
//                stringBuilder.append(aMapLocation.getGpsAccuracyStatus() + '\n');
//                stringBuilder.append(aMapLocation.getLocationDetail() + '\n');
                stringBuilder.append("纬度：" + aMapLocation.getLatitude() + '\n');
                stringBuilder.append("经度：" + aMapLocation.getLongitude() + '\n');

                System.out.println(address);
//                tv_location.setText(address == null ? "无地址信息" : address);
                tv_location = binding.tvLocation;
                tv_location.setText(address == null ? "无地址信息" : stringBuilder);
                //定位停止后，本地定位不被销毁
                mLocationClient.stopLocation();
                if(mListener != null){
                    mListener.onLocationChanged(aMapLocation);
                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }
    /**
     * 初始化地图
     * @param savedInstanceState
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = binding.map;
        //在activity执行onCreate时执行mMapView.onCreate(savedInstanceState)，创建地图
        mapView.onCreate(savedInstanceState);
        //初始化地图控制器对象
        aMap = mapView.getMap();

        // 设置定位监听
        aMap.setLocationSource(this);
        //设置最小缩放等级为16 ，缩放级别范围为[3, 20]
        aMap.setMinZoomLevel(16);

        // 设置为true表示显示定位层并可触发定位，false表示隐藏定位层并不可触发定位，默认是false
        aMap.setMyLocationEnabled(true);
    }
    /**
     * 激活定位
     */
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
        if (mLocationClient != null) {
            mLocationClient.startLocation();//启动定位
        }
    }

    /**
     * 停止定位
     */
    @Override
    public void deactivate() {
        mListener = null;
        if (mLocationClient != null) {
            mLocationClient.stopLocation();
            mLocationClient.onDestroy();
        }
        mLocationClient = null;
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mapView.onDestroy()，销毁地图
        mapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mapView.onResume ()，重新绘制加载地图
        mapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mapView.onPause ()，暂停地图的绘制
        mapView.onPause();
    }
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
    }

}
