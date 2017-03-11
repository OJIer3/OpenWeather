package ru.zakharov.oleg.openweather.gson;

import android.os.Parcel;
import android.os.Parcelable;

public class CurrentWeather implements Parcelable {
    public String name;
    public int cityId;
    public int temperature;
    public int iconId;
    public float lat, lng;

    private CurrentWeather(Parcel in) {
        name = in.readString();
        cityId = in.readInt();
        temperature = in.readInt();
        iconId = in.readInt();
        lat = in.readFloat();
        lng = in.readFloat();
    }
    CurrentWeather(){
        name="";
        cityId = 0;
        temperature = 0;
        iconId = 0;
        lng=0;
        lat=0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeInt(cityId);
        dest.writeInt(temperature);
        dest.writeInt(iconId);
        dest.writeFloat(lat);
        dest.writeFloat(lng);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<CurrentWeather> CREATOR = new Parcelable.Creator<CurrentWeather>() {
        @Override
        public CurrentWeather createFromParcel(Parcel in) {
            return new CurrentWeather(in);
        }

        @Override
        public CurrentWeather[] newArray(int size) {
            return new CurrentWeather[size];
        }
    };
}