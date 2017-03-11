package ru.zakharov.oleg.openweather;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ru.zakharov.oleg.openweather.gson.ForecastWeather;
import ru.zakharov.oleg.openweather.retrofit.CurrentWeatherSingleton;

import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

public class FragmentWeatherForecast extends Fragment
                                  implements SwipeRefreshLayout.OnRefreshListener{

    public FragmentWeatherForecast() {}
    private class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

        private final int verticalSpaceHeight;

        VerticalSpaceItemDecoration(int verticalSpaceHeight) {
            this.verticalSpaceHeight = verticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = verticalSpaceHeight;
        }
    }

    private MyWeatherForecastAdapter adapter;
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipe;
    private TextView empty;
    private Disposable forecastWeather;
    private int lastCityId = -1;
    private String lastCityName = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState!=null) {
            lastCityId = savedInstanceState.getInt("lastCityId");
            lastCityName = savedInstanceState.getString("lastCityName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weatherforecast, container, false);
        Context context = view.getContext();

        recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        VerticalSpaceItemDecoration divider =
                new VerticalSpaceItemDecoration((int)(5*getResources().getDisplayMetrics().density));
        recyclerView.addItemDecoration(divider);
        adapter = new MyWeatherForecastAdapter(new ArrayList<ForecastWeather>(), lastCityName);
        recyclerView.setAdapter(adapter);
        recyclerView.setVisibility(View.GONE);

        swipe = (SwipeRefreshLayout)view.findViewById(R.id.swipe);
        swipe.setColorSchemeResources(R.color.progress1, R.color.progress2, R.color.progress3);
        swipe.setOnRefreshListener(this);

        empty = (TextView)view.findViewById(R.id.empty);
        if (lastCityId!=-1) {
            swipe.setRefreshing(true);
            recyclerView.setVisibility(View.GONE);
            empty.setText(R.string.weather_loading);
            empty.setVisibility(View.VISIBLE);
            loadForecastWeather();
        }
        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("lastCityId", lastCityId);
        outState.putString("lastCityName", lastCityName);
    }

    @Override
    public void onDestroy() {
        if (forecastWeather!=null && !forecastWeather.isDisposed())
            forecastWeather.dispose();
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        if (lastCityId!=-1 && empty.getVisibility()==View.VISIBLE) {
            swipe.setRefreshing(true);
            empty.setText(R.string.weather_loading);
            empty.setVisibility(View.VISIBLE);
            loadForecastWeather();
        }
        else {
            swipe.setRefreshing(false);
        }
    }

    void loadForecastWeather(){
        if (forecastWeather!=null && !forecastWeather.isDisposed()){
            forecastWeather.dispose();
        }
        forecastWeather = CurrentWeatherSingleton.getForecastWeatherObservable(lastCityId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .cache()
                .subscribeWith(
                        new DisposableObserver<List<ForecastWeather>>() {
                            @Override
                            public void onNext(List<ForecastWeather> forecastWeathers) {
                                Log.d("WeatherMap", "ForecastOnNext");
                                swipe.setRefreshing(false);
                                adapter.updateData(forecastWeathers, lastCityName);
                                if (forecastWeathers.size()!=0) {
                                    recyclerView.setVisibility(View.VISIBLE);
                                    empty.setVisibility(View.GONE);
                                }
                                else {
                                    empty.setText(R.string.weather_no);
                                    empty.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onError(Throwable e) {
                                Log.d("WeatherMap", "ForecastOnError");
                                e.printStackTrace();
                                if (e instanceof SocketTimeoutException)
                                    loadForecastWeather();
                                else {
                                    empty.setText(R.string.weather_error);
                                    empty.setVisibility(View.VISIBLE);
                                }
                            }

                            @Override
                            public void onComplete() {
                                Log.d("WeatherMap", "ForecastOnComplete");
                            }
                        }
                );
    }

    public void setCityId(City city){
        lastCityId = city.id;
        lastCityName = city.name;
        swipe.setRefreshing(true);
        recyclerView.setVisibility(View.GONE);
        adapter.updateData(new ArrayList<ForecastWeather>(), lastCityName);
        empty.setText(R.string.weather_loading);
        empty.setVisibility(View.VISIBLE);
        loadForecastWeather();
    }
}
