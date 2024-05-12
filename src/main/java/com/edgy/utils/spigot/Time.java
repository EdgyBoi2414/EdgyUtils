package com.edgy.utils.spigot;

public class Time {
  public static String secondsToMinutesAndSeconds(int seconds) {
    return ticksToMinutesAndSeconds(seconds * 20);
  }

  public static String ticksToMinutesAndSeconds(long ticks) {
    return ticksToMinutesAndSeconds((int) ticks);
  }

  public static String ticksToMinutesAndSeconds(int ticks) {
    int totalSeconds = ticks / 20;

    int minutes = totalSeconds / 60;
    int seconds = totalSeconds % 60;

    return minutes + ":" + formatSeconds(seconds);
  }

  public static String ticksToSeconds(int ticks) {
    return String.valueOf(ticks / 20);
  }

  public static String formatSeconds(Number seconds) {
    if (String.valueOf(seconds).toCharArray().length == 1)
      return "0" + seconds;
    else
      return String.valueOf(seconds);
  }
}
