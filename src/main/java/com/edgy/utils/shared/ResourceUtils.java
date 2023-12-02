package com.edgy.utils.shared;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourceUtils {

  public static void saveResource(File dataFolder, String resourcePath) {
    if (resourcePath == null || resourcePath.equals("")) {
      throw new IllegalArgumentException("Resource path cannot be null or empty");
    }

    resourcePath = resourcePath.replace('\\', '/');
    InputStream in = getResource(resourcePath);
    if (in == null) {
      throw new IllegalArgumentException("The embedded resource '" + resourcePath + "' cannot be found!");
    }

    File outFile = new File(dataFolder, resourcePath);
    int lastIndex = resourcePath.lastIndexOf('/');
    File outDir = new File(dataFolder, resourcePath.substring(0, Math.max(lastIndex, 0)));

    if (!outDir.exists()) {
      outDir.mkdirs();
    }

    try {
      Properties oldProps = new Properties();
      Properties newProps = new Properties();

      newProps.load(in);
      if (outFile.exists()) {
        oldProps.load(Files.newInputStream(outFile.toPath()));
        newProps.putAll(oldProps);
      }

      newProps.store(Files.newOutputStream(outFile.toPath()), null);
    } catch (IOException ex) {
      Logger.getAnonymousLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, ex);
    }
  }

  public static InputStream getResource(String filename) {
    if (filename == null) {
      throw new IllegalArgumentException("Filename cannot be null");
    }

    try {
      URL url = ResourceUtils.class.getClassLoader().getResource(filename);

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

}
