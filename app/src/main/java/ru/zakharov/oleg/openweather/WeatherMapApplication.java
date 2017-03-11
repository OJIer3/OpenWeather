package ru.zakharov.oleg.openweather;

import android.app.Application;

import ru.zakharov.oleg.openweather.retrofit.CurrentWeatherSingleton;

public class WeatherMapApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        CurrentWeatherSingleton.init();
    }

    @Override
    public void onTerminate() {
        CurrentWeatherSingleton.dispose();
        super.onTerminate();
    }
}
