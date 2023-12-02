package com.edgy.utils.spigot;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class AccountFetcher {

  public static String getName(UUID uniqueId) {
    try {
      HttpURLConnection urlConnection = (HttpURLConnection) new URL(
          "https://sessionserver.mojang.com/session/minecraft/profile/"
              + uniqueId.toString()).openConnection();
      urlConnection.setRequestMethod("GET");

      BufferedReader in = new BufferedReader(
          new InputStreamReader(urlConnection.getInputStream())
      );
      String line;
      StringBuilder content = new StringBuilder();
      while ((line = in.readLine()) != null) {
        content.append(line);
      }
      in.close();

      JSONObject json = (JSONObject) new JSONParser().parse(content.toString());

      return (String) json.get("name");
    } catch (Exception err) {
      return null;
    }
  }

}
