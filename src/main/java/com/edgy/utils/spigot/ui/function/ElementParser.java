package com.edgy.utils.spigot.ui.function;

import java.util.concurrent.CompletableFuture;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

@FunctionalInterface
public interface ElementParser<E> {

  CompletableFuture<E> apply(Player player, ConfigurationSection section);

}
