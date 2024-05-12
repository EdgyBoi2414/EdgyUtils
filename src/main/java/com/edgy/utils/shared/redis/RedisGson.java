package com.edgy.utils.shared.redis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;

public class RedisGson {

  private static GsonBuilder gsonBuilder = new GsonBuilder();

  public static void registerTypeAdapter(Type type, Object typeAdapter) {
    gsonBuilder.registerTypeAdapter(type, typeAdapter);
  }

  public static Gson get() {
    return gsonBuilder.create();
  }

}
