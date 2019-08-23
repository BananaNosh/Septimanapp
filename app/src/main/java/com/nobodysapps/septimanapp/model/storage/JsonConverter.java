package com.nobodysapps.septimanapp.model.storage;

import androidx.annotation.NonNull;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

@Singleton
public class JsonConverter {
    private Gson gson;

    @Inject
    public JsonConverter() {
        gson = new GsonBuilder()
//                .registerTypeAdapterFactory(RuntimeAdapterFactories.gameControllerFactory())
//                .registerTypeAdapterFactory(RuntimeAdapterFactories.actionFactory())
//                .registerTypeAdapterFactory(HanabiTypeAdapterFactory.create())
//                .enableComplexMapKeySerialization()
                .create();
    }


    @NonNull
    public <T> String toJson(T obj) {
        return gson.toJson(obj);
    }

    public <T> Map toJsonMap(T obj) {
        return fromJson(toJson(obj), Map.class);
    }

    public <T> T fromJsonMap(@NonNull Map<String, Object> map, Class<T> type) {
        return gson.fromJson(toJson(map), type);
    }

    public <T> T fromJson(String json, Class<T> type) {
        return gson.fromJson(json, type);
    }
}
