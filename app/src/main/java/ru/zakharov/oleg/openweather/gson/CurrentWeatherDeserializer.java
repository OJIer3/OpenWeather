package ru.zakharov.oleg.openweather.gson;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import ru.zakharov.oleg.openweather.R;

public class CurrentWeatherDeserializer implements JsonDeserializer<List<CurrentWeather>> {
    @Override
    public List<CurrentWeather> deserialize(JsonElement json,
                                            Type typeOfT,
                                            JsonDeserializationContext context)
                                            throws JsonParseException
    {
        Log.d("WeatherMap", "CurrentWeatherDeserializer");
        List<CurrentWeather> result = new ArrayList<>();
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray list = jsonObject.getAsJsonArray("list");
        for (JsonElement element : list){
            JsonObject entry = element.getAsJsonObject();
            CurrentWeather cw = new CurrentWeather();

            //Получаем id города
            cw.cityId = entry.getAsJsonPrimitive("id").getAsInt();

            //Полуаем название города
            cw.name = entry.getAsJsonPrimitive("name").getAsString();

            //Получаем координаты города
            JsonObject coord = entry.getAsJsonObject("coord");
            try {cw.lat = coord.getAsJsonPrimitive("lat").getAsFloat();}
            catch (Exception ignore){cw.lat = coord.getAsJsonPrimitive("Lat").getAsFloat();}
            try {cw.lng = coord.getAsJsonPrimitive("lon").getAsFloat();}
            catch (Exception ignore){cw.lng = coord.getAsJsonPrimitive("Lon").getAsFloat();}

            //Получаем температуру в городе
            cw.temperature = entry.getAsJsonObject("main").getAsJsonPrimitive("temp").getAsInt();

            //Узнаем какую иконку погоды показывать
            int iconId;
            try {
                JsonObject weather = entry.getAsJsonArray("weather").get(0).getAsJsonObject();
                iconId = Integer.parseInt(weather.getAsJsonPrimitive("icon").getAsString().substring(0,2));
            }
            catch (Exception e){
                iconId=0;
            }
            switch (iconId){
                case 1: {cw.iconId = R.drawable.sunny; break;} //ясно
                case 2: {cw.iconId=R.drawable.partly_cloudy; break;} //облачно с прояснениями
                case 3: {cw.iconId=R.drawable.cloudy; break;} //облачно
                case 4: {cw.iconId=R.drawable.cloudy; break;} //дождевые облака
                case 9: {cw.iconId=R.drawable.drizzle; break;} //ливни
                case 10: {cw.iconId=R.drawable.drizzle; break;} //дождь
                case 11: {cw.iconId=R.drawable.thunderstorms; break;} //гроза
                case 13: {cw.iconId=R.drawable.snowfall; break;} //снег
                case 50: {cw.iconId=R.drawable.haze; break;} //дымка
                default: {cw.iconId=R.drawable.slight_drizzle; break;} //неизвестна
            }
            result.add(cw);
        }
        return result;
    }
}