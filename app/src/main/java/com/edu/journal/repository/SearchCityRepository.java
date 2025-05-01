package com.edu.journal.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.edu.journal.ApiService;
import com.edu.journal.db.bean.SearchCityResponse;
import com.edu.journal.Constant;
import com.edu.mylibrary.network.ApiType;
import com.edu.mylibrary.network.NetworkApi;
import com.edu.mylibrary.network.observe.BaseObserver;

@SuppressLint("CheckResult")
public class SearchCityRepository {

    private static final class SearchCityRepositoryHolder{
        private static final SearchCityRepository mInstance = new SearchCityRepository();
    }

    public static SearchCityRepository getInstance() {
        return SearchCityRepository.SearchCityRepositoryHolder.mInstance;
    }

    private static final String TAG = SearchCityRepository.class.getSimpleName();
    public void searchCity(MutableLiveData<SearchCityResponse> responseLiveData,
                           MutableLiveData<String> failed, String cityName, boolean isExact) {

        NetworkApi.createService(ApiService.class, ApiType.SEARCH).searchCity(cityName, isExact ? Constant.EXACT : Constant.FUZZY)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(SearchCityResponse searchCityResponse) {
                        if (searchCityResponse == null) {
                            failed.postValue("搜索城市数据为null，请检查城市名称是否正确。");
                            return;
                        }
                        //请求接口成功返回数据，失败返回状态码
                        if (Constant.SUCCESS.equals(searchCityResponse.getCode())) {
                            responseLiveData.postValue(searchCityResponse);
                        } else {
                            failed.postValue(searchCityResponse.getCode());
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        failed.postValue(e.getMessage());
                    }
                }));
    }
    public void searchCity(MutableLiveData<SearchCityResponse> responseLiveData,
                           MutableLiveData<String> failed, String areaName, String cityName, boolean isExact) {

        NetworkApi.createService(ApiService.class, ApiType.SEARCH).searchCity(areaName,cityName, isExact ? Constant.EXACT : Constant.FUZZY)
                .compose(NetworkApi.applySchedulers(new BaseObserver<>() {
                    @Override
                    public void onSuccess(SearchCityResponse searchCityResponse) {
                        if (searchCityResponse == null) {
                            failed.postValue("搜索城市数据为null，请检查城市名称是否正确。");
                            return;
                        }
                        //请求接口成功返回数据，失败返回状态码
                        if (Constant.SUCCESS.equals(searchCityResponse.getCode())) {
                            responseLiveData.postValue(searchCityResponse);
                        } else {
                            failed.postValue(searchCityResponse.getCode());
                        }
                    }

                    @Override
                    public void onFailure(Throwable e) {
                        Log.e(TAG, "onFailure: " + e.getMessage());
                        failed.postValue(e.getMessage());
                    }
                }));
    }

}
