package com.edgy.utils.spigot.listener;

import com.edgy.utils.spigot.collections.OnlinePlayerList;
import com.edgy.utils.spigot.collections.OnlinePlayerMap;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class OnlinePlayerCollectionListener implements Listener {

  @EventHandler
  public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
    OnlinePlayerList.getActivePlayerLists().forEach(onlinePlayerList -> onlinePlayerList.remove(event.getPlayer()));
    OnlinePlayerMap.getActivePlayerMaps().forEach(onlinePlayerMap -> {
      onlinePlayerMap.handleQuit(event.getPlayer());
      onlinePlayerMap.remove(event.getPlayer());
    });
  }

}
