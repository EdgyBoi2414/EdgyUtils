package com.edgy.utils.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public abstract class ConfigHolder<C> {

  protected final String fileName;
  protected final File dataFolder;
  protected final File configFile;
  protected final ClassLoader classLoader;

  protected C config;

  public ConfigHolder(String fileName, File dataFolder, ClassLoader classLoader) {
    this.fileName = fileName;
    this.classLoader = classLoader;
    this.dataFolder = dataFolder;
    this.configFile = new File(dataFolder, fileName);

    this.config = loadConfig();
  }

  public ConfigHolder(String fileName, File dataFolder) {
    this(fileName, dataFolder, ConfigHolder.class.getClassLoader());
  }

  public C get() {
    return config;
  }

  public void reload() {
    config = loadConfig();
  }

  public boolean saveAndReload() {
    if (saveConfig()) {
      reload();
      return true;
    } else {
      return false;
    }
  }

  public boolean save() {
    return saveConfig();
  }

  public InputStream getResource(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename cannot be null");
    }

    try {
      URL url = classLoader.getResource(filename);

      if (url == null) {
        return null;
      }

      URLConnection connection = url.openConnection();
      connection.setUseCaches(false);
      return connection.getInputStream();
    } catch (IOException ex) {
      return null;
    }
  }

  public C loadConfig() {
    if (!configFile.exists()) {
      configFile.getParentFile().mkdirs();
      saveResource();
    }

    return onLoad();
  }

  protected abstract C onLoad();
  protected abstract void saveResource();

  protected abstract boolean saveConfig();


}