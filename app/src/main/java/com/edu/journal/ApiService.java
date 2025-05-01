package com.edu.journal;

import com.edu.journal.db.bean.DailyResponse;
import com.edu.journal.db.bean.HourlyResponse;
import com.edu.journal.db.bean.LifestyleResponse;
import com.edu.journal.db.bean.NowResponse;
import com.edu.journal.db.bean.SearchCityResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {
///geo/v2/city/lookup
    /**
     * 搜索城市  模糊搜索，国内范围 返回10条数据
     *
     * @param location 城市名
     * @param mode     exact 精准搜索  fuzzy 模糊搜索
     * @return NewSearchCityResponse 搜索城市数据返回
     */
    @GET("/geo/v2/city/lookup?key="+Constant.API_KEY+"&range=cn")
    Observable<SearchCityResponse> searchCity(@Query("location") String location,
                                              @Query("mode") String mode);

    @GET("/geo/v2/city/lookup?key="+Constant.API_KEY+"&range=cn")
    Observable<SearchCityResponse> searchCity(@Query("location") String location,@Query("adm") String adm,
                                              @Query("mode") String mode);
    @GET("/v7/weather/now?key=" + Constant.API_KEY)
    Observable<NowResponse> nowWeather(@Query("location") String cityID);

    @GET("/v7/weather/7d?key=" + Constant.API_KEY)
    Observable<DailyResponse> dailyWeather(@Query("location") String cityID);

    @GET("/v7/indices/1d?key=" + Constant.API_KEY)
    Observable<LifestyleResponse> lifestyle(@Query("type") String type, @Query("location") String cityID);
    //24小时查询
    @GET("/v7/weather/24h?key=" + Constant.API_KEY)
    Observable<HourlyResponse> hourlyWeather(@Query("location") String cityID);




}
