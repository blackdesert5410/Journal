package com.edu.journal.viewmodel;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.edu.journal.db.bean.DailyResponse;
import com.edu.journal.db.bean.HourlyResponse;
import com.edu.journal.db.bean.LifestyleResponse;
import com.edu.journal.db.bean.NowResponse;
import com.edu.journal.db.bean.Province;
import com.edu.journal.db.bean.SearchCityResponse;
import com.edu.journal.repository.CityRepository;
import com.edu.journal.repository.SearchCityRepository;
import com.edu.journal.repository.WeatherRepository;
import com.edu.mylibrary.base.BaseViewModel;

import java.util.List;

public class MainViewModel extends BaseViewModel {

//    private WeatherRepository weatherRepository =  WeatherRepository.getInstance();

    public MutableLiveData<SearchCityResponse> searchCityResponseMutableLiveData = new MutableLiveData<>();
    /**
     * 搜索成功
     * @param cityName 城市名称
     * @param isExact 是否精准搜索
     */
    public void searchCity(String cityName, boolean isExact) {
        Log.i("运行","正在搜索");
        SearchCityRepository.getInstance().searchCity(searchCityResponseMutableLiveData, failed, cityName, isExact);
    }
    public void searchCity(String areaName, String cityName, boolean isExact) {
        Log.i("运行","正在搜索");
        SearchCityRepository.getInstance().searchCity(searchCityResponseMutableLiveData, failed , areaName , cityName, isExact);
    }



    public MutableLiveData<NowResponse> nowResponseMutableLiveData = new MutableLiveData<>();

    public void nowWeather(String cityId) {
        WeatherRepository.getInstance().nowWeather(nowResponseMutableLiveData,failed, cityId);
    }





    public MutableLiveData<DailyResponse> dailyResponseMutableLiveData = new MutableLiveData<>();

    public void dailyWeather(String cityId) {
        WeatherRepository.getInstance().dailyWeather(dailyResponseMutableLiveData, failed, cityId);
    }

    public MutableLiveData<LifestyleResponse> lifestyleResponseMutableLiveData = new MutableLiveData<>();

    public void lifestyle(String cityId) {
        WeatherRepository.getInstance().lifestyle(lifestyleResponseMutableLiveData, failed, cityId);
    }


    public MutableLiveData<List<Province>> cityMutableLiveData = new MutableLiveData<>();

    public void getAllCity() {
        CityRepository.getInstance().getCityData(cityMutableLiveData);
    }

    //小时预报
    public MutableLiveData<HourlyResponse> hourlyResponseMutableLiveData = new MutableLiveData<>();

    public void hourlyWeather(String cityId) {
        WeatherRepository.getInstance().hourlyWeather(hourlyResponseMutableLiveData, failed, cityId);
    }





    public MutableLiveData<String> failed = super.failed;



}
