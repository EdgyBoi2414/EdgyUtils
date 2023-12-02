package com.edgy.utils;

import com.edgy.utils.spigot.BukkitPlugin;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class EdgyUtils {

  private static BukkitPlugin bukkitPlugin;

  public static void initializeBukkit(BukkitPlugin plugin) {
    if (bukkitPlugin != null) {
      throw new IllegalStateException("EdgyUtils has already been initialized!");
    }
    bukkitPlugin = plugin;
  }

  public static BukkitPlugin bukkit() {
    if (bukkitPlugin == null) {
      throw new IllegalStateException("EdgyUtils has not been initialized!");
    }
    return bukkitPlugin;
  }

  // todo: bungee
  public static Logger logger() {
    return bukkit().getLogger();
  }

  // todo: bungee
  public static File dataFolder() {
    return bukkit().getDataFolder();
  }

  public static boolean papi() {
    return Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
  }
}
