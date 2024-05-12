package com.edgy.utils;

import com.edgy.utils.bungee.BungeePlugin;
import com.edgy.utils.spigot.BukkitPlugin;
import java.io.File;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;

public class EdgyUtils {

  private static BukkitPlugin bukkitPlugin;
  private static BungeePlugin bungeePlugin;

  public static void initializeBukkit(BukkitPlugin plugin) {
    if (bukkitPlugin != null) {
      throw new IllegalStateException("EdgyUtils has already been initialized!");
    }
    bukkitPlugin = plugin;
  }

  public static void initializeBungee(BungeePlugin plugin) {
    if (bungeePlugin != null) {
      throw new IllegalStateException("EdgyUtils has already been initialized!");
    }
    bungeePlugin = plugin;
  }

  public static BukkitPlugin bukkit() {
    if (bukkitPlugin == null) {
      throw new IllegalStateException("EdgyUtils has not been initialized!");
    }
    return bukkitPlugin;
  }

  public static BungeePlugin bungee() {
    if (bungeePlugin == null) {
      throw new IllegalStateException("EdgyUtils has not been initialized!");
    }
    return bungeePlugin;
  }

  public static Logger logger() {
    if (bukkitPlugin != null) {
      return bukkit().getLogger();
    }

    if (bungeePlugin != null) {
      return bungee().getLogger();
    }

    throw new IllegalStateException("EdgyUtils has not been initialized!");
  }


  public static File dataFolder() {
    if (bukkitPlugin != null) {
      return bukkit().getDataFolder();
    }

    if (bungeePlugin != null) {
      return bungee().getDataFolder();
    }

    throw new IllegalStateException("EdgyUtils has not been initialized!");
  }

  public static boolean papi() {
    if (bukkitPlugin != null) {
      return bukkit().getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
    }

    if (bungeePlugin != null) {
      return bungee().getProxy().getPluginManager().getPlugin("PlaceholderAPI") != null;
    }

    return false;
  }
}
