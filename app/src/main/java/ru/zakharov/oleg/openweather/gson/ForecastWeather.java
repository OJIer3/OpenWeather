package ru.zakharov.oleg.openweather.gson;


import android.os.Parcel;
import android.os.Parcelable;


public class ForecastWeather implements Parcelable{
    public String date;
    public String weather;
    public String pressure;
    public String temperature;
    public String clouds;
    public String wind;
    public int iconId;

    ForecastWeather(){
        date="";
        temperature="";
        pressure="";
        weather="";
        clouds="";
        wind="";
        iconId=0;
    }

    protected ForecastWeather(Parcel in) {
        date = in.readString();
        weather = in.readString();
        pressure = in.readString();
        temperature = in.readString();
        clouds = in.readString();
        wind = in.readString();
        iconId = in.readInt();
    }

    public static final Creator<ForecastWeather> CREATOR = new Creator<ForecastWeather>() {
        @Override
        public ForecastWeather createFromParcel(Parcel in) {
            return new ForecastWeather(in);
        }

        @Override
        public ForecastWeather[] newArray(int size) {
            return new ForecastWeather[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(date);
        parcel.writeString(weather);
        parcel.writeString(pressure);
        parcel.writeString(temperature);
        parcel.writeString(clouds);
        parcel.writeString(wind);
        parcel.writeInt(iconId);
    }
}
