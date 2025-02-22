package com.edgy.utils.bungee.collections;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A list of online ProxiedPlayers. If a ProxiedPlayer disconnects, they are removed from the list. Also prevents
 * offline ProxiedPlayers from being added to the list.
 */
@SuppressWarnings("all")
public class OnlineProxiedPlayerList {

    private static final List<OnlineProxiedPlayerList> activeProxiedPlayerLists = new ArrayList<>();

    private final ArrayList<UUID> ProxiedPlayers = new ArrayList<>();

    public static List<OnlineProxiedPlayerList> getActiveProxiedPlayerLists() {
        return new ArrayList<>(activeProxiedPlayerLists);
    }

    public OnlineProxiedPlayerList() {
        activeProxiedPlayerLists.add(this);
    }

    public boolean add(ProxiedPlayer ProxiedPlayer) {
        if (!ProxiedPlayer.isConnected()) {
            return false;
        }

        return ProxiedPlayers.add(ProxiedPlayer.getUniqueId());
    }

    public boolean addAll(Collection<? extends ProxiedPlayer> c) {
        return ProxiedPlayers.addAll(c.stream().filter(ProxiedPlayer::isConnected).map(ProxiedPlayer::getUniqueId).collect(
                Collectors.toList()));
    }

    public boolean remove(ProxiedPlayer ProxiedPlayer) {
        return ProxiedPlayers.remove(ProxiedPlayer.getUniqueId());
    }

    public boolean removeAll(Collection<? extends ProxiedPlayer> c) {
        return ProxiedPlayers.removeAll(c.stream().map(ProxiedPlayer::getUniqueId).collect(Collectors.toList()));
    }

    public boolean contains(ProxiedPlayer ProxiedPlayer) {
        return ProxiedPlayers.contains(ProxiedPlayer.getUniqueId());
    }

    public boolean containsAll(Collection<? extends ProxiedPlayer> c) {
        return ProxiedPlayers.containsAll(c.stream().map(ProxiedPlayer::getUniqueId).collect(Collectors.toList()));
    }

    public List<ProxiedPlayer> getProxiedPlayers() {
        return ProxiedPlayers.stream().map(uuid -> ProxyServer.getInstance().getPlayer(uuid)).collect(Collectors.toList());
    }

    public ProxiedPlayer get(int index) {
        return ProxyServer.getInstance().getPlayer(ProxiedPlayers.get(index));
    }

    public int size() {
        return ProxiedPlayers.size();
    }

    public boolean isEmpty() {
        return ProxiedPlayers.isEmpty();
    }

    public void clear() {
        ProxiedPlayers.clear();
    }


    /**
     * Stop listening for ProxiedPlayer disconnects.
     */
    public void stopListening() {
        activeProxiedPlayerLists.remove(this);
    }

}
