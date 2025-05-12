package org.ulpgc.business.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class JsonMapperService {
    private final Gson gson;

    public JsonMapperService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(JsonArray.class, new JsonArrayAdapter())
                .create();
    }

    public <T> T fromJson(JsonObject jsonObject, Class<T> clazz) {
        return gson.fromJson(jsonObject, clazz);
    }

    public <T> List<T> fromJsonArray(JsonArray jsonArray, Class<T> clazz) {
        return gson.fromJson(jsonArray, TypeToken.getParameterized(List.class, clazz).getType());
    }
}