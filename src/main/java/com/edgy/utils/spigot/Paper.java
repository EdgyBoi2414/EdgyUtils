package com.edgy.utils.spigot;

public class Paper {

  public static boolean isPaper() {
    try {
      Class.forName("com.destroystokyo.paper.ParticleBuilder");
      return true;
    } catch (ClassNotFoundException err) {
      return false;
    }
  }

}
