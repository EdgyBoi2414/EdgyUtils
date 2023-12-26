package com.edgy.utils.shared.cache;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.function.Function;

public class Cache<K, V> {

  public static <K, V> Builder<K, V> builder() {
    return new Builder<>();
  }
  private final HashMap<K, V> cache = new HashMap<>();
  private final HashMap<K, LocalTime> expire = new HashMap<>();
  private final Function<K, V> loader;
  private final Long expiryValue;

  private final TemporalUnit expiryUnit;

  private Cache(Builder<K, V> builder) {
    this.loader = builder.loader;
    this.expiryValue = builder.expiryValue;
    this.expiryUnit = builder.expiryUnit;
  }

  public V get(K key) {
    if (cache.containsKey(key)) {
      if (expire.containsKey(key)) {
        if (LocalTime.now().isBefore(expire.get(key))) {
          return cache.get(key);
        }
      }
    }

    V value = loader.apply(key);
    cache.put(key, value);
    expire.put(key, LocalTime.now().plus(expiryValue, expiryUnit));
    return value;
  }

  public void invalidate(K key) {
    cache.remove(key);
    expire.remove(key);
  }


  public static class Builder<K, V> {

    private Function<K, V> loader = null;
    private Long expiryValue = null;
    private TemporalUnit expiryUnit = null;

    public Builder<K, V> loader(Function<K, V> loader) {
      this.loader = loader;
      return this;
    }

    public Builder<K, V> expiry(long expiryValue, ChronoUnit expiryUnit) {
      this.expiryValue = expiryValue;
      this.expiryUnit = expiryUnit;

      return this;
    }

    public Cache<K, V> build() {
      return new Cache<>(this);
    }

  }

}
