package com.edgy.utils.shared;

import com.edgy.utils.EdgyUtils;
import java.util.logging.Level;

public class BuiltInLogger {

  public final void log(Level level, String message) {
    EdgyUtils.logger().log(level, "[" + this.getClass().getSimpleName() + "] " + message);
  }
  public final void log(Level level, String message, Throwable throwable) {
    EdgyUtils.logger().log(level,"[" + this.getClass().getSimpleName() + "] " + message, throwable);
  }

}
