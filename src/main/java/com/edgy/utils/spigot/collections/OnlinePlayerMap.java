package com.edgy.utils.spigot.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A map that accepts online players as keys. If a player disconnects, they are removed from the key set. Also prevents offline players from being added to the key set.
 */
public class OnlinePlayerMap<T> {

    private static final List<OnlinePlayerMap<?>> activePlayerMaps = new ArrayList<>();

    private final HashMap<UUID, T> map = new HashMap<>();
    private Consumer<T> onQuit = (value) -> {};

    public OnlinePlayerMap(Consumer<T> onQuit) {
        super();
        this.onQuit = onQuit;
    }

    public OnlinePlayerMap() {
        activePlayerMaps.add(this);
    }

    public static List<OnlinePlayerMap<?>> getActivePlayerMaps() {
        return activePlayerMaps;
    }

    public void handleQuit(Player player) {
        T value = map.get(player.getUniqueId());
        if (value == null) return;
        onQuit.accept(value);
    }

    public T put(Player player, T value) {
      if (!player.isOnline()) {
        return null;
      }

        return map.put(player.getUniqueId(), value);
    }

    public void putAll(HashMap<Player, T> map) {
        for (Player player : map.keySet()) {
            put(player, map.get(player));
        }
    }

    public T remove(Player player) {
        return map.remove(player.getUniqueId());
    }

    public T get(Player player) {
        return map.get(player.getUniqueId());
    }

    public boolean containsKey(Player player) {
        return map.containsKey(player.getUniqueId());
    }

    public List<Player> keySet() {
        return map.keySet().stream().map(Bukkit::getPlayer).collect(Collectors.toList());
    }

    public List<T> values() {
        return new ArrayList<>(map.values());
    }
}