package com.edgy.utils.shared.time;

import java.time.LocalTime;
import java.util.concurrent.TimeUnit;
import org.jetbrains.annotations.NotNull;

public class Interval implements Comparable<Interval> {

  private final long millis;

  /**
   * Create a new interval object from a string with the format
   * <code>1d 1h 1m 1s</code>
   * @param interval Interval as string
   */
  public Interval(String interval) {
    long millis = 0;

    String[] split = interval.split(" ");
    for (String subInterval : split) {
      if (subInterval.length() < 2) {
        throw new IllegalArgumentException("Invalid sub-interval format: " + subInterval);
      }

      char lastChar = subInterval.charAt(subInterval.length() - 1);
      long timeValue = Long.parseLong(subInterval.substring(0, subInterval.length() - 1));
      switch (lastChar) {
        case 'd' -> millis += TimeUnit.DAYS.toMillis(timeValue);
        case 'h' -> millis += TimeUnit.HOURS.toMillis(timeValue);
        case 'm' -> millis += TimeUnit.MINUTES.toMillis(timeValue);
        case 's' -> millis += TimeUnit.SECONDS.toMillis(timeValue);
      }
    }

    this.millis = millis;
  }

  public long millis() {
    return millis;
  }

  public long asUnit(TimeUnit unit) {
    return switch (unit) {
      case NANOSECONDS -> TimeUnit.MILLISECONDS.toNanos(millis);
      case MICROSECONDS -> TimeUnit.MILLISECONDS.toMicros(millis);
      case MILLISECONDS -> TimeUnit.MILLISECONDS.toMillis(millis);
      case SECONDS -> TimeUnit.MILLISECONDS.toSeconds(millis);
      case MINUTES -> TimeUnit.MILLISECONDS.toMinutes(millis);
      case HOURS -> TimeUnit.MILLISECONDS.toHours(millis);
      case DAYS -> TimeUnit.MILLISECONDS.toDays(millis);
    };
  }

  public long asTicks() {
    return asUnit(TimeUnit.SECONDS) / 20;
  }

  public LocalTime subtract(LocalTime time) {
    return time.minusNanos(asUnit(TimeUnit.NANOSECONDS));
  }

  @Override
  public int compareTo(@NotNull Interval interval) {
    return Long.compare(millis, interval.millis);
  }
}
