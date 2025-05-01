package com.edu.journal.location;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;


/**
 * 定位接口
 */

public interface LocationCallback {
    /**
     * 接收定位
     * @param aMapLocation 定位数据
     */
    void onLocationChanged(AMapLocation aMapLocation);

}
