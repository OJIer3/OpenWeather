package ru.zakharov.oleg.openweather;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.zakharov.oleg.openweather.gson.ForecastWeather;

import java.util.List;

class MyWeatherForecastAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    private final List<ForecastWeather> mValues;
    private String cityName;

    MyWeatherForecastAdapter(List<ForecastWeather> items, String cityName) {
        mValues = items;
        this.cityName = cityName;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType==TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_weatherforecast_item, parent, false);
            return new ItemHolder(view);
        }
        else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_weatherforecast_header, parent, false);
            return new HeaderHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemHolder) {
            ForecastWeather fw = mValues.get(position-1);
            ((ItemHolder) holder).date.setText(fw.date);
            ((ItemHolder) holder).date.setCompoundDrawablesWithIntrinsicBounds(fw.iconId, 0, 0, 0);
            ((ItemHolder) holder).weather.setText(fw.weather);
            ((ItemHolder) holder).temperature.setText(fw.temperature);
            ((ItemHolder) holder).pressure.setText(fw.pressure);
            ((ItemHolder) holder).clouds.setText(fw.clouds);
            ((ItemHolder) holder).wind.setText(fw.wind);
        }
        else {
            ((HeaderHolder) holder).header.setText(cityName);
        }
    }

    @Override
    public int getItemCount() {
        return mValues.size()+1;
    }

    @Override
    public int getItemViewType(int position) {
        return position==0 ? TYPE_HEADER : TYPE_ITEM;
    }

    void updateData(List<ForecastWeather> items, String cityName){
        this.cityName = cityName;
        mValues.clear();
        mValues.addAll(items);
        notifyDataSetChanged();
    }

    private class ItemHolder extends RecyclerView.ViewHolder {
        final TextView date;
        final TextView weather;
        final TextView temperature;
        final TextView pressure;
        final TextView clouds;
        final TextView wind;

        ItemHolder(View view) {
            super(view);
            date = (TextView) view.findViewById(R.id.date);
            weather = (TextView) view.findViewById(R.id.weather);
            temperature = (TextView) view.findViewById(R.id.temperature);
            pressure = (TextView) view.findViewById(R.id.pressure);
            clouds = (TextView) view.findViewById(R.id.clouds);
            wind = (TextView) view.findViewById(R.id.wind);
        }
    }

    private class HeaderHolder extends RecyclerView.ViewHolder{
        final TextView header;
        HeaderHolder(View view){
            super(view);
            header = (TextView)view.findViewById(R.id.header);
        }

    }
}
