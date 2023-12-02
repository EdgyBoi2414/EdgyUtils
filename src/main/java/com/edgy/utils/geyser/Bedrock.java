package com.edgy.utils.geyser;

import java.util.UUID;
import org.geysermc.cumulus.form.Form;
import org.geysermc.floodgate.api.FloodgateApi;
import org.geysermc.geyser.api.GeyserApi;

public class Bedrock {

  public static boolean isBedrock() {
    try {
      Class.forName("org.geysermc.geyser.api.GeyserApi");
      return true;
    } catch (ClassNotFoundException err) {
      return isFloodgate();
    }
  }

  public static boolean isFloodgate() {
    try {
      Class.forName("org.geysermc.floodgate.api.FloodgateApi");
      return true;
    } catch (ClassNotFoundException err) {
      return false;
    }
  }

  public static GeyserApi getGeyserApi() {
    return GeyserApi.api();
  }

  public static FloodgateApi getFloodgateApi() {
    return FloodgateApi.getInstance();
  }

  public static boolean isGeyserPlayer(UUID uniqueId) {
    if (isFloodgate()) {
      return getFloodgateApi().isFloodgatePlayer(uniqueId);
    }

    return getGeyserApi().isBedrockPlayer(uniqueId);
  }

  public static boolean sendForm(UUID uniqueId, Form form) {
    if (isFloodgate()) {
      return getFloodgateApi().sendForm(uniqueId, form);
    }

    return getGeyserApi().sendForm(uniqueId, form);
  }

}
