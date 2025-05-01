package com.edu.mylibrary.base;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class BaseViewModel extends ViewModel {
    protected MutableLiveData<String> failed = new MutableLiveData<>();

}
