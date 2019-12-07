package com.thanthi.dtnext.dtnextapplication.database;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.thanthi.dtnext.dtnextapplication.model.SubMenuApi;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

public class MenuConvertors {

    private static Gson gson = new Gson();

    @TypeConverter
    public static List<SubMenuApi> stringToList(String data){
        if (data == null){
            return Collections.emptyList();
        }
        Type listType = new TypeToken<List<SubMenuApi>>(){}.getType();
        return gson.fromJson(data, listType);
    }

    @TypeConverter
    public static String listToString(List<SubMenuApi> subMenuApiList){
        return gson.toJson(subMenuApiList);
    }
}
