package com.edgy.utils.shared;

import java.util.HashMap;
import java.util.Map;

public class MapBuilder<K, V> {

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> mapOf(Object... objects) {
    if (objects.length % 2 != 0) {
      throw new IllegalArgumentException("Objects must be in pairs!");
    }
    MapBuilder<K, V> builder = new MapBuilder<>();
    for (int i = 0; i < objects.length; i += 2) {
      builder.put((K) objects[i], (V) objects[i + 1]);
    }
    return builder.build();
  }

  private final HashMap<K, V> map = new HashMap<>();

  public MapBuilder<K, V> put(K key, V value) {
    map.put(key, value);
    return this;
  }

  public HashMap<K, V> build() {
    return map;
  }

}
