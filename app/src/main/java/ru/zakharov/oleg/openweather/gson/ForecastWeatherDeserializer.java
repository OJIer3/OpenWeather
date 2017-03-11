package ru.zakharov.oleg.openweather.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.zakharov.oleg.openweather.R;


public class ForecastWeatherDeserializer implements JsonDeserializer<List<ForecastWeather>> {

    @Override
    public List<ForecastWeather> deserialize(JsonElement json,
                                             Type typeOfT,
                                             JsonDeserializationContext context)
                                             throws JsonParseException
    {
        final String DATE_FORMAT = "EEEE, d MMM, HH:mm";
        final double mmHg = 0.75006375541921;

        List<ForecastWeather> result = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT, Locale.getDefault());
        JsonObject jsonObject = json.getAsJsonObject();
        JsonArray list = jsonObject.getAsJsonArray("list");
        for (JsonElement element : list){
            ForecastWeather fw = new ForecastWeather();
            JsonObject entry = element.getAsJsonObject();

            //поулчение даты прогноза
            fw.date = sdf.format(entry.getAsJsonPrimitive("dt").getAsLong()* 1000L);

            //получение температуры и давления в мм рт. столба
            JsonObject main = entry.getAsJsonObject("main");
            fw.temperature = String.format(Locale.US, "Температура: %1$.1f ℃",
                                           main.getAsJsonPrimitive("temp").getAsFloat());
            fw.pressure = String.format(Locale.US, "Давление: %1$d мм рт.ст.",
                       (int)(main.getAsJsonPrimitive("pressure").getAsFloat()*mmHg+0.5));

            //получение описания погоды и соответствующей иконки
            JsonObject weather = entry.getAsJsonArray("weather").get(0).getAsJsonObject();
            fw.weather = weather.getAsJsonPrimitive("description").getAsString();
            int iconId;
            try {iconId = Integer.parseInt(weather.getAsJsonPrimitive("icon").getAsString().substring(0,2));}
            catch (Exception e){iconId=0;}
            switch (iconId){
                case 1: {fw.iconId = R.drawable.sunny; break;} //ясно
                case 2: {fw.iconId=R.drawable.partly_cloudy; break;} //облачно с прояснениями
                case 3: {fw.iconId=R.drawable.cloudy; break;} //облачно
                case 4: {fw.iconId=R.drawable.cloudy; break;} //дождевые облака
                case 9: {fw.iconId=R.drawable.drizzle; break;} //ливни
                case 10: {fw.iconId=R.drawable.drizzle; break;} //дождь
                case 11: {fw.iconId=R.drawable.thunderstorms; break;} //гроза
                case 13: {fw.iconId=R.drawable.snowfall; break;} //снег
                case 50: {fw.iconId=R.drawable.haze; break;} //дымка
                default: {fw.iconId=R.drawable.slight_drizzle; break;} //неизвестна
            }

            //получение данных об облачности
            try {
                fw.clouds = String.format(Locale.US, "Облачность: %1$d%%",
                        entry.getAsJsonObject("clouds").getAsJsonPrimitive("all").getAsInt());
            }
            catch (Exception e){
                fw.clouds = "Облачность: нет данных";
            }

            //получение данных о ветре
            JsonObject wind = entry.getAsJsonObject("wind");
            String speed = String.format(Locale.US, "Ветер: %1$.1f м/с ",
                                          wind.getAsJsonPrimitive("speed").getAsFloat());
            float direction = wind.getAsJsonPrimitive("deg").getAsFloat();
            if (direction>=337.5 || direction<22.5 ) speed+="C";
            else if (direction>=22.5 && direction<67.5) speed+="СВ";
            else if (direction>=67.5 && direction<112.5) speed+="В";
            else if (direction>=112.5 && direction<157.5) speed+="ЮВ";
            else if (direction>=157.5 && direction<202.5) speed+="Ю";
            else if (direction>=202.5 && direction<247.5) speed+="ЮЗ";
            else if (direction>=247.5 && direction<292.5) speed+="З";
            else  speed+="СЗ";
            fw.wind = speed;
            result.add(fw);
        }
        return result;
    }
}
