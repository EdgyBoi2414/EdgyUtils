package com.edgy.utils.shared;

import java.io.File;
import java.util.Objects;

public class FileUtils {

  public static int delete(File file) {
    int fails = 0;
    if (file.exists()) {
      File[] files = file.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isDirectory()) {
            delete(f);
          } else {
            fails += f.delete() ? 0 : 1;
          }
        }
      }
    }

    fails += file.delete() ? 0 : 1;
    return fails;
  }

}
