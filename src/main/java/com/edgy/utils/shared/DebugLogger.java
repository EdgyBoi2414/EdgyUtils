package com.edgy.utils.shared;

import com.edgy.utils.EdgyUtils;
import java.util.logging.Level;

public class DebugLogger {

  private static boolean enabled = false;

  public static void enabled(boolean enabled) {
    DebugLogger.enabled = enabled;
  }

  public static boolean enabled() {
    return enabled;
  }

  public static void info(String message) {
    log(Level.INFO, message);
  }

  public static void warning(String message) {
    log(Level.WARNING, message);
  }

  public static void severe(String message) {
    log(Level.SEVERE, message);
  }

  public static void log(String message) {
    log(Level.INFO, message);
  }

  public static void log(Level level, String message) {
    log(level, message, null);
  }

  public static void log(Level level, String message, Throwable throwable) {
    if (!enabled) {
      return;
    }

    EdgyUtils.logger().log(level, message, throwable);
  }

}
