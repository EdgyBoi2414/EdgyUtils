package com.edgy.utils.spigot;

import com.edgy.utils.EdgyUtils;
import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {

  private final Plugin plugin = EdgyUtils.bukkit();
  private final String fileName;
  private final File configFile;

  private YamlConfiguration config;

  public Config(String fileName) {
    this(fileName, false);
  }

  public Config(String fileName, boolean holdLoad) {
    this.fileName = fileName;
    this.configFile = new File(plugin.getDataFolder(), fileName);

    if (!holdLoad) {
      this.config = loadConfig();
    }
  }

  public Config(File file, boolean holdLoad) {
    this.fileName = file.getName();
    this.configFile = file;

    if (!holdLoad) {
      this.config = loadConfig();
    }
  }

  private YamlConfiguration loadConfig() {
    if (!configFile.exists()) {
      configFile.getParentFile().mkdirs();
      plugin.saveResource(fileName, false);
    }

    return YamlConfiguration.loadConfiguration(configFile);
  }

  private boolean saveConfig() {
    try {
      config.save(configFile);
      return true;
    } catch (Exception err) {
      err.printStackTrace();
      return false;
    }
  }

  private void reloadConfig() {
    config = loadConfig();
  }

  public YamlConfiguration get() {
    return config;
  }

  public boolean saveAndReload() {
    if (saveConfig()) {
      reloadConfig();
      return true;
    } else {
      return false;
    }
  }

  public boolean save() {
    return saveConfig();
  }

  public void reload() {
    reloadConfig();
  }

  public Location getLocation(String path) {
    ConfigurationSection section = config.getConfigurationSection(path);
    if (section == null) {
      throw new RuntimeException(
          new InvalidConfigurationException("Location section " + path + " is missing!"));
    }

    return new Location(
        Bukkit.getWorld(Objects.requireNonNull(section.getString("world"))),
        section.getDouble("x"),
        section.getDouble("y"),
        section.getDouble("z"),
        (float) section.getDouble("yaw", 0),
        (float) section.getDouble("pitch", 0)
    );
  }

  public List<Location> getLocations(String path) {
    return config.getMapList(path)
        .stream()
        .map(map -> new Location(
            Bukkit.getWorld((String) map.get("world")),
            ((Number) map.get("x")).doubleValue(),
            ((Number) map.get("y")).doubleValue(),
            ((Number) map.get("z")).doubleValue()
        ))
        .collect(Collectors.toList());
  }

  public String getRandomString(String listPath) {
    List<String> list = config.getStringList(listPath);
    return list.get(ThreadLocalRandom.current().nextInt(list.size()));
  }

}
