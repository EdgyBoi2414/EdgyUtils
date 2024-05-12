package com.edgy.utils.bungee;

import com.edgy.utils.EdgyUtils;
import com.edgy.utils.shared.ConfigHolder;
import com.google.common.io.ByteStreams;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeConfig extends ConfigHolder<Configuration> {

  public BungeeConfig(String fileName) {
    super(fileName, EdgyUtils.dataFolder());
  }

  public BungeeConfig(String fileName, ClassLoader classLoader) {
    super(fileName, EdgyUtils.dataFolder(), classLoader);
  }

  @Override
  protected Configuration onLoad() {
    if (!configFile.exists()) {
      configFile.getParentFile().mkdirs();
      saveResource();
    }

    try {
      config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
      return config;
    } catch (Exception err) {
      err.printStackTrace();
      return null;
    }
  }

  @Override
  protected void saveResource() {
    try {
      InputStream in = getResource(fileName);
      OutputStream out = Files.newOutputStream(configFile.toPath());
      ByteStreams.copy(in, out);
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  @Override
  protected boolean saveConfig() {
    try {
      ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, configFile);
      return true;
    } catch (Exception err) {
      err.printStackTrace();
      return false;
    }
  }

}
