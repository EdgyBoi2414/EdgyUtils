package com.edgy.utils.spigot.collections;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * A list of online players. If a player disconnects, they are removed from the list. Also prevents
 * offline players from being added to the list.
 */
@SuppressWarnings("all")
public class OnlinePlayerList {

  private static final List<OnlinePlayerList> activePlayerLists = new ArrayList<>();

  private final ArrayList<UUID> players = new ArrayList<>();

  public static List<OnlinePlayerList> getActivePlayerLists() {
    return new ArrayList<>(activePlayerLists);
  }

  public OnlinePlayerList() {
    activePlayerLists.add(this);
  }

  public boolean add(Player player) {
    if (!player.isOnline()) {
      return false;
    }

    return players.add(player.getUniqueId());
  }

  public boolean addAll(Collection<? extends Player> c) {
    return players.addAll(c.stream().filter(Player::isOnline).map(Player::getUniqueId).collect(
        Collectors.toList()));
  }

  public boolean remove(Player player) {
    return players.remove(player.getUniqueId());
  }

  public boolean removeAll(Collection<? extends Player> c) {
    return players.removeAll(c.stream().map(Player::getUniqueId).collect(Collectors.toList()));
  }

  public boolean contains(Player player) {
    return players.contains(player.getUniqueId());
  }

  public boolean containsAll(Collection<? extends Player> c) {
    return players.containsAll(c.stream().map(Player::getUniqueId).collect(Collectors.toList()));
  }

  public List<Player> getPlayers() {
    return players.stream().map(Bukkit::getPlayer).collect(Collectors.toList());
  }

  public Player get(int index) {
    return Bukkit.getPlayer(players.get(index));
  }

  public int size() {
    return players.size();
  }

  public boolean isEmpty() {
    return players.isEmpty();
  }

  public void clear() {
    players.clear();
  }


  /**
   * Stop listening for player disconnects.
   */
  public void stopListening() {
    activePlayerLists.remove(this);
  }

}