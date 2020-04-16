package com.nobodysapps.septimanapp.model.storage

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.lang.reflect.Type
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class JsonConverter @Inject constructor() {
    private val gson: Gson = GsonBuilder() //                .registerTypeAdapterFactory(RuntimeAdapterFactories.gameControllerFactory())
//                .registerTypeAdapterFactory(RuntimeAdapterFactories.actionFactory())
//                .registerTypeAdapterFactory(HanabiTypeAdapterFactory.create())
//                .enableComplexMapKeySerialization()
        .create()

    fun <T> toJson(obj: T): String {
        return gson.toJson(obj)
    }

//    fun <T> toJsonMap(obj: T): Map<*, *> {
//        return fromJson<Map<*, *>>(
//            toJson(obj),
//            MutableMap::class.java
//        )
//    }
//
//    fun <T> fromJsonMap(
//        map: Map<String?, Any?>,
//        type: Class<T>?
//    ): T {
//        return gson.fromJson(toJson<Map<String, Any>>(map), type)
//    }

    fun <T> fromJson(json: String?, type: Type): T = gson.fromJson(json, type)

}