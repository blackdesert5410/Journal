package com.edu.journal.viewmodel;

import androidx.lifecycle.MutableLiveData;

import com.edu.journal.db.bean.Province;
import com.edu.journal.repository.CityRepository;
import com.edu.mylibrary.base.BaseViewModel;

import java.util.List;

public class SplashViewModel extends BaseViewModel {

    public MutableLiveData<List<Province>> listMutableLiveData = new MutableLiveData<>();

    /**
     * 添加城市数据
     */
    public void addCityData(List<Province> provinceList) {
        CityRepository.getInstance().addCityData(provinceList);
    }

    /**
     * 获取所有城市数据
     */
    public void getAllCityData() {
        CityRepository.getInstance().getCityData(listMutableLiveData);
    }
}
