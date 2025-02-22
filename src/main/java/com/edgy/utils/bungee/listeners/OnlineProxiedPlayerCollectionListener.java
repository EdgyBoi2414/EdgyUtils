package com.edgy.utils.bungee.listeners;

import com.edgy.utils.bungee.collections.OnlineProxiedPlayerList;
import com.edgy.utils.bungee.collections.OnlineProxiedPlayerMap;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class OnlineProxiedPlayerCollectionListener implements Listener {

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        OnlineProxiedPlayerList.getActiveProxiedPlayerLists().forEach(onlinePlayerList -> {
            onlinePlayerList.remove(event.getPlayer());
        });
        OnlineProxiedPlayerMap.getActiveProxiedPlayerMaps().forEach(onlinePlayerMap -> {
            onlinePlayerMap.handleQuit(event.getPlayer());
            onlinePlayerMap.remove(event.getPlayer());
        });
    }

}
