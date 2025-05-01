package com.edu.journal.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.maps.MapsInitializer;
import com.amap.api.services.core.ServiceSettings;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;


import com.edu.journal.Constant;
import com.edu.journal.R;
import com.edu.journal.databinding.DialogDailyDetailBinding;
import com.edu.journal.db.bean.HourlyResponse;
import com.edu.journal.location.GoodLocation;
import com.edu.journal.location.LocationCallback;
import com.edu.journal.ui.adapter.HourlyAdapter;
import com.edu.journal.ui.adapter.LifestyleAdapter;
import com.edu.journal.db.bean.DailyResponse;
import com.edu.journal.db.bean.LifestyleResponse;
import com.edu.journal.db.bean.NowResponse;
import com.edu.journal.db.bean.SearchCityResponse;
import com.edu.journal.ui.adapter.DailyAdapter;
import com.edu.journal.databinding.ActivityMainBinding;
import com.edu.journal.map;
import com.edu.journal.utils.CityDialog;
import com.edu.journal.utils.EasyDate;
import com.edu.journal.utils.MVUtils;
import com.edu.journal.utils.WeatherUtil;
import com.edu.journal.viewmodel.MainViewModel;
import com.edu.mylibrary.base.NetworkActivity;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


//import com.edu.journal.Utils.JwtUtils;

//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.view.View;
//import android.widget.TextView;
//import android.widget.Toast;

//import androidx.activity.EdgeToEdge;
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;


//import java.io.IOException;
//import java.nio.charset.StandardCharsets;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;


//import okhttp3.Call;
//import okhttp3.Callback;
//import okhttp3.OkHttpClient;
//import okhttp3.Request;
//import okhttp3.Response;
//import pub.devrel.easypermissions.AfterPermissionGranted;
//import pub.devrel.easypermissions.EasyPermissions;


public class MainActivity extends NetworkActivity<ActivityMainBinding> implements CityDialog.SelectedCityCallback, LocationCallback {

    private Menu mMenu;

    private String mCityName;

    private String mAreaName;

    private boolean isRefresh;

    private int cityFlag = 0;
    private int scrollFlag = 0;

    private GoodLocation goodLocation;

    //城市弹窗
    private CityDialog cityDialog;

    private final List<DailyResponse.DailyBean> dailyBeanList = new ArrayList<>();
    private final DailyAdapter dailyAdapter = new DailyAdapter(dailyBeanList);

    private final List<LifestyleResponse.DailyBean> lifestyleList = new ArrayList<>();
    private final LifestyleAdapter lifestyleAdapter = new LifestyleAdapter(lifestyleList);

    private final List<HourlyResponse.HourlyBean> hourlyBeanList = new ArrayList<>();
    private final HourlyAdapter hourlyAdapter = new HourlyAdapter(hourlyBeanList);


    private TextView tvlocation;
    private Button button;
    private MainViewModel viewModel;
    private Intent intent;
    //请求权限码

    private final String[] permissions = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    //请求权限意图
    private ActivityResultLauncher<String[]> requestPermissionIntent;
    //声明AMapLocationClient类对象
//    public AMapLocationClient mLocationClient = null;
    //声明AMapLocationClientOption对象
//    public AMapLocationClientOption mLocationOption = null;

    @Override
    public void selectedCity(String cityName,String areaName) {
        cityFlag = 1;//切换城市
        mAreaName = areaName;//切换城市赋值
        mCityName = cityName;
        //搜索城市
        viewModel.searchCity(areaName,true);//city对应area
//        viewModel.searchCity(areaName,mCityName,true);
        //显示所选城市
        binding.tvCity.setText(cityName+" "+areaName);
        if(scrollFlag == 1)
            binding.tvTitle.setText((mCityName == null ? "城市天气" : mCityName+" "+mAreaName));
    }



