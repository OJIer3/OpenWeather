package ru.zakharov.oleg.openweather.retrofit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.zakharov.oleg.openweather.gson.CurrentWeather;
import ru.zakharov.oleg.openweather.gson.CurrentWeatherDeserializer;
import ru.zakharov.oleg.openweather.gson.ForecastWeather;
import ru.zakharov.oleg.openweather.gson.ForecastWeatherDeserializer;


public class CurrentWeatherSingleton{

    private static OpenWeatherApi openWeatherApi;
    private static BehaviorSubject<List<ForecastWeather>> observableForecast;
    private static Disposable disposableForecast;
    private static int lastCityId;

    private CurrentWeatherSingleton() {}

    public static void init() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<List<CurrentWeather>>(){}.getType(),
                                     new CurrentWeatherDeserializer())
                .registerTypeAdapter(new TypeToken<List<ForecastWeather>>(){}.getType(),
                                     new ForecastWeatherDeserializer())
                .create();

        RxJava2CallAdapterFactory rxAdapter = RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io());

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(10000, TimeUnit.SECONDS)
                .readTimeout(10000,TimeUnit.SECONDS).build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OpenWeatherConstants.BASE_API_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(rxAdapter)
                .client(client)
                .build();
        openWeatherApi = retrofit.create(OpenWeatherApi.class);
        lastCityId=-1;
    }

    public static void dispose(){
        if (disposableForecast!= null && !disposableForecast.isDisposed())
            disposableForecast.dispose();
        if (observableForecast!=null)
            observableForecast.onComplete();
    }

    public static Observable<List<CurrentWeather>> getCurrentWeatherObservable(String coordinates) {
        if (openWeatherApi==null) init();
        return openWeatherApi.getCurrentWeather(coordinates,
                                                OpenWeatherConstants.UNITS,
                                                OpenWeatherConstants.LANG,
                                                OpenWeatherConstants.API_KEY);
    }

    public static Observable<List<ForecastWeather>> getForecastWeatherObservable(int cityId) {
        if (openWeatherApi==null) init();
        if (lastCityId!=cityId) {
            lastCityId=cityId;
            if (observableForecast!=null) observableForecast.onComplete();
            observableForecast = BehaviorSubject.create();
            getDisposableForecast();
        }
        return observableForecast;
    }

    private static void getDisposableForecast(){
        disposableForecast = openWeatherApi.getForecastWeather(lastCityId,
                OpenWeatherConstants.UNITS,
                OpenWeatherConstants.LANG,
                OpenWeatherConstants.API_KEY)
                .timeout(10000, TimeUnit.MILLISECONDS)
                .doOnNext(new Consumer<List<ForecastWeather>>() {
                    @Override
                    public void accept(@NonNull List<ForecastWeather> forecastWeathers) throws Exception {
                        if (observableForecast!=null)
                            observableForecast.onNext(forecastWeathers);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        if (throwable instanceof SocketTimeoutException)
                            getDisposableForecast();
                        else lastCityId=-1;
                    }
                })
                .subscribe();
    }
}