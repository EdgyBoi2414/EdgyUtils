package com.edgy.utils.shared;

public class NameFormatter {

  public static String apply(String name) {
    if (name.endsWith("s")) {
      return name + "'";
    }

    return name + "'s";
  }

}