    public void setToolbarMoreIconCustom(Toolbar toolbar) {
        if (toolbar == null) return;
        toolbar.setTitle("");
        Drawable moreIcon = ContextCompat.getDrawable(toolbar.getContext(), R.drawable.ic_round_add_32);
        if (moreIcon != null) toolbar.setOverflowIcon(moreIcon);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        //根据cityFlag设置重新定位菜单项是否显示
        mMenu.findItem(R.id.item_relocation).setVisible(cityFlag == 1);
        return true;
    }


    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId()==R.id.item_switching_cities){
            if (cityDialog != null) {
                showMsg("go go go!选择你的城市");
                cityDialog.show();
            } else {
                showMsg("城市数据正在加载中，请稍候...");
            }
        }
        if(item.getItemId()==R.id.item_relocation){
            startlocate();
        }
        if (item.getItemId()==R.id.item_music){
            intent = new Intent();
            intent.setClass(MainActivity.this, MusicActivity.class);
            startActivity(intent);
        }

        if(item.getItemId()==R.id.item_schedule){
            intent = new Intent();
            intent.setClass(MainActivity.this, CalendarActivity.class);
            startActivity(intent);
        }

        if(item.getItemId()==R.id.item_settings){
            intent = new Intent();
            intent.setClass(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        }

        if(Integer.valueOf(item.getItemId())==null){
            Log.e("错误","onOptionsItemSelected出错了");
        }
        return true;
    }
    
    //
    private void showDailyDetailDialog(DailyResponse.DailyBean dailyBean) {
        BottomSheetDialog dialog = new BottomSheetDialog(MainActivity.this);
        DialogDailyDetailBinding detailBinding = DialogDailyDetailBinding.inflate(LayoutInflater.from(MainActivity.this), null, false);
        //关闭弹窗
        detailBinding.ivClose.setOnClickListener(v -> dialog.dismiss());
        //设置数据显示
        detailBinding.toolbarDaily.setTitle(String.format("%s   %s", dailyBean.getFxDate(), EasyDate.getWeek(dailyBean.getFxDate())));
        detailBinding.toolbarDaily.setSubtitle("天气预报详情");
        detailBinding.tvTmpMax.setText(String.format("%s℃", dailyBean.getTempMax()));
        detailBinding.tvTmpMin.setText(String.format("%s℃", dailyBean.getTempMin()));
        detailBinding.tvUvIndex.setText(dailyBean.getUvIndex());
        detailBinding.tvCondTxtD.setText(dailyBean.getTextDay());
        detailBinding.tvCondTxtN.setText(dailyBean.getTextNight());
        detailBinding.tvWindDeg.setText(String.format("%s°", dailyBean.getWind360Day()));
        detailBinding.tvWindDir.setText(dailyBean.getWindDirDay());
        detailBinding.tvWindSc.setText(String.format("%s级", dailyBean.getWindScaleDay()));
        detailBinding.tvWindSpd.setText(String.format("%s公里/小时", dailyBean.getWindSpeedDay()));
        detailBinding.tvCloud.setText(String.format("%s%%", dailyBean.getCloud()));
        detailBinding.tvHum.setText(String.format("%s%%", dailyBean.getHumidity()));
        detailBinding.tvPres.setText(String.format("%shPa", dailyBean.getPressure()));
        detailBinding.tvPcpn.setText(String.format("%smm", dailyBean.getPrecip()));
        detailBinding.tvVis.setText(String.format("%skm", dailyBean.getVis()));
        dialog.setContentView(detailBinding.getRoot());
        dialog.show();
    }

    //初始化视图

    private void initView(){
        setToolbarMoreIconCustom(binding.materialToolbar);
        binding.rvDaily.setLayoutManager(new LinearLayoutManager(this));
//        dailyAdapter.setOnClickItemCallback(new OnClickItemCallback() {
//            @Override
//            public void onItemClick(int position) {
//
//            }
//        });
        dailyAdapter.setOnClickItemCallback(position -> showDailyDetailDialog(dailyBeanList.get(position)));

        binding.rvDaily.setAdapter(dailyAdapter);
        binding.rvLifestyle.setLayoutManager(new LinearLayoutManager(this));
        binding.rvLifestyle.setAdapter(lifestyleAdapter);
        //下拉刷新监听
        binding.layRefresh.setOnRefreshListener(() -> {
            if (mAreaName == null) {
                binding.layRefresh.setRefreshing(false);
                return;
            }
            //设置正在刷新
            isRefresh = true;
            startlocate();
            //搜索城市
            viewModel.searchCity(mAreaName,true);
//            viewModel.searchCity(mAreaName,mCityName,true);
        });
        //滑动监听
        binding.layScroll.setOnScrollChangeListener((View.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            if (scrollY > oldScrollY) {
                //getMeasuredHeight() 表示控件的绘制高度
                if (scrollY > binding.layScrollHeight.getMeasuredHeight()) {
                    scrollFlag = 1;
                    binding.tvTitle.setText((mCityName == null ? "城市天气" : mCityName+" "+mAreaName));
                }
            } else if (scrollY < oldScrollY) {
                if (scrollY < binding.layScrollHeight.getMeasuredHeight()) {
                    //改回原来的
                    scrollFlag = 0;
                    binding.tvTitle.setText("城市天气");
                }
            }
        });
        LinearLayoutManager hourlyLayoutManager = new LinearLayoutManager(this);
        hourlyLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        binding.rvHourly.setLayoutManager(hourlyLayoutManager);
        binding.rvHourly.setAdapter(hourlyAdapter);
    }

    /**
     * 注册意图
     */
    @Override
    protected void onRegister() {
        requestPermissionIntent = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),result ->{
            boolean fineLocation = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
            boolean writeStorage = Boolean.TRUE.equals(result.get(Manifest.permission.WRITE_EXTERNAL_STORAGE));
            
            // 更新被拒绝的权限列表
            updateDeniedPermissions(result);
            
            if (fineLocation && writeStorage) {
                //权限已经获取到，开始定位
                startlocate();
                Log.i("初始化","定位初始化");
            }
        });
    }
    
    /**
     * 更新被拒绝的权限列表
     */
    private void updateDeniedPermissions(java.util.Map<String, Boolean> result) {
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        List<String> remainingPermissions = new ArrayList<>();
        
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                
                // 检查权限是否在被拒绝的权限列表中
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    // 如果权限已授予，则不添加到剩余权限列表中
                    if (!Boolean.TRUE.equals(result.get(permission))) {
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
     * 初始化
     */
    @Override
    protected void onCreate() {
//        setFullScreenImmersion();
        initLocation();
//        checkingAndroidVersion();
        requestPermission();
        initView();
        viewModel = new ViewModelProvider(this).get(MainViewModel.class);
        viewModel.getAllCity();

        button = binding.button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent = new Intent();
                intent.setClass(MainActivity.this, map.class);
                startActivity(intent);
//                cityDialog.show();
            }
        });

