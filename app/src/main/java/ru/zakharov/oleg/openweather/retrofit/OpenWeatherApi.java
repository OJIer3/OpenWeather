package ru.zakharov.oleg.openweather.retrofit;

import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;
import ru.zakharov.oleg.openweather.gson.CurrentWeather;
import ru.zakharov.oleg.openweather.gson.ForecastWeather;


interface OpenWeatherApi {
    @GET("box/city")
    Observable<List<CurrentWeather>> getCurrentWeather(@Query("bbox") String coordinates,
                                                       @Query("units") String units,
                                                       @Query("lang") String lang,
                                                       @Query("appid") String id);
    @GET("forecast")
    Observable<List<ForecastWeather>> getForecastWeather(@Query("id") int cityId,
                                                         @Query("units") String units,
                                                         @Query("lang") String lang,
                                                         @Query("appid") String id);
}
