package com.edgy.utils.bungee.collections;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A map that accepts online players as keys. If a player disconnects, they are removed from the key set. Also prevents offline players from being added to the key set.
 */
public class OnlineProxiedPlayerMap<T> {

    private static final List<OnlineProxiedPlayerMap<?>> activeProxiedPlayerMaps = new ArrayList<>();

    private final HashMap<UUID, T> map = new HashMap<>();
    private Consumer<T> onQuit = (value) -> {
    };

    public OnlineProxiedPlayerMap(Consumer<T> onQuit) {
        super();
        this.onQuit = onQuit;
    }

    public OnlineProxiedPlayerMap() {
        activeProxiedPlayerMaps.add(this);
    }

    public static List<OnlineProxiedPlayerMap<?>> getActiveProxiedPlayerMaps() {
        return activeProxiedPlayerMaps;
    }

    public void handleQuit(ProxiedPlayer player) {
        T value = map.get(player.getUniqueId());
        if (value == null) return;
        onQuit.accept(value);
    }

    public T put(ProxiedPlayer player, T value) {
        if (!player.isConnected()) {
            return null;
        }

        return map.put(player.getUniqueId(), value);
    }

    public void putAll(HashMap<ProxiedPlayer, T> map) {
        for (ProxiedPlayer player : map.keySet()) {
            put(player, map.get(player));
        }
    }

    public T remove(ProxiedPlayer player) {
        return map.remove(player.getUniqueId());
    }

    public T get(ProxiedPlayer player) {
        return map.get(player.getUniqueId());
    }

    public boolean containsKey(ProxiedPlayer player) {
        return map.containsKey(player.getUniqueId());
    }

    public List<ProxiedPlayer> keySet() {
        return map.keySet()
                .stream()
                .map(uuid -> ProxyServer.getInstance().getPlayer(uuid))
                .collect(Collectors.toList());
    }

    public List<T> values() {
        return new ArrayList<>(map.values());
    }
}