//        if(cityDialog != null){
//            relocate();
//        }

    }

    /**
     * 数据观察
     */
    @Override
    protected void onObserveData() {
        if(viewModel != null){
            viewModel.searchCityResponseMutableLiveData.observe(this,searchCityResponse -> {
                List<SearchCityResponse.LocationBean> location = searchCityResponse.getLocation();
                if (location!=null&&location.size()>0){
                    String id = location.get(0).getId();//只捕捉首个结果
                    //重新设置定位按钮是否显示
                    if(mMenu !=null)
                    mMenu.findItem(R.id.item_relocation).setVisible(cityFlag ==1);
                    Log.d("TAG","城市ID："+id);
                    if(isRefresh){
                        showMsg("刷新完成");
                        binding.layRefresh.setRefreshing(false);
                        isRefresh = false;
                    }
                    if(id!= null){
                        viewModel.nowWeather(id);
                        viewModel.dailyWeather(id);
                        viewModel.lifestyle(id);
                        viewModel.hourlyWeather(id);
                    }
                }
            });

            viewModel.nowResponseMutableLiveData.observe(this,nowResponse -> {
                NowResponse.NowBean now = nowResponse.getNow();
                if(now!= null){
                    binding.tvInfo.setText(now.getText());
                    WeatherUtil.changeIcon(binding.ivStatus, Integer.parseInt(now.getIcon()));
                    binding.tvTemp.setText(now.getTemp());
                    binding.tvUpdateTime.setText("最近更新时间:  "+ EasyDate.greenwichupToSimpleTime(nowResponse.getUpdateTime()));
                    binding.tvWindDirection.setText("风向     " + now.getWindDir());//风向
                    binding.tvWindPower.setText("风力     " + now.getWindScale() + "级");//风力
                    binding.wwBig.startRotate();//大风车开始转动
                    binding.wwSmall.startRotate();//小风车开始转动

                }
            });

            viewModel.dailyResponseMutableLiveData.observe(this,dailyResponse -> {
                List<DailyResponse.DailyBean> daily = dailyResponse.getDaily();
                if(daily != null){
                    if(dailyBeanList.size()>0){
                            dailyBeanList.clear();
                    }
                    dailyBeanList.addAll(daily);
                    dailyAdapter.notifyDataSetChanged();
                    binding.tvHeight.setText(String.format("%s℃", daily.get(0).getTempMax()));
                    binding.tvLow.setText(String.format(" / %s℃", daily.get(0).getTempMin()));

                }
            });

            viewModel.lifestyleResponseMutableLiveData.observe(this, lifestyleResponse -> {
                List<LifestyleResponse.DailyBean> daily = lifestyleResponse.getDaily();
                if (daily != null) {
                    if (lifestyleList.size() > 0) {
                        lifestyleList.clear();
                    }
                    lifestyleList.addAll(daily);
                    lifestyleAdapter.notifyDataSetChanged();
                }
            });

            //获取本地城市数据返回
            viewModel.cityMutableLiveData.observe(this, provinces -> {
                //城市弹窗初始化
                Log.e("弹窗","初始化弹窗啊");
                cityDialog = CityDialog.getInstance(MainActivity.this, provinces);
                cityDialog.setSelectedCityCallback(this);
            });
            //小时天气预报
            viewModel.hourlyResponseMutableLiveData.observe(this, hourlyResponse -> {
                List<HourlyResponse.HourlyBean> hourly = hourlyResponse.getHourly();
                if (hourly != null) {
                    if (hourlyBeanList.size() > 0) {
                        hourlyBeanList.clear();
                    }
                    hourlyBeanList.addAll(hourly);
                    hourlyAdapter.notifyDataSetChanged();
                }
            });

            viewModel.failed.observe(this,this::showLongMsg);


        }
    }
    /**
     * 请求权限
     */
    private void requestPermission(){
        // 检查是否有被拒绝的权限记录
        String deniedPermissionsJson = MVUtils.getString(Constant.DENIED_PERMISSIONS, "");
        List<String> permissionsToRequest = new ArrayList<>();
        
        if (!deniedPermissionsJson.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(deniedPermissionsJson);
                
                // 检查定位权限是否在被拒绝的权限列表中
                boolean needLocationPermission = false;
                for (int i = 0; i < jsonArray.length(); i++) {
                    String permission = jsonArray.getString(i);
                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || 
                        permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        needLocationPermission = true;
                        permissionsToRequest.add(permission);
                    }
                }
                
                // 如果需要请求权限，则请求
                if (!permissionsToRequest.isEmpty()) {
                    Log.i("初始化", "请求被拒绝的权限: " + permissionsToRequest);
                    requestPermissionIntent.launch(permissionsToRequest.toArray(new String[0]));
                    return;
                }
            } catch (JSONException e) {
                Log.e("初始化", "解析被拒绝的权限列表失败", e);
            }
        }
        
        // 如果没有被拒绝的权限记录，检查当前权限状态
        if(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED
        || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
            //开始权限请求
            requestPermissionIntent.launch(permissions);
            return;
        }
        
        Log.i("初始化","已有所有权限，开始定位");
        startlocate();
    }

    /**
     * 初始化定位
     */
    private void initLocation() {
        //初始化定位
//        try {
//            mLocationClient = new AMapLocationClient(getApplicationContext());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        if (mLocationClient != null) {
//            //设置定位回调监听
//            mLocationClient.setLocationListener(this);
//            //初始化AMapLocationClientOption对象
//            mLocationOption = new AMapLocationClientOption();
//            //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
//            mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
//            //获取最近3s内精度最高的一次定位结果：
//            //设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
//            mLocationOption.setOnceLocationLatest(true);
//            //设置是否返回地址信息（默认返回地址信息）
//            mLocationOption.setNeedAddress(true);
//            //设置定位请求超时时间，单位是毫秒，默认30000毫秒，建议超时时间不要低于8000毫秒。
//            mLocationOption.setHttpTimeOut(20000);
//            //关闭缓存机制，高精度定位会产生缓存。
//            mLocationOption.setLocationCacheEnable(false);
//            //给定位客户端对象设置定位参数
//            mLocationClient.setLocationOption(mLocationOption);
//            Log.i("初始化","定位参数设置初始化");
//        }
        goodLocation = GoodLocation.getInstance(this);
        goodLocation.setCallback(this);
    }

    /**
     * 开始定位
     */
    private void startlocate() {
//        Context context = this;
//        //定位隐私政策同意
//        AMapLocationClient.updatePrivacyShow(context, true, true);
//        AMapLocationClient.updatePrivacyAgree(context, true);
//        //地图隐私政策同意
//        MapsInitializer.updatePrivacyShow(context, true, true);
//        MapsInitializer.updatePrivacyAgree(context, true);
//        //搜索隐私政策同意
//        ServiceSettings.updatePrivacyShow(context, true, true);
//        ServiceSettings.updatePrivacyAgree(context, true);
//        if (mLocationClient != null) {
//            Log.e("运行","开始定位");
//            mLocationClient.startLocation();
//        }
        cityFlag =0;
        goodLocation.startlocate(this);
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
                double latitude = aMapLocation.getLatitude();
                double longitude = aMapLocation.getLongitude();
                //地址
                String address = aMapLocation.getAddress();
                String district = aMapLocation.getDistrict();//获取区县
                String coordType = aMapLocation.getCoordType();//获取经纬度坐标类型
                int errorCode = aMapLocation.getLocationType();//161  表示网络定位结果
                String country = aMapLocation.getCountry();    //获取国家
                String province = aMapLocation.getProvince();    //获取省份
                String city = aMapLocation.getCity();    //获取城市
                String street = aMapLocation.getStreet();    //获取街道信息
                String locationDescribe = aMapLocation.getDescription();    //获取位置描述信息

//                tvlocation = binding.tvLocation;
//                tvlocation.setText(address == null ? "无定位" : address);
                binding.tvLocation.setText(address == null ? "无定位":address);
                //只定位一次

                if (viewModel != null && district != null) {
                    mCityName = city;//定位后赋值
                    mAreaName = district;
                    //显示当前定位城市
//                    binding.tvCity.setText(city + " " + district);
                    binding.tvCity.setText(mCityName + " " + mAreaName);
                    //搜索城市
                    viewModel.searchCity(district, true);
//                    viewModel.searchCity(district,city,true);
                    Log.i("运行","开始回调定位");
                } else {
                    Log.e("TAG", "district" + district);
                }

            } else {
                //定位失败时，可通过ErrCode（错误码）信息来确定失败的原因，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
            }
        }
    }

    //手动重新定位
    private void relocate(){
        initLocation();
        requestPermission();
        onRegister();
        onObserveData();
//        mLocationClient.stopLocation();
    }

}