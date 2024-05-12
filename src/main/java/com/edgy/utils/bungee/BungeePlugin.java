package com.edgy.utils.bungee;

import cloud.commandframework.bungee.BungeeCommandManager;
import com.edgy.utils.EdgyUtils;
import com.edgy.utils.bungee.BungeeCloudCommands.BungeeAbstractCommandContainer;
import com.edgy.utils.spigot.CloudCommands;
import com.edgy.utils.spigot.CloudCommands.AbstractCommandContainer;
import com.edgy.utils.shared.Manager;
import com.edgy.utils.shared.Messages;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeePlugin extends Plugin {

  private final List<Manager> managers = new ArrayList<>();

  private Messages<CommandSender> messages;

  @Override
  public void onEnable() {
    EdgyUtils.initializeBungee(this);

    try {
      messages = setupMessages();
      managers.addAll(setupManagers());
      BungeeCloudCommands.bungee(setupCommands());
      for (Listener listener : setupListeners()) {
        getProxy().getPluginManager().registerListener(this, listener);
      }
    } catch (Exception err) {
      err.printStackTrace();
    }
  }

  @Override
  public void onDisable() {
    BungeeTasks.clearAsyncs();
  }

  public final int reload() {
    int fails = 0;
    for (Manager manager : managers) {
      try {
        manager.reload();
      } catch (Exception err) {
        manager.log(Level.SEVERE, "Error while reloading", err);
        fails++;
      }
    }

    messages.reload(getDataFolder(), messages.getMessagesFile());

    return fails;
  }

  @SuppressWarnings("unchecked")
  public final <M extends Manager> M manager(Class<M> clazz) {
    for (Manager manager : managers) {
      if (manager.getClass().equals(clazz)) {
        return (M) manager;
      }
    }
    throw new IllegalArgumentException("Manager not found: " + clazz.getSimpleName());
  }

  public final Messages<CommandSender> messages() {
    return messages;
  }

  protected List<BungeeAbstractCommandContainer<CommandSender, BungeeCommandManager<CommandSender>>> setupCommands() {
    return new ArrayList<>();
  }
  protected abstract Messages<CommandSender> setupMessages();
  protected abstract List<Manager> setupManagers();
  protected abstract List<Listener> setupListeners();
}
