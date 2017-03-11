package ru.zakharov.oleg.openweather;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMapOptions;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.net.SocketTimeoutException;
import java.util.Formatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;
import ru.zakharov.oleg.openweather.gson.CurrentWeather;
import ru.zakharov.oleg.openweather.gson.ForecastWeather;
import ru.zakharov.oleg.openweather.retrofit.CurrentWeatherSingleton;

public class WeatherMap extends AppCompatActivity implements OnMapReadyCallback{

    public class PagerAdapter extends FragmentPagerAdapter {

        PagerAdapter(FragmentManager fm) {super(fm);}

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    CameraPosition camPos = new CameraPosition.Builder()
                                    .target(new LatLng(56.7351924, 38.7835911))
                                    .zoom(4)
                                    .bearing(0)
                                    .tilt(0)
                                    .build();
                    GoogleMapOptions options = new GoogleMapOptions();
                    options.mapType(GoogleMap.MAP_TYPE_TERRAIN)
                            .compassEnabled(true)
                            .rotateGesturesEnabled(true)
                            .scrollGesturesEnabled(true)
                            .tiltGesturesEnabled(true)
                            .zoomControlsEnabled(true)
                            .zoomGesturesEnabled(true)
                            .useViewLifecycleInFragment(true)
                            .camera(camPos);
                    final SupportMapFragment fragment = SupportMapFragment.newInstance(options);
                    fragment.getMapAsync(WeatherMap.this);
                    return fragment;
                }
                case 1: return new FragmentWeatherForecast();
            }
            return null;
        }
        @Override
        public int getCount() { return 2;}
        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0: return getString(R.string.tab_map);
                case 1: return getString(R.string.tab_weather);
            }
            return null;
        }
    }

    public static class GoogleDialog extends DialogFragment {
        public static final String TAG = " GoogleDialog";


        @android.support.annotation.NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())//, R.style.MyAlertDialogStyle)
                    .setTitle(getString(R.string.dialog_title_google))
                    .setMessage(GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(getActivity()))
                    .create();
        }
    }


    private GoogleMap mMap;
    ViewPager mViewPager;
    private Disposable currentWeather;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.title_activity_weather_map);
        toolbar.setLogo(R.mipmap.ic_launcher);
        setSupportActionBar(toolbar);

        FragmentManager fm = getSupportFragmentManager();
        mViewPager = (ViewPager) findViewById(R.id.container);
        PagerAdapter mPagerAdapter = new PagerAdapter(fm);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        SupportMapFragment fragment =
                (SupportMapFragment) fm.findFragmentByTag("android:switcher:" + R.id.container + ":0");
        if (fragment!=null){
            fragment.getMapAsync(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem item = menu.add(Menu.NONE, 0, Menu.NONE, R.string.menu_google);
        item.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 0:
                new GoogleDialog().show(getSupportFragmentManager(), GoogleDialog.TAG);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.d("WeatherMap", "OnCameraIdle");
                loadCurrentWeather();
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                mViewPager.setCurrentItem(1, true);
                Fragment frag = getSupportFragmentManager()
                        .findFragmentByTag("android:switcher:" + R.id.container + ":1");
                if (frag!=null && frag instanceof FragmentWeatherForecast){
                    ((FragmentWeatherForecast) frag).setCityId((City)marker.getTag());
                }
                return true;
            }
        });
    }

    private String getVisibleAreaCoord(){
        VisibleRegion region = mMap.getProjection().getVisibleRegion();
        LatLng leftBottom = region.nearLeft;
        LatLng rightTop = region.farRight;
        int zoom = (int)(mMap.getCameraPosition().zoom+0.5);
        StringBuilder sb = new StringBuilder();
        Formatter formatter = new Formatter(sb, Locale.US);
        formatter.format("%1$.6f,%2$.6f,%3$.6f,%4$.6f,%5$d",
                leftBottom.longitude, leftBottom.latitude,
                rightTop.longitude, rightTop.latitude,
                zoom);
        return sb.toString();
    }

    private void loadCurrentWeather() {
        Log.d("WeatherMap", "loadCurrentWeather");
        if (currentWeather !=null && !currentWeather.isDisposed()) {
            currentWeather.dispose();
        }
        currentWeather = Observable.just(0)
                .delay(2000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<Integer, Observable<List<CurrentWeather>>>() {
                             @Override
                             public Observable<List<CurrentWeather>> apply(@NonNull Integer integer) throws Exception {
                                 return CurrentWeatherSingleton.getCurrentWeatherObservable(getVisibleAreaCoord());
                             }
                         }
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .doOnNext(new Consumer<List<CurrentWeather>>() {
                    @Override
                    public void accept(@NonNull List<CurrentWeather> currentWeathers) throws Exception {
                        Log.d("WeatherMap", "CurrentOnNext");
                        mMap.clear();
                        for (CurrentWeather cw : currentWeathers){
                            Marker marker =
                                    mMap.addMarker(new MarkerOptions()
                                            .position(new LatLng(cw.lat, cw.lng))
                                            .icon(BitmapDescriptorFactory.fromBitmap(
                                                    getMarkerBitmapFromView(cw.iconId,
                                                            cw.name,
                                                            cw.temperature))));
                            marker.setTag(new City(cw.name, cw.cityId));
                        }
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(@NonNull Throwable throwable) throws Exception {
                        Log.d("WeatherMap", "CurrentOnError");
                        throwable.printStackTrace();
                        if (throwable instanceof SocketTimeoutException)
                            loadCurrentWeather();
                    }
                })
                .subscribe();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (currentWeather!=null && !currentWeather.isDisposed())
            currentWeather.dispose();
    }

    @SuppressLint({"SetTextI18n","InflateParams"})
    private Bitmap getMarkerBitmapFromView(int iconId, String name, int temp) {

        View customMarkerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                .inflate(R.layout.onmap_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.marker_icon);
        markerImageView.setImageResource(iconId);
        ((TextView)customMarkerView.findViewById(R.id.marker_name)).setText(name);
        ((TextView)customMarkerView.findViewById(R.id.marker_temp)).setText(Integer.toString(temp) + " \u2103");
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();
        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(),
                                                    customMarkerView.getMeasuredHeight(),
                                                    Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);
        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);
        return returnedBitmap;
    }
}